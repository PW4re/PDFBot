import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.grizzly.utils.Pair;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

public class Bot {
    private HashMap<String, User> users;
    private MessageManager messageManager;
    private String token;
    private ArrayList<String> fileFormats = new ArrayList<String>();

    public Bot(String token) {
        this.token = token;
        messageManager = new MessageManager();
        users = new HashMap<>();
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
        return "Доступные команды:\n\t/help - Как использовать бота.\n\t/merge - Соединить несколько .pdf файлов в один." +
                "\n\t/convert - сконвертировать файлы в .pdf. Доступные форматы для конвертации: jpg, jpeg, png";

    }

    public PartialBotApiMethod<Message> readMessage(Update update) throws IOException {
        Message message = update.getMessage();
        String name = message.getChatId().toString();
        if (!users.containsKey(name))
            users.put(name, new User());
        if (message.hasPhoto() || message.hasDocument())
            takePhotoOrDocument(message, name);
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
            users.get(name).setResultFileName(fileName);

            switch (command) {
                case "/merge":
                    if (users.get(name).getCondition() == UserConditions.WAITING)
                        return new SendMessage().setChatId(message.getChatId())
                                .setText("Пожалуйста, добавьте PDF-файлы для склейки");
                    users.get(name).setCondition(UserConditions.FINISHING_MERGE);
                    break;
                case "/convert":
                    if (users.get(name).getCondition() == UserConditions.WAITING)
                        return new SendMessage().setChatId(message.getChatId())
                                .setText("Пожалуйста, добавьте фото или документ для конвертации в PDF-формат");
                    users.get(name).setCondition(UserConditions.FINISHING_CONVERT);
                    break;
                case "/help":
                    return new SendMessage().setChatId(message.getChatId())
                            .setText(getHelpString());
                case "/docs":
                    if (users.get(name).getDocsNames().isEmpty())
                        return new SendMessage().setChatId(message.getChatId()).setText("Вы пока что не добавили файлов!");

                    if (users.get(name).getCondition() == UserConditions.ADDING) {
                        String fileNames = getCurrentDocumentsNames(users.get(name).getDocsNames());
                        return new SendMessage().setChatId(message.getChatId()).setText(fileNames);
                    }
                    break;
                case "/removelast":
                    if (users.get(name).getDocsNames().isEmpty())
                        return new SendMessage().setChatId(message.getChatId()).setText("Вы пока что не добавили файлов!");
                    else {
                        users.get(name).removeLastDoc();
                        return new SendMessage().setChatId(message.getChatId()).setText("Последний файл успешно удалён.");
                    }
                case "/bug_report":

                case "/next_bug":

                case "/reverse":
                    users.get(name).reverseDocs();
                    break;
                default:
                    users.get(name).setDefaultName();
                    return new SendMessage().setChatId(update.getMessage().getChatId())
                            .setText("Я не знаю такой команды((");

            }
        }
        return messageManager.processMessage(users.get(name), message.getChatId());
    }
}