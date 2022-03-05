### 为什么需要建造者模式
如果一个类中有很多属性，为了避免构造函数的参数列表过长，影响代码的可读性和易用性，我们可以通过构造函数配合set方法来解决，但是如果存在以下情况中的任意一种，我们就要考虑使用建造者模式了
- 我们把类的必填项放到构造函数中，强制创建对象的时候就设置。如果必填的属性有很多，把这些必填属性都放到构造函数中设置，那构造函数就又会出现参数列表过长的问题。如果把必填属性通过set方法设置，那校验这些必填属性是否已经填写的逻辑就无处安放了
- 如果类的属性之间有一定的依赖关系或者约束条件，我们继续使用构造函数配合set方法的设计思路，那这些依赖关系或约束条件的校验逻辑就无处安放了
- 如果我们希望创建不可变对象，也就死说在对象创建好之后，就不能再修改内部的属性值，要实现这个功能我们就不能在类中暴露set方法，这样构造函数配合set方法来设置属性值的方式就无法使用了

### 创建者模式
可以把对象创建的校验逻辑提取到一个Builder类中，通过Builder类的set方法设置对象创建的各种配置参数，然后在使用build方法真正创建对象之前，做集中的校验，校验通过之后才会创建对象。除此之外，还需要把真正的对象构造函数改为private私有权限。这样我们就只能通过建造者来创建类对象了，并且类没有提供任何set方法，这样创建出来的对象就是不可变对象了

代码示例
```java
public class ResourcePoolConfig {
    private String name;
    private int maxTotal;
    private int maxIdle;
    private int minIdle;
    
    private ResourcePoolConfig(Builder builder) {
        this.name = builder.name;
        this.maxTotal = builder.maxTotal;
        this.maxIdle = builder.maxIdle;
        this.minIdle = builder.minIdle;
    }
    
    // 省略getter方法 ...
    
    public static class Builder {
        private static final int DEFAULT_MAX_TOTAL = 8;
        private static final int DEFAULT_MAX_IDLE = 8;
        private static final int DEFAULT_MIN_IDLE = 0;
        
        private String name;
        private int maxTotal = DEFAULT_MAX_TOTAL;
        private int maxIdle = DEFAULT_MAX_IDLE;
        private int minIdle = DEFAULT_MIN_IDLE;
        
        public ResourcePoolConfig build() {
            // 校验逻辑放到这里来做，包括必填项，依赖关系，约束等
            if (StringUtils.isBlank(name)) {
                throw new IllegalArgumentException("...");
            }
            if (maxIdle > maxTotal) {
                throw new IllegalArgumentException("...");
            }
            if (minIdle > maxTotal || minIdle > maxIdle) {
                throw new IllegalArgumentException("...");
            }
            
            return new ResourcePoolConfig(this);  // 这里传入的是this对象
        }
        
        // 下面是所有的set方法
        public Builder setName(String name) {
            if (StringUtils.isBalank(name)) {
                throw new IllegalArgumentException("...");
            }
            this.name = name;
            return this;
        }
        
        public Builder setMaxTotal(int maxTotal) {
            if (maxTotal <= 0) {
                throw new IllegalArgumentException("...");
            }
            this.maxTotal = maxTotal;
            return this;
        }
        
        public Builder setMaxIdle(int maxIdle) {
            if (maxIdle <0) {
                throw new IllegalArgumentException("...");
            }
            this.maxIdle = maxIdle;
            return this;
        }
        
        public Builder setMinIdle(int minIdle) {
            if (minIdle < 0) {
                throw new IllegalArgumentException("...");
            }
            this.minIdle = minIdle;
            return this;
        }
    }
}
```

建造者模式还可以避免对象处于一种中间的无效状态，譬如
```java
public class Rectangle {
    private int width;
    private int height;
    
    public void setWidth(int width) { this.width = width; }
    public void setHeight(int height) { this.height = height; }
}

public class Driver {
    public static void main(String[] args) {
        Rectangle rectangle = new Rectangle();  // rectangle invalid
        rectangle.setWidth(2);  // rectangle invalid
        rectangle.setWidth(3);  // rectangle valie
    }
}
```

### 与工厂模式的区别
- 工厂模式用来创建不同但是相关类型的对象，由给定的参数来决定创建具体那种类型的对象
- 建造者模式用来创建一种类型的复杂对象，通过设置不同的可选参数，"定制化"地创建不同的对象
