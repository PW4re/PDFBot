import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.*;
import java.util.ArrayList;

public class Converter {
    private ArrayList<DocumentInfo> docs;
    private PDFMerger merger;
    ArrayList<ByteArrayInputStream> results;

    public Converter() {
        docs = new ArrayList<>();
        merger = new PDFMerger();
        results = new ArrayList<>();
    }

    public void addToConvert(DocumentInfo doc) {
        docs.add(doc);
    }

    public ByteArrayInputStream convert() {
        if (docs.isEmpty())
            return null;

        for (DocumentInfo doc : docs) {
            try {
                String name = doc.getName();

                if (isValidPhotoFormat(name))
                    results.add(convertImage(doc.getData()));

                if(isValidTextFormat(name))
                    results.add(convertText(doc.getData()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        docs.clear();
        if (results.size() == 1)
            return results.remove(0);
        else {
            for (ByteArrayInputStream result : results) {
                merger.addToMerge(new DocumentInfo("name.pdf", result));
            }
            results.clear();
            return merger.merge();
        }
    }

    public boolean isValidTextFormat(String docName){
        return docName.endsWith(".txt");
    }

    public boolean isValidPhotoFormat(String docName){
        return docName.endsWith(".png") || docName.endsWith(".jpg") || docName.endsWith(".jpeg");
    }

    private ByteArrayInputStream convertImage(InputStream inputStream) throws IOException, DocumentException {
        ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        Document doc = new Document();
        PdfWriter writer = PdfWriter.getInstance(doc, byteArrayOutStream);
        Image image = Image.getInstance(inputStream.readAllBytes());
        image.setAlignment(Element.ALIGN_CENTER);
        float scaleX = ((doc.getPageSize().getWidth() - doc.leftMargin() - doc.rightMargin()) / image.getWidth()) * 100;
        float scaleY = ((doc.getPageSize().getHeight() - doc.bottomMargin() - doc.topMargin()) / image.getHeight()) * 100;
        float scale = Math.min(scaleX, scaleY);
        image.scalePercent(scale);
        writer.open();
        doc.open();
        doc.add(image);
        doc.close();
        writer.flush();
        writer.close();

        return new ByteArrayInputStream(byteArrayOutStream.toByteArray());
    }

    private ByteArrayInputStream convertText(InputStream inputStream) throws IOException, DocumentException {
        Document pdfDoc = new Document(PageSize.A4);
        ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
        PdfWriter.getInstance(pdfDoc, byteArrayOutStream).setPdfVersion(PdfWriter.PDF_VERSION_1_7);
        pdfDoc.open();
        Font myFont = new Font();
        myFont.setStyle(Font.NORMAL);
        myFont.setSize(11);
        pdfDoc.add(new Paragraph("\n"));
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String strLine ;
        while ((strLine = br.readLine()) != null) {
            Paragraph para = new Paragraph(strLine + "\n", myFont);
            para.setAlignment(Element.ALIGN_JUSTIFIED);
            pdfDoc.add(para);
        }
        pdfDoc.close();
        br.close();

        return new ByteArrayInputStream(byteArrayOutStream.toByteArray());
    }
}