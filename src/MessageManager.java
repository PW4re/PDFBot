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



    public PartialBotApiMethod<Message> processMessage(User user, Long chatId) {
        PartialBotApiMethod result = null;
        UserConditions condition = user.getCondition();

        ArrayDeque<DocumentInfo> docs = user.getDocumentInfos();
        if (condition == UserConditions.FINISHING_MERGE) {
            result = processTextMessageToMerge(docs, chatId);
        }

        if (condition == UserConditions.FINISHING_CONVERT) {
            result = processTextMessageToConvert(docs, chatId);
        }

        return result;
    }

    private PartialBotApiMethod<Message> processTextMessageToMerge(ArrayDeque<DocumentInfo> docs, long chatId) {
        for (DocumentInfo doc : docs)
            merger.addToMerge(doc);
        ByteArrayInputStream byteArrayInStream = merger.merge();
        return new SendDocument().setChatId(chatId).setDocument("merged.pdf", byteArrayInStream);
    }

    private PartialBotApiMethod<Message> processTextMessageToConvert(ArrayDeque<DocumentInfo> docs, long chatId) {
        for (DocumentInfo doc : docs)
            converter.addToConvert(doc);
        ByteArrayInputStream byteArrayInStream = converter.convert();
        return new SendDocument().setChatId(chatId).setDocument("converted.pdf", byteArrayInStream);
    }
}