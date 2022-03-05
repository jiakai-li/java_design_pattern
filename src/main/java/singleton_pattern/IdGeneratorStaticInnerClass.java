package singleton_pattern;

import java.util.concurrent.atomic.AtomicLong;

/*
静态内部类
    1. SingletonHolder是一个静态内部类，当外部类IdGenerator被加载的时候，并不会创建SingletonHolder实例对象
    2. 只有当调用getInstance方法时，SingletonHolder才会创建instance
    3. instance的唯一性，创建过程的线程安全性，都由JVM来保证
    4. 保证了线程安全，又能做到延迟加载
 */

public class IdGeneratorStaticInnerClass {
    private AtomicLong id = new AtomicLong(0);
    private IdGeneratorStaticInnerClass() {};

    private static class SingletonHolder {
        private static final IdGeneratorStaticInnerClass instance = new IdGeneratorStaticInnerClass();
    }

    public static IdGeneratorStaticInnerClass getInstance() {
        return SingletonHolder.instance;
    }

    public long getId() {
        return id.incrementAndGet();
    }
}
