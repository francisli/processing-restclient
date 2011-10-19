package com.francisli.processing.http;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A JsonObject represents some data from a JSON response. You can query
 * the type of data and access its value(s) using the functions in this class.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation, version 3.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * @author Francis Li <mail@francisli.com>
 */
public class JSONObject {
    Object object;
    
    JSONObject(Object object) {
        //// if this is an array or dictionary, wrap its contents
        if (object instanceof ArrayList) {
            ArrayList array = (ArrayList)object;
            ArrayList wrapped = new ArrayList();
            for (Object element: array) {
                wrapped.add(new JSONObject(element));
            }
            object = wrapped;
        } else if (object instanceof HashMap) {
            HashMap map = (HashMap)object;
            for (Object key: map.keySet()) {
                map.put(key, new JSONObject(map.get(key)));
            }
        }
        this.object = object;
    }     
    
    public String toString() {
        if (object == null) {
            return "null";
        }
        return object.toString();
    }
    
    /**
     * Returns true if this JSONObject represents a null value.
     * 
     * @return true if this JSONObject represents a null value, false otherwise
     */
    public boolean isNull() {
        return object == null;
    }
    
    /**
     * Returns true if this JSONObject represents a Map whose values can
     * be retrieved by key.
     * 
     * @return true if this JSONObject represents a Map, false otherwise
     */
    public boolean isMap() {
        return object instanceof HashMap;
    }
    
    /**
     * Returns the JSONObject value for the specified key, given that this
     * JSONObject is a dictionary.
     * 
     * @param key The key you wish to retrieve the value for
     * @return A JSONObject representing the value
     */
    public JSONObject get(String key) {
        HashMap map = (HashMap)object;
        return (JSONObject)map.get(key);
    }
    
    /**
     * Returns true if this JSONObject represents a list whose elements can
     * be retrieved by index position.
     * 
     * @return true if this JSONObject represents a list, false otherwise
     */
    public boolean isList() {
        return object instanceof ArrayList;
    }
    
    /**
     * Returns the JSONObject element for the specified index, given that this
     * JSONObject represents a list.
     * 
     * @param i Index of the element you wish to retrieve
     * @return A JSONObject representing the element
     */
    public JSONObject get(int i) {
        ArrayList array = (ArrayList)object;
        return (JSONObject)array.get(i);
    }
    
    /**
     * Returns the number of JSONObject elements in this list, given that this
     * JSONObject represents a list.
     * 
     * @return size of this list
     */
    public int size() {
        ArrayList array = (ArrayList)object;
        return array.size();
    }
    
    /**
     * Returns true if this JSONObject represents a String, false otherwise.
     * 
     * @return true if this JSONObject represents a String, false otherwise.
     */
    public boolean isString() {
        return object instanceof String;
    }
    
    /**
     * Returns the value of this JSONObject as a String.
     * 
     * @return A String object
     */
    public String stringValue() {
        return (String) object;
    }
    
    /**
     * Returns true if this JSONObject represents a floating-point value, false 
     * otherwise.
     * 
     * @return true if this JSONObject represents a floating-point value, false otherwise.
     */
    public boolean isFloatingPoint() {
        return (object instanceof Double) || (object instanceof BigDecimal);
    }
    
    /**
     * Returns the value of this JSONObject as a float.
     * 
     * @return A float value
     */
    public float floatValue() {
        if (object instanceof Double) {
            return ((Double)object).floatValue();
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal)object).floatValue();
        }
        throw new RuntimeException("JSONObject: floatValue() requested on non-floating-point data");
    }
    
    /**
     * Returns the value of this JSONObject as a double.
     * 
     * @return A double value
     */
    public double doubleValue() {
        if (object instanceof Double) {
            return ((Double)object).doubleValue();
        } else if (object instanceof BigDecimal) {
            return ((BigDecimal)object).doubleValue();
        }
        throw new RuntimeException("JSONObject: floatValue() requested on non-floating-point data");
    }
    
    /**
     * Returns true if this JSONObject represents an integer value, false otherwise.
     * 
     * @return true if this JSONObject represents an integer value, false otherwise.
     */
    public boolean isInteger() {
        return (object instanceof Long) || (object instanceof BigInteger);
    }
    
    /**
     * Returns the value of this JSONObject as an int.
     * 
     * @return An int value
     */
    public int intValue() {
        if (object instanceof Long) {
            return ((Long)object).intValue();
        } else if (object instanceof BigInteger) {
            return ((BigInteger)object).intValue();
        }
        throw new RuntimeException("JSONObject: intValue() requested on non-integer data");
    }
    
    /**
     * Returns the value of this JSONObject as a long.
     * 
     * @return A long value
     */
    public long longValue() {
        if (object instanceof Long) {
            return ((Long)object).longValue();
        } else if (object instanceof BigInteger) {
            return ((BigInteger)object).longValue();
        }
        throw new RuntimeException("JSONObject: intValue() requested on non-integer data");
    }
    
    /**
     * Returns true if this JSONObject represents a boolean value, false otherwise.
     * 
     * @return true if this JSONObject represents a boolean value, false otherwise.
     */
    public boolean isBoolean() {
        return object instanceof Boolean;
    }
    
    /**
     * Returns the value of this JSONObject as a boolean.
     * 
     * @return A boolean value
     */
    public boolean booleanValue() {
        return ((Boolean)object).booleanValue();
    }
}
