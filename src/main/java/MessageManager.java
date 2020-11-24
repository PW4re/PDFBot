import java.io.*;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;

import com.itextpdf.text.DocumentException;
import jdk.jshell.spi.ExecutionControl.NotImplementedException;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

public class MessageManager {;
    private PDFMerger merger;
    private Converter converter;

    public MessageManager() {
        merger = new PDFMerger();
        converter = new Converter();
    }



    public PartialBotApiMethod<Message> processMessage(User user, UserConditions condition, Long chatId) {
        PartialBotApiMethod result = null;
        ArrayDeque<InputStream> inputStreams = user.getDocs();
        if (condition == UserConditions.FINISHING_MERGE) {
            result = processTextMessageToMerge(user.getName(), inputStreams, chatId);
        }

        if (condition == UserConditions.FINISHING_CONVERT) {
            result = processTextMessageToConvert(user.getName(), inputStreams, chatId);
        }

        return result;
    }

    private PartialBotApiMethod<Message> processTextMessageToMerge(String userName,
                                                                   ArrayDeque<InputStream> inputStreams, long chatId) {
        merger.setUserName(userName);
        for (InputStream inputStream : inputStreams)
            merger.addToMerge(inputStream);
        ByteArrayInputStream byteArrayInStream = merger.merge();
        return new SendDocument().setChatId(chatId).setDocument("abc.pdf", byteArrayInStream);
    }

    private PartialBotApiMethod<Message> processTextMessageToConvert(String userName,
                                                                     ArrayDeque<InputStream> inputStreams, long chatId) {
        for (InputStream inputStream : inputStreams)
            converter.addToConvert(inputStream);
        ByteArrayInputStream byteArrayInStream = converter.convert();
        return new SendDocument().setChatId(chatId).setDocument("converted.pdf", byteArrayInStream);
    }
}