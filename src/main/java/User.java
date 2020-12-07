import javax.swing.text.Document;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class User {
    private ArrayDeque<DocumentInfo> usersDocs = new ArrayDeque<DocumentInfo>();
    private UserConditions currentCondition = UserConditions.WAITING;
    private String name;

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public UserConditions getCondition() {
        return currentCondition;
    }

    public void removeLastDoc(){
        usersDocs.removeLast();
    }

    public ArrayList<InputStream> getDocsData(){
        ArrayList<InputStream> docsData = new ArrayList<>();
        for (var i : usersDocs) {
            docsData.add(i.getData());
        }
        return docsData;
    }

    public ArrayDeque<DocumentInfo> getDocumentInfos()
    {
        return usersDocs;
    }

    public ArrayList<String> getDocsNames(){
        ArrayList<String> docsNames = new ArrayList<>();
        for (var i : usersDocs) {
            docsNames.add(i.getName());
        }
        return docsNames;
    }

    public void addDoc(String docName, InputStream doc){
        usersDocs.add(new DocumentInfo(docName, doc));
    }

    public void addDoc(DocumentInfo docInfo){
        usersDocs.add(docInfo);
    }

    public void clearDocs(){
        usersDocs = new ArrayDeque<DocumentInfo>();
    }

    public void setCondition(UserConditions condition){
        currentCondition = condition;
    }
}