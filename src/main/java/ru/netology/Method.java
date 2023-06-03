package ru.netology;

import java.util.HashMap;
import java.util.Map;

public enum Method {
    GET,
    POST;

    private Map<String, Handler> map;

    Method(){
        map = new HashMap<>();
    }


    public Map<String, Handler> getMap() {
        return map;
    }

    public void addToMap(String path, Handler handler){
        map.put(path, handler);
    }

}
