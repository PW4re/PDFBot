import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.*;
import java.io.File;

public class PDFMerger {
    private PDFMergerUtility merger;
    private String userName;

    public PDFMerger() {
        merger = new PDFMergerUtility();
    }

    public void setUserName(String name) {
        userName = name;
    }

    public void addToMerge(InputStream inputStream) {
        merger.addSource(inputStream);
    }

    public ByteArrayInputStream merge() {
        ByteArrayOutputStream outStream = null;
        try {
            outStream = new ByteArrayOutputStream();
            merger.setDestinationStream(outStream);
            merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
            merger = new PDFMergerUtility();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(outStream.toByteArray());
    }
}