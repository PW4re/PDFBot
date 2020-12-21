import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import redis.clients.jedis.Jedis;

import java.io.IOException;


public class Telebot extends TelegramLongPollingBot {
    private static Bot bot;
    private static String token;



    public Telebot() {

    }

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        token = args[0];
        bot = new Bot(token);
        try {
            botsApi.registerBot(new Telebot());
        } catch (TelegramApiException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "";
    }

    @Override
    public String getBotToken() { return token; }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                PartialBotApiMethod<Message> message = bot.readMessage(update);
                if (message != null) {
                    try {
                        if (message instanceof SendMessage) {
                            this.execute((SendMessage) message);
                        }

                        if (message instanceof SendDocument) {
                            this.execute(((SendDocument) message));
                            bot.clear(update.getMessage().getChat().getUserName());
                        }
                    } catch (TelegramApiException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (IOException e){
            e.printStackTrace();
        }

    }
}