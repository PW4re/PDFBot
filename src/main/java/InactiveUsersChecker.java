import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;


public class InactiveUsersChecker implements Runnable {
    private HashMap<String, User> users;

    public InactiveUsersChecker(HashMap<String, User> users) {
        this.users = users;
    }

    @Override
    public void run() {
        while(true){
            try {
//                ReentrantLock locker
                Thread.sleep(1000);
                checkInactiveUsers();
            } catch (InterruptedException ignore) {

            }
        }
    }

    private void checkInactiveUsers() {
        long timeNow = System.currentTimeMillis();
        ArrayList<Map.Entry> willBeRemoved = new ArrayList<>();

        for (Map.Entry<String, User> entry : users.entrySet()) {
            User user = entry.getValue();
            long duration = TimeUnit.MINUTES.convert(timeNow - user.getLastActionTime(),
                    TimeUnit.MILLISECONDS);
            if (duration >= 20) {
                willBeRemoved.add(entry);
            }
        }

        for (Map.Entry<String, User> entry : willBeRemoved)
            users.remove(entry.getKey(), entry.getValue());
        willBeRemoved.clear();
    }
}
