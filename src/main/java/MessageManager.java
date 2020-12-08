import java.io.*;
import java.util.ArrayDeque;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageManager {;
    private PDFMerger merger;
    private Converter converter;

    public MessageManager() {
        merger = new PDFMerger();
        converter = new Converter();
    }



    public PartialBotApiMethod<Message> processMessage(User user, Long chatId) {
        PartialBotApiMethod<Message> result = null;
        UserConditions condition = user.getCondition();
        ArrayDeque<DocumentInfo> docs = user.getDocumentInfos();
        String resultFileName = user.getResultFileName();
        if (condition == UserConditions.FINISHING_MERGE) {
            result = processTextMessageToMerge(resultFileName, docs, chatId);
            user.clearDocs();
        }

        if (condition == UserConditions.FINISHING_CONVERT) {
            result = processTextMessageToConvert(resultFileName, docs, chatId);
            user.clearDocs();
        }


        return result;
    }

    private PartialBotApiMethod<Message> processTextMessageToMerge(String fileName,
                                                                   ArrayDeque<DocumentInfo> docs, long chatId) {
        for (DocumentInfo doc : docs)
            merger.addToMerge(doc);
        ByteArrayInputStream byteArrayInStream = merger.merge();
        if (fileIsEmpty(byteArrayInStream))
            return new SendMessage().setChatId(chatId).setText("Среди добавленных файлов не " +
                    "нашлось подходящий для склейки.");
        return new SendDocument().setChatId(chatId).setDocument(fileName, byteArrayInStream);
    }

    private PartialBotApiMethod<Message> processTextMessageToConvert(String fileName,
                                                                     ArrayDeque<DocumentInfo> docs, long chatId) {
        for (DocumentInfo doc : docs)
            converter.addToConvert(doc);
        ByteArrayInputStream byteArrayInStream = converter.convert();
        if (fileIsEmpty(byteArrayInStream))
            return new SendMessage().setChatId(chatId).setText("Среди добавленных файлов не " +
                    "нашлось подходящий для конвертирования.");
        return new SendDocument().setChatId(chatId).setDocument(fileName, byteArrayInStream);
    }

    private boolean fileIsEmpty(ByteArrayInputStream byteArrayInputStream) {
        return byteArrayInputStream.available() == 0;
    }
}