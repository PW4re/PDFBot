import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.*;
import java.io.File;

public class PDFMerger {
    private PDFMergerUtility merger;

    public PDFMerger() {
        merger = new PDFMergerUtility();
    }

    public void addToMerge(DocumentInfo doc) {
        if (doc.getName().endsWith(".pdf"))
            merger.addSource(doc.getData());
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