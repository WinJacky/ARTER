package main.java.lock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LockHelper {

    private static final ReentrantLock lock = new ReentrantLock();
    private static Map<String, Condition> conditions;

    public LockHelper() {
    }

    public static Condition getNewCondition() {
        return lock.newCondition();
    }

    public static Condition setNewCondition(String name) {
        Condition condition = lock.newCondition();
        if (conditions == null) {
            conditions = new HashMap<>();
        }
        conditions.put(name, condition);
        return condition;
    }

    public static Condition getCondition(String name) {
        return conditions.get(name);
    }

    public static ReentrantLock getLock() {
        return lock;
    }
}
