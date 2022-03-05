package singleton_pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
实现单例类：
    1. 构造函数需要是private访问权限，避免外部通过new创建实例
    2. 考虑对象创建时的线程安全问题
    3. 考虑是否支持延迟加载
    4. 考虑getInstance方法是否高性能（是否加锁）

单例类的用处：
    从业务概念上，有些数据在系统中只应该保存一份，就比较适合设计为单例类
 */

public class Logger {
    private FileWriter writer;
    private static final Logger instance = new Logger();

    private Logger() {
        File file = new File("/Users/jiakai/Desktop/log.txt");
        try {
            writer = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getInstance() {
        return instance;
    }

    public void log(String message) {
        try {
            writer.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
