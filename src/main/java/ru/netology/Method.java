package ru.netology;

import java.util.HashMap;
import java.util.Map;

public enum Method {
    GET,
    POST;

    private Map<String, Handler> mapHandler;

    Method(){
        mapHandler = new HashMap<>();
    }


    public Map<String, Handler> getMap() {
        return mapHandler;
    }

    public void addToMapHandler(String path, Handler handler){
        mapHandler.put(path, handler);
    }

}
