package tools;

import java.util.HashMap;
import java.util.Set;

public class JsonTree {
    private HashMap<String, JsonTree> map = new HashMap<>();
    private String rawJSON;

    JsonTree(String str) {rawJSON = str;}

    public String retrieveValue() {
        if(map.isEmpty()) return rawJSON;
        else return null;
    }

    public String getRawJSON() { return rawJSON;}

    public JsonTree get(String str) {return map.get(str);}

    protected HashMap<String, JsonTree> getMap() {return this.map;}

    public Set<String> getKeys() {return map.keySet();}

    public void add(String key, JsonTree tree) {map.put(key, tree);}
}
