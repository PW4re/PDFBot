import org.apache.http.impl.conn.SystemDefaultRoutePlanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

public class FileManager { // тоже Singleton. А еще можно попробовать вместо создания файлов ByteArrayOutputStream
    private HashMap<String, Integer> names;

    public FileManager(){
        names = new HashMap<>();
    }

    public boolean directoryExists(String userId) { return names.containsKey(userId + "Files"); }

    public void createDirectory(String userId) {
        File directory = new File(userId + "Files");
        boolean res = directory.mkdir();
        if (!res)
            System.out.println("Не получилось создать директорию " + userId);
        names.put(directory.getName(), 100);
    }

    public void deleteDirectory(String userId){
        String name = userId + "Files";
        File directory  = new File(name);
        if (names.containsKey(name)){
            recursiveDelete(directory);
            System.out.println("Директория " + name +" удалена");
            names.remove(name);
        }
        else{
            System.out.println("Не получилось удалить директорию " + name);
        }
    }

    public void clearAll(){
        for (String dirName : names.keySet()) {
            File directory = new File(dirName + "Files");
            recursiveDelete(directory);
            names.remove(dirName);
        }
    }

    private void recursiveDelete(File file) {
        if (!file.exists())
            return;

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                recursiveDelete(f);
            }
        }
        if (!file.delete())
            System.out.println("Не удалось удалить " + file.getName());
    }
}
