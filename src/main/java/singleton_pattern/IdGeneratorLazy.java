package singleton_pattern;

import java.util.concurrent.atomic.AtomicLong;

/*
懒汉式:
    1. getInstance有一把大锁导致并发度低
    2. 如果这个方法频繁的用到，那么频繁加锁解锁会导致性能瓶颈
 */

public class IdGeneratorLazy {
    private AtomicLong id = new AtomicLong(0);
    private static IdGeneratorLazy instance;
    private IdGeneratorLazy() {};

    public  static synchronized IdGeneratorLazy getInstance() {
        if (instance == null) {
            instance = new IdGeneratorLazy();
        }
        return instance;
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
