### Bridge pattern

Decouple an abstraction from its implementation so that the two can vary independently, 将抽象和现实解耦，让他们可以独立变化

Using JDBC as an example:

```java
CLass.forName("com.mysql.jdbc.Driver");  // load and register JDBC driver
String url = "jdbc:mysql://localhost:3306/sample_db?user=root&password=password";
Connection conn = DriverManager.getConnection(url);
Statement stmt = con.createStatement();
String query = "SELECT * FROM test";
ResultSet res = stmt.executeQuery(query);
while (res.next()) {
  res.getString(1);
  res.getInt(2);
}
```

When we want to change from MySQL to Oracle, it is simply just change from `com.mysql.jdbc.Driver` to `oracle.jdbc.driver.OracleDriver`. Now, the question is, how did we acheive this?

### JDBC code

Let's take a look at `com.mysql.jdbc.Driver` class

```java
package com.mysql.jdbc;
import java.sql.SQLException;

public class Driver extends NonRegisteringDriver implements java.sql.Driver {
  static {
    try {
      java.sql.DriverManager.registerDriver(new Driver());
    } catch (SQLException E) {
      throw new RuntimeException("Can't register driver!");
    }
  }

  /**
   * Construct a new driver and register it with DriverManager
   * @throws SQLException if a database error occurs.
   */
  public Driver() throws SQLException {
    // Required for Class.forName().newInstance()
  }
}
```

When `Class.forName("com.mysql.jdbc.Driver")` executes, there are two things happened:

- JVM finds and loads the `Driver` class
- `static` code block executes, which registers the `Driver` class to `DriverManager`



Now let's take a look at `DriverManager` class

```java

public class DriverManager {
  private final static CopyOnWriteArrayList<DriverInfo> registeredDrivers = new CopyOnWriteArrayList<DriverInfo>();

  //...
  static {
    loadInitialDrivers();
    println("JDBC DriverManager initialized");
  }
  //...

  public static synchronized void registerDriver(java.sql.Driver driver) throws SQLException {
    if (driver != null) {
      registeredDrivers.addIfAbsent(new DriverInfo(driver));
    } else {
      throw new NullPointerException();
    }
  }

  public static Connection getConnection(String url, String user, String password) throws SQLException {
    java.util.Properties info = new java.util.Properties();
    if (user != null) {
      info.put("user", user);
    }
    if (password != null) {
      info.put("password", password);
    }
    return (getConnection(url, info, Reflection.getCallerClass()));
  }
  //...
}
```

- JDBC is the abstraction

- Concrete Driver is the implementation