package singleton_pattern;

import java.util.concurrent.atomic.AtomicLong;

/*
饿汉式:
    1. instance实例的创建过程是线程安全的
    2. 这样的实现方式不支持延迟加载
    3. 如果初始化耗时较长，那最好不要等到真正要用到它的时候才区执行这个过程
 */

public class IdGeneratorHungry {
    private AtomicLong id = new AtomicLong(0);
    private static final IdGeneratorHungry instance = new IdGeneratorHungry();

    private IdGeneratorHungry() {};

    public static IdGeneratorHungry getInstance() {
        return instance;
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
