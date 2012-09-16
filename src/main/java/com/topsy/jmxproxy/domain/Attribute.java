package com.topsy.jmxproxy.domain;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.openmbean.CompositeData;

//import net.sf.json.JSONArray;
//import net.sf.json.JSONObject;
//import net.sf.json.JSONSerializer;

public class Attribute {
    private Object value = null;

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    /*
    public String toJSONString() {
        return Attribute.toJSON(this.value).toString();
    }

    public static String toJSONString(Object value) {
        return Attribute.toJSON(value).toString();
    }

    private static Object toJSON(Object value) {
        if (value == null) {
            return "null";
        } else if (value.getClass().isArray()) {
            List list = new ArrayList();
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                list.add(toJSON(Array.get(value, i)));
            }
            return (JSONArray)JSONSerializer.toJSON(list);
        } else if (Collection.class.isAssignableFrom(value.getClass())) {
            List list = new ArrayList();
            for (Object obj : ((Collection<?>)value)) {
                list.add(toJSON(obj));
            }
            return (JSONArray)JSONSerializer.toJSON(list);
        } else if (Map.class.isAssignableFrom(value.getClass())) {
            Map map = new HashMap();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>)value).entrySet()) {
                map.put(toJSON(entry.getKey()), toJSON(entry.getValue()));
            }
            return (JSONObject)JSONSerializer.toJSON(map);
        } else if (CompositeData.class.isAssignableFrom(value.getClass())) {
            Map map = new HashMap();
            CompositeData data = (CompositeData)value;
            for (Object key : data.getCompositeType().keySet()) {
                map.put((String)key, toJSON(data.get((String)key)));
            }
            return (JSONObject)JSONSerializer.toJSON(map);
        } else {
            return value;
        }
    }
    */
}
