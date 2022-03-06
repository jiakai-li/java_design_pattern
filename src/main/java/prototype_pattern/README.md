### Prototype pattern
We can use prototype pattern to create new object, if:
- it's expensive to create an object
- objects of the same class not different too much from each other

### What does it mean be "expensive to create an object"?
When create an object, there is a whole process of memory allocation, object initialization, etc. But this is negligible and is not a reason of using prototype pattern.

However, if the data required need complicated computation, e.g. hash, sort, etc., or is retrieved from slow IOs, e.g. RPC, network, database, file system, etc., then the prototype pattern might be useful.

### Example
Given below scenario:
- We have 100,000 "key word" information in the database
- Each "key word" information contains
  - key word itself
  - the count of how many times this key word has been searched agains
  - last update timestamp
  - etc
- The system A loads this information into memory during startup to provide service. We can build a hash map for this information

As data changed dynamically, we could update system A's data using below logic:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Demo {
  private ConcurrentHashMap<String, SearchWord> currentKeywords = new ConcurrentHashMap<String, SearchWord>();
  private long lastUpdateTime = -1;

  public void refresh() {
    // read from database the changed data and update currentKeywords
    List<SearchWord> toBeUpdatedSearchWords = getSearchWords(lastUpdateTime);
    long maxNewUpdatedTime = lastUpdateTime;
    for (SearchWord searchWord : toBeUpdatedSearchWords) {
      if (searchWord.getLastUpdateTime() > maxNewUpdatedTime) {
        maxNewUpdatedTime = searchWord.getLastUpdateTime();
      }
      if (currentKeywords.containsKey(searchWord.getKeyWord())) {
        currentKeywords.replace(searchWord.getKeyWord(), searchWord);
      } else {
        currentKeywords.put(searchWord.getKeyWord(), searchWord);
      }
    }

    lastUpdateTime = maxNewUpdatedTime;
  }

  private List<SearchWord> getSearchWords(long lastUpdateTime) {
    // retrieve search words from database based on the latest update timestamp of system A
    return new ArrayList<SearchWord>();
  }
}
```

What happens if we put two more requirements for the update:
- Update of data in system A needs to be atomic
- System A need to be available during data update

We can use below logic:

```java
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class Demo {
  private HashMap<String, SearchWord> currentKeywords = new HashMap<>();

  public void refresh() {
    HashMap<String, SearchWord> newKeywords = new LinkedHashMap<>();

    // update newKeywords
    List<SearchWord> toBeUpdatedSearchWords = getSearchWords();
    for (SearchWord searchWord : toBeUpdatedSearchWords) {
        newKeywords.put(searchWord.getKeyword(), searchWord);
    }
    
    currentKeywords = newKeywords;
  }

  private List<SearchWord> getSearchWords() {
    // retrieve ALL search words from database
    return new ArrayList<SearchWord>();
  }
}
```

As we can imagine, the `newKeywords` is expensive to create, and this is when prototype pattern could be used

```java
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Demo {
  private HashMap<String, SearchWord> currentKeywords = new HashMap<String, SearchWord>();
  private long lastUpdateTime = -1;

  public void refresh() {
    // prototype pattern
    HashMap<String, SearchWord> newKeywords = currentKeywords.clone();
    
    // read from database the changed data and update currentKeywords
    List < SearchWord > toBeUpdatedSearchWords = getSearchWords(lastUpdateTime);
    long maxNewUpdatedTime = lastUpdateTime;
    for (SearchWord searchWord : toBeUpdatedSearchWords) {
      if (searchWord.getLastUpdateTime() > maxNewUpdatedTime) {
        maxNewUpdatedTime = searchWord.getLastUpdateTime();
      }
      if (newKeywords.containsKey(searchWord.getKeyWord())) {
        newKeywords.replace(searchWord.getKeyWord(), searchWord);
      } else {
        newKeywords.put(searchWord.getKeyWord(), searchWord);
      }
    }

    lastUpdateTime = maxNewUpdatedTime;
    currentKeywords = newKeywords;
  }

  private List<SearchWord> getSearchWords(long lastUpdateTime) {
    // retrieve search words from database based on the latest update timestamp of system A
    return new ArrayList<SearchWord>();
  }
}
```

### Deep copy and shallow copy
In java, Object clone() is a shallow copy, which means it copies primitive type and memory address of reference type but won't recursively copy the reference object itself. As a result, the update of `newKeywords` will affect the data inside `currentKeywords` and break the atomic requirement. We should use deep copy instead (through serialization):
```java
public Object deepCopy(Object object) {
    ByteArrayOutputStream bo = new ByteArrayOutputStream();
    ObjectOutputStream oo = new ObjectOutputStream(bo);
    oo.writeObject(object);
    
    ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
    ObjectInputStream oi = new ObjectInputStream(bi);
    
    return oi.readObject();
}
```

BUT, this is, again, expensive. Do we really need to deep copy everything? Probably not

```java

import java.util.ArrayList;

public class Demo {
  private HashMap<String, SearchWord> currentKeywords = new HashMap<>();
  private long lastUpdateTime = -1;

  public void refresh() {
    // Shallow copy first
    HashMap<String, SearchWord> newKeywords = (HashMap<String, SearchWord>) currentKeywords.clone();

    // read from database the changed data and update currentKeywords
    List<SearchWord> toBeUpdatedSearchWords = getSearchWords(lastUpdateTime);
    long maxNewUpdatedTime = lastUpdateTime;
    for (SearchWord searchWord : toBeUpdatedSearchWords) {
      if (searchWord.getLastUpdateTime() > maxNewUpdatedTime) {
        maxNewUpdatedTime = searchWord.getLastUpdateTime();
      }
      if (newKeywords.containsKey(searchWord.getKeyword())) {
        // newKeywords remove the item first
        newKeywords.remove(searchWord.getKeyword());
      }
      // newKeywords put the new item
      newKeywords.put(searchWord.getKeyword(), searchWord);
    }

    lastUpdateTime = maxNewUpdatedTime;
    currentKeywords = newKeywords;
  }

  private List<SearchWord> getSearchWords(long lastUpdateTime) {
    // retrieve search words from database based on the latest update timestamp of system A
    return new ArrayList<SearchWord>();
  }
}
```

This way, we could clone the expensive calculated hash map efficiently using prototype pattern. We don't always need to use deep copy. If the target itself is immutable, it might be safe to use shallow copy, otherwise if the target is mutable, then the data consistency might be violated during the data update, shallow copy would of course be more performant if it is suitable.
