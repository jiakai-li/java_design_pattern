### Proxy pattern
Extend functionality using proxies (without changing the original class, or proxied class)

For example, take a look at below code, which uses metrics collector to generate some API stats
```java
public class UserController {
  //...getter and setters...
  private MetricsCollector metricsCollector; // dependency injection

  public UserVo login(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    // ... login logic...

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    //...return user VO...
  }

  public UserVo register(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    // ... register logic...

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    //...return user VO...
  }
}
```

There are two problems with this code:
- `metricsCollecor` is highly coupled with business code, it will be difficult to change
- the code is a violation of single responsibility rule, because we are doing two things within the same class

### Use proxy pattern
We can let proxy and real class implement the same interface, and inject real class into the proxy class
```java
public interface IUserController {
    UserVo login(String telephone, String password);
    UserVo register(String telephone, String password);
}

public class UserController implements IUserController {
    //...

    @Override
    public UserVo login(String telephone, String password) {
        //...login logic...
        //...return user VO...
    }

    @Override
    public UserVo register(String telephone, String password) {
        //...register logic...
        //...return user VO...
    }
}

// proxy class implements the same interface
public class UserControllerProxy implements IUserController {
    private MetricsCollector metricsCollector;
    private UserController userController;

    public UserControllerProxy(UserController userController) {
        this.userController = userController;
        this.metricsCollector = new MetricsCollector();
    }

    @Override
    public UserVo login(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        // use real class to handle the business logic
        UserVo userVo = userController.login(telephone, password);

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        return userVo;
    }

    @Override
    public UserVo register(String telephone, String password) {
        long startTimestamp = System.currentTimeMillis();

        UserVo userVo = userController.register(telephone, password);

        long endTimeStamp = System.currentTimeMillis();
        long responseTime = endTimeStamp - startTimestamp;
        RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
        metricsCollector.recordRequest(requestInfo);

        return userVo;
    }
}

// use UserControllerProxy
IUserController userController = new UserControllerProxy(new UserController());
```

On the other hand, if you don't have control to the real class and not able to modify the interface it implements, then we can do it using inheritance
```java
public class UserControllerProxy extends UserController {
  private MetricsCollector metricsCollector;

  public UserControllerProxy() {
    this.metricsCollector = new MetricsCollector();
  }

  public UserVo login(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    UserVo userVo = super.login(telephone, password);

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("login", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    return userVo;
  }

  public UserVo register(String telephone, String password) {
    long startTimestamp = System.currentTimeMillis();

    UserVo userVo = super.register(telephone, password);

    long endTimeStamp = System.currentTimeMillis();
    long responseTime = endTimeStamp - startTimestamp;
    RequestInfo requestInfo = new RequestInfo("register", responseTime, startTimestamp);
    metricsCollector.recordRequest(requestInfo);

    return userVo;
  }
}
// use UserControllerProxy
UserController userController = new UserControllerProxy();
```

### Dynamic proxy
There are some cons in the above-mentioned solutions
- we need to implement all methods in the proxy class, which could be verbose
- what happens if we have more than one class need to proxy, do we implement the solution multiple times?

This is where dynamic proxy will help
```java
// use MetricsCollectorProxy
MetricsCollectorProxy proxy = new MetricsCollectorProxy();
IUserController userController = (IUserController) proxy.createProxy(new UserController());
```

### When to use proxy pattern
- some non-business functionalities in the app, for example, idempotency, log, rate limit, authorization, etc.
- RPC, cache, RPC can be seen as a proxy as well
