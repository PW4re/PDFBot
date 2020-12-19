import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.jshell.spi.ExecutionControl;
import org.glassfish.grizzly.utils.Pair;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

public class Bot {
    private HashMap<String, User> users;
    private MessageManager messageManager;
    private String token;
    private ArrayList<KeyboardRow> keyboard = new ArrayList<>();
    private KeyboardRow firstRow = new KeyboardRow();
    private KeyboardRow secondRow = new KeyboardRow();
    ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();

    public Bot(String token) {
        this.token = token;
        messageManager = new MessageManager();
        users = new HashMap<>();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        Thread thread = new Thread(new InactiveUsersChecker(users));
        thread.start();
    }

    private URL uploadFile(String file_id) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + token + "/getFile?file_id=" + file_id);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String res = in.readLine();
        JSONObject jsonResult = new JSONObject(res);
        JSONObject path = jsonResult.getJSONObject("result");
        String file_path = path.getString("file_path");
        System.out.println(file_path);
        return new URL("https://api.telegram.org/file/bot" + token + "/" + file_path);
    }

    public void clear(String name) {
        users.remove(name);
    }

    private Pair<String, String> parseCommand(String message) throws Exception {
        if (message.equals(""))
            return new Pair<>("", "");
        String[] parts = message.split(" ");
        String command = parts[0];
        if (parts.length == 1)
            return new Pair<>(command, "");
        String fileName = String.join(" ", Arrays.copyOfRange(parts, 1, parts.length));
        Pattern pattern = Pattern.compile("[/\\:?*\"<>|]");
        Matcher matcher = pattern.matcher(fileName);
        if (fileName.length() > 80 || matcher.find())
            throw new Exception("Invalid name!");

        return new Pair<>(command, fileName);
    }

    private void takePhotoOrDocument(Message message, String name) throws IOException {
        if (message.hasDocument()){
            Document doc = message.getDocument();
            String fileId = doc.getFileId();
            String docName = doc.getFileName();
            users.get(name).addDoc(docName, uploadFile(fileId).openStream());
            users.get(name).setCondition(UserConditions.ADDING);
        }
        else if (message.hasPhoto()) {
            int max = 0;
            PhotoSize highestQuality = null;
            for (PhotoSize photoSize : message.getPhoto()) {
                if (photoSize.getHeight() * photoSize.getWidth() >= max) {
                    max = photoSize.getHeight() * photoSize.getWidth();
                    highestQuality = photoSize;
                }
            }
            assert highestQuality != null;
            var uniqueId = highestQuality.getFileUniqueId();
            users.get(name).addDoc(uniqueId + ".jpg", uploadFile(highestQuality.getFileId()).openStream());
            users.get(name).setCondition(UserConditions.ADDING);
        }
    }

    private String getCurrentDocumentsNames(ArrayList<String> names) {
        StringBuilder sb = new StringBuilder();
        sb.append("Текущие добавленные файлы:\n");
        for (var i : names) {
            sb.append(i);
            sb.append(", ");
            sb.append("\n");
        }
        sb.delete(sb.length()-3, sb.length() - 1);

        return sb.toString();
    }

    private String getHelpString() {
        return """
                Доступные команды:
                 /help - Как использовать бота.
                 /merge - Соединить несколько .pdf файлов в один.
                 /convert - сконвертировать файлы в .pdf. Доступные форматы для конвертации: jpg, jpeg, png. Если добавить несколько файлов, то в результате они объединятся автоматчески.
                 /docs - вывести список добавленных документов.
                 /removelast - удалить последний добавленный файл.
                 /reverse - развернуть список добавленных файлов (склеивание будет производиться в обратном порядке).
                """;
    }

    public PartialBotApiMethod<Message> readMessage(Update update) throws IOException {
        Message message = update.getMessage();
        String chatId = message.getChatId().toString();
        if (!users.containsKey(chatId)) {
            users.put(chatId, new User());
        }
        users.get(chatId).setLastActionTime(System.currentTimeMillis());
        if (message.hasPhoto() || message.hasDocument()) {
            takePhotoOrDocument(message, chatId);
            configureKeyboard();
            return new SendMessage().setChatId(chatId).setText("Добавлено").setReplyMarkup(replyKeyboardMarkup);
        }
        else if (message.hasText()){
            Pair<String, String> commandAndFileName;
            try {
                commandAndFileName = parseCommand(message.getText());
            } catch (Exception e) {
                return new SendMessage().setChatId(message.getChatId()).setText("Длина имени файла не должна " +
                        "превышать 80 символов, в имени файла не должны встречаться символы: /:\\?*\"<>|");
            }
            String command = commandAndFileName.getFirst();
            String fileName = commandAndFileName.getSecond();
            users.get(chatId).setResultFileName(fileName);

            switch (command) {

                case "/merge":
                    if (users.get(chatId).getCondition() == UserConditions.WAITING)
                        return new SendMessage().setChatId(message.getChatId())
                                .setText("Пожалуйста, добавьте PDF-файлы для склейки");
                    users.get(chatId).setCondition(UserConditions.FINISHING_MERGE);
                    break;

                case "/convert":
                    if (users.get(chatId).getCondition() == UserConditions.WAITING)
                        return new SendMessage().setChatId(message.getChatId())
                                .setText("Пожалуйста, добавьте фото или документ для конвертации в PDF-формат");
                    users.get(chatId).setCondition(UserConditions.FINISHING_CONVERT);
                    break;

                case "/help":
                    return new SendMessage().setChatId(message.getChatId())
                            .setText(getHelpString());

                case "/docs":
                    if (users.get(chatId).getDocsNames().isEmpty())
                        return new SendMessage().setChatId(message.getChatId()).setText("Вы пока что не добавили файлов!");

                    if (users.get(chatId).getCondition() == UserConditions.ADDING) {
                        String fileNames = getCurrentDocumentsNames(users.get(chatId).getDocsNames());
                        return new SendMessage().setChatId(message.getChatId()).setText(fileNames);
                    }
                    break;

                case "/removelast":
                    if (users.get(chatId).getDocsNames().isEmpty())
                        return new SendMessage().setChatId(message.getChatId()).setText("Вы пока что не добавили файлов!");
                    else {
                        users.get(chatId).removeLastDoc();
                        return new SendMessage().setChatId(message.getChatId()).setText("Последний файл успешно удалён.");
                    }

                case "/removeall":
                    users.get(chatId).clearDocs();
                    return new SendMessage().setChatId(message.getChatId()).setText("Файлы успешно удалены");

                case "/bug_report":

                case "/next_bug":

                case "/reverse":
                    users.get(chatId).reverseDocs();
                    break;
                default:
                    users.get(chatId).setDefaultName();

                    return new SendMessage().setChatId(update.getMessage().getChatId())
                            .setText("Я не знаю такой команды((");

            }
        }
        clearKeyboard();
        return messageManager.processMessage(users.get(chatId), message.getChatId());
    }

    public void clearKeyboard(){
        keyboard.clear();
        replyKeyboardMarkup.setKeyboard(keyboard);
    }

    public void configureKeyboard(){
        keyboard.clear();
        firstRow.clear();
        firstRow.add("/merge");
        firstRow.add("/convert");
        keyboard.add(firstRow);
        replyKeyboardMarkup.setKeyboard(keyboard);
    }
}