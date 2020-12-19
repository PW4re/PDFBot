import javax.print.Doc;
import javax.swing.text.Document;
import java.io.InputStream;
import java.util.*;

public class User {
    private ArrayDeque<DocumentInfo> usersDocs = new ArrayDeque<>();
    private UserConditions currentCondition = UserConditions.WAITING;
    private String resultFileName;
    private Long lastActionTime;

    public User() {
        setDefaultName();
        lastActionTime = System.currentTimeMillis();
    }

    public void setLastActionTime(long time) { lastActionTime = time; }

    public Long getLastActionTime() { return lastActionTime; }

    public String getResultFileName() { return resultFileName; }

    public void setResultFileName(String name) {
        if (!name.equals(""))
            resultFileName  = name.endsWith(".pdf") ? name : name + ".pdf";
    }

    public void setDefaultName() { resultFileName = "result.pdf"; }

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

    public void reverseDocs() {
        ArrayDeque<DocumentInfo> reversedQueue = new ArrayDeque<>();
        for (Iterator<DocumentInfo> it = usersDocs.descendingIterator(); it.hasNext(); ) {
            DocumentInfo dInfo = it.next();
            reversedQueue.add(dInfo);
        }
        usersDocs = reversedQueue;
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
        usersDocs = new ArrayDeque<>();
    }

    public void setCondition(UserConditions condition){
        currentCondition = condition;
    }
}