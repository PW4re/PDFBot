import java.io.InputStream;
import java.util.ArrayDeque;

public class User {
    private ArrayDeque<InputStream> usersDocs = new ArrayDeque<>();
    private UserConditions currentCondition = UserConditions.WAITING; // ключ - расширениеб значение - список InputStream
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

    public ArrayDeque<InputStream> getDocs(){
        return usersDocs;
    }

    public void addDoc(InputStream doc){
        usersDocs.add(doc);
    }

    public void clearDocs(){
        usersDocs = new ArrayDeque<InputStream>();
    }

    public void setCondition(UserConditions condition){
        currentCondition = condition;
    }
}