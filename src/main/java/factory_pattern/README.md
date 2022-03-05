### 工厂模式的细分类型
- 简单工厂
- 工厂方法
- 抽象工厂

### 简单工厂
举例：根据文件的后缀选择不同的解析器，将存储在文件中的配置解析成内存对象

```java
import java.io.InvalidClassException;

public class RuleConfigSource {
    public RuleConfig load(String ruleConfiguFilePath) {
        String fileExtension = getFileExtension(ruleConfiguFilePath);
        IRuleConfigParser parser = null;
        if ("json".equalsIgnoreCase(fileExtension)) {
            parser = new JsonRuleConfigParser();
        } else {
            throw new InvalidClassException(
                    "Rule config file format is not supported"
            );
        }

        String configText = "";
        return parser.parse(configText);
    }

    private String getFileExtension(String filePath) {
        return "json";
    }
}

public class RuleConfig {
}

public interface IRuleConfigParser {
    RuleConfig parse(String configText);
}

public class JsonRuleConfigParser implements IRuleConfigParser {
    public RuleConfig parse(String configText) {
        return new RuleConfig();
    }
}
```

上面这段代码创建parser的部分可以独立剥离出来，抽象成createParser函数，同时为了让类的职责更加单一，代码更加清晰，还可以进一步把createParser函数剥离到一个独立的类中，让这个类只负责对象的创建，这个类就是简单工厂模式

```java
import java.io.InvalidClassException;

public class RuleConfigSource {
    public RuleConfig load(String ruleConfigFilePath) {
        String fileExtension = getFileExtension(ruleConfigFilePath);
        IRuleConfigParser parser = RuleConfigParserFactory.createParser(fileExtension);
        if (parser == null) {
            throw new InvalidClassException(
                    "Rule config file format is not supported"
            );
        }
        
        String configText = "";
        return parser.parse(configText);
    }
}

public class RuleConfigParserFactory {
    public static IRuleConfigParser createParser(String configFormat) {
        IRuleConfigParser parser = null;
        if ("json".equalsIgnoreCase(configFormat)) {
            parser = new JsonRuleConfigParser();
        } else if ("xml".equalsIgnoreCase(configFormat)) {
            parser = new XmlRuleConfigParser();
        }
        
        return parser;
    }
}
```
实际上，如果parser可以复用的话也可以将parser缓存起来直接使用，避免每次都创建新的对象

```java
import java.util.HashMap;
import java.util.Map;

public class RuleConfigParserFactory {
    private static final Map<String, RuleConfigParser> cachedParser = new HashMap<>();

    static {
        cachedParser.put("json", new JsonRuleConfigParser());
        cachedParser.put("xml", new XmlRuleConfigParser());
    }

    public static IRuleConfigParser createParser(String configFormat) {
        if (configFormat == null || configFormat.isEmpty()) {
            return null;
        }
        return cachedParser.get(configFormat.toLowerCase());
    }
}
```
对于以上两种简单工厂模式的实现方法，如果需要添加新的parser，势必要改动到RuleConfigParserFactory的代码，尽管这种实现有多处if分之判断逻辑，违背开闭原则，但权衡扩展性和可读性，这样的代码实现在大多数情况下（譬如不需要频繁添加parser，也没有太多parser）也是没有问题的

### 工厂方法
如果我们要将if分之逻辑去掉的话，那么可以利用多态进行重构
```java
public interface IRuleConfigParserFactory {
    IRuleConfigParserFactory createParser();
}

public class JsonRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParserFactory createParser() {
        return new JsonRuleConfigParserFactory();
    }
}

public class XmlRuleConfigParserFactory implements IRuleConfigParserFactory {
    @Override
    public IRuleConfigParserFactory createParser() {
        return new XmlRuleConfigParserFactory();
    }
}
```
这样当我们新增一种parser的时候，只需要新增一个实现了IRuleConfigParserFactory接口的Factory类即可，所以工厂方法比简单工厂更加符合开闭原则。但是我们还需要为工厂类再创建一个简单工厂，用来创建工厂类对象
```java

public class RuleConfigSource {
  public RuleConfig load(String ruleConfigFilePath) {
    String ruleConfigFileExtension = getFileExtension(ruleConfigFilePath);

    IRuleConfigParserFactory parserFactory = RuleConfigParserFactoryMap.getParserFactory(ruleConfigFileExtension);
    if (parserFactory == null) {
      throw new InvalidRuleConfigException("Rule config file format is not supported: " + ruleConfigFilePath);
    }
    IRuleConfigParser parser = parserFactory.createParser();

    String configText = "";
    //从ruleConfigFilePath文件中读取配置文本到configText中
    RuleConfig ruleConfig = parser.parse(configText);
    return ruleConfig;
  }

  private String getFileExtension(String filePath) {
    //...解析文件名获取扩展名，比如rule.json，返回json
    return "json";
  }
}

//因为工厂类只包含方法，不包含成员变量，完全可以复用，
//不需要每次都创建新的工厂类对象，所以，简单工厂模式的第二种实现思路更加合适。
public class RuleConfigParserFactoryMap { //工厂的工厂
  private static final Map<String, IRuleConfigParserFactory> cachedFactories = new HashMap<>();

  static {
    cachedFactories.put("json", new JsonRuleConfigParserFactory());
    cachedFactories.put("xml", new XmlRuleConfigParserFactory());
    cachedFactories.put("yaml", new YamlRuleConfigParserFactory());
    cachedFactories.put("properties", new PropertiesRuleConfigParserFactory());
  }

  public static IRuleConfigParserFactory getParserFactory(String type) {
    if (type == null || type.isEmpty()) {
      return null;
    }
    IRuleConfigParserFactory parserFactory = cachedFactories.get(type.toLowerCase());
    return parserFactory;
  }
}
```

### 什么时候用工厂方法，什么时候用简单工厂
- 当创建对象的逻辑比较复杂，不只是简单的new一下就可以，而是要组合其他类对象，做各种初始化操作的时候就推荐使用工厂方法模式，将复杂的创建逻辑拆分到多个工厂类中，让每个工厂类都不至于过于复杂
- 另外在某些场景下，如果对象不可复用，那工厂类每次都要返回不同的对象，这时只能选择第一种包含if分支的实现方式，如果我们需要避免if-else分支，就需要使用工厂方法模式

### 抽象工厂
在上面的例子中，假设我们除了文件类型，还有另外一个唯独的分类，譬如Rule规则配置还是System系统配置，这样的话就会有2*n种搭配的parser类，这时就可以用到抽象工厂，回顾一下，抽象工厂是用来创建一系列相关的类的类的，这里RuleParser和SystemParser是相关的，都是根据文件格式来创建的，所以使用时就是通过json这个文件格式创建一个JsonConfigParserFactory对象，然后通过该对象创建一系列"相关的"产品，即RuleParser和SystemParser
```java

public interface IConfigParserFactory {
  IRuleConfigParser createRuleParser();
  ISystemConfigParser createSystemParser();
  //此处可以扩展新的parser类型，比如IBizConfigParser
}

public class JsonConfigParserFactory implements IConfigParserFactory {
  @Override
  public IRuleConfigParser createRuleParser() {
    return new JsonRuleConfigParser();
  }

  @Override
  public ISystemConfigParser createSystemParser() {
    return new JsonSystemConfigParser();
  }
}

public class XmlConfigParserFactory implements IConfigParserFactory {
  @Override
  public IRuleConfigParser createRuleParser() {
    return new XmlRuleConfigParser();
  }

  @Override
  public ISystemConfigParser createSystemParser() {
    return new XmlSystemConfigParser();
  }
}

// 省略YamlConfigParserFactory和PropertiesConfigParserFactory代码
```

### 要不要使用工厂模式
- 封装变化：创建逻辑有可能变化，封装成工厂类之后，创建逻辑的变更对调用者透明
- 代码复用：创建代码抽离到独立的工厂类之后可以复用
- 隔离复杂性：封装复杂的创建逻辑，调用者无需了解如何创建对象
- 控制复杂度：将创建代码抽离出来，让原本的函数或类职责更加单一，代码更简洁
