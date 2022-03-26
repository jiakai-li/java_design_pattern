### Decorator pattern and Java stream IO
Consider how we usually use Java `InputStream` class to read a file:
```java
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileRead {
    public static void main(String[] args) {
        InputStream in = new FileInputStream("test.txt");
        InputStream buffered_in = new BufferedInputStream(in);
        byte[] data = new byte[128];
        while(buffered_in.read(data) != -1) {
            ...
        }
    }
}
```
This is an example of decorator pattern. But consider what happens if instead we use inheritance to enhance the functionality of IO streams, we might have a special class like:
```java
InputStream bin = new BufferedFileInputStream("test.txt");
byte[] data = new byte[128];
while(bin.read(data) != -1) {
    ...
}
```
This would work, but what happens if we want to extend the class to add more functionalities? We might add more classes with different combinations, which leads to an inheritance explosion

### Design based on decorator
We can solve the above problem using composition
```java

public abstract class InputStream {
  //...
  public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }
  
  public int read(byte b[], int off, int len) throws IOException {
    //...
  }
  
  public long skip(long n) throws IOException {
    //...
  }

  public int available() throws IOException {
    return 0;
  }
  
  public void close() throws IOException {}

  public synchronized void mark(int readlimit) {}
    
  public synchronized void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }
}

public class BufferedInputStream extends InputStream {
  protected volatile InputStream in;

  protected BufferedInputStream(InputStream in) {
    this.in = in;
  }
  
  //...Implementations and enhancements
}

public class DataInputStream extends InputStream {
  protected volatile InputStream in;

  protected DataInputStream(InputStream in) {
    this.in = in;
  }
  
  //...Implementations and enhancements
}
```

Special notes about decorator patterns:
- Decorator class and Original class inherit from the same Base class, as a result, we can wrap the original class with multiple decorator classes. For example:
  ```java
  InputStream in = new FileInputStream("/user/wangzheng/test.txt");
  InputStream bin = new BufferedInputStream(in);
  DataInputStream din = new DataInputStream(bin);
  int data = din.readInt();
  ```
- Decorator class is used to enhance the original class, add some special functionalities. Compare to proxy pattern, decorator pattern adds functionalities that is related to the original class. Proxy pattern adds functionalities that might be completely different from the original class

Compare proxy pattern and decorator pattern:
```java

// Proxy pattern structure
public interface IA {
  void f();
}
public class A impelements IA {
  public void f() { //... }
}
public class AProxy implements IA {
  private IA a;
  public AProxy(IA a) {
    this.a = a;
  }
  
  public void f() {
    // some logic
    a.f();
    // some logic
  }
}

// Decorator pattern structure
public interface IA {
  void f();
}
public class A implements IA {
  public void f() { //... }
}
public class ADecorator implements IA {
  private IA a;
  public ADecorator(IA a) {
    this.a = a;
  }
  
  public void f() {
    // more functions
    a.f();
    // more functions
  }
}
```

### Implementation notes
Because decorator class inherits from the same base class, which might be abstract itself. Thus, decorator class need to implement all the required methods, even in case it just pass the function call to the wrapped original class. This is redundant, and as a result, Java has a base concrete class `FilterInputStream`:
```java
public class FilterInputStream extends InputStream {
  protected volatile InputStream in;

  protected FilterInputStream(InputStream in) {
    this.in = in;
  }

  public int read() throws IOException {
    return in.read();
  }

  public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }
   
  public int read(byte b[], int off, int len) throws IOException {
    return in.read(b, off, len);
  }

  public long skip(long n) throws IOException {
    return in.skip(n);
  }

  public int available() throws IOException {
    return in.available();
  }

  public void close() throws IOException {
    in.close();
  }

  public synchronized void mark(int readlimit) {
    in.mark(readlimit);
  }

  public synchronized void reset() throws IOException {
    in.reset();
  }

  public boolean markSupported() {
    return in.markSupported();
  }
}
```
The other decorator classes can directly inherit from this concrete base class, and only implement the ones that it needs to enhance, and leave the rest method with the default implementation from the base class
