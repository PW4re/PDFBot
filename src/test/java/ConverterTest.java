import com.itextpdf.text.DocumentException;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ConverterTest {
    private static FileManager fileManager = new FileManager();
    private static String defaultUser = "Programmer";
    private static Converter converter = new Converter();
    private static InputStream inputStream;

    @org.junit.BeforeClass
    public static void setUp() throws IOException {
        fileManager.createDirectory(defaultUser);
        inputStream = new ByteArrayInputStream("abc".getBytes());
    }


    @Test
    public void convertTextTest() throws DocumentException, IOException {
//        converter.addToConvert(inputStream);
//        char[] buf = new char[3];
//        File file = converter.convertText();
//        int c;
//        FileReader fr = new FileReader(file.getPath());
//        while((c = fr.read(buf))>0){
//            if(c < 3){
//                buf = Arrays.copyOf(buf, c);
//            }
//        }
//        fr.close();
    }

    @org.junit.AfterClass
    public static void tearDown() {
        fileManager.deleteDirectory("Programmer");
    }
}
