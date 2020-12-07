import java.io.InputStream;

public class DocumentInfo {
    private InputStream data;
    private String name;
    public DocumentInfo(String name, InputStream data) {
        this.data = data;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public InputStream getData(){
        return data;
    }
}
