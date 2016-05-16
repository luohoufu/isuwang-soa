package com.isuwang.scala.dbc.utils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * Created by tangliu on 2016/5/12.
 */
public class B {

    public void setValue(Optional<Double> value) {
        this.value = value;
    }

    Optional<Double> value;

    public Optional<Double> getValue() {
        return value;
    }

    public static void main(String[] args) throws NoSuchFieldException {

        Class<B> clazz = B.class;
        Field fld = clazz.getDeclaredField("value");
        Class<?> x = fld.getType();
        System.out.println("x = " + x.getName());

        Type genericType = fld.getGenericType();
        if(genericType instanceof ParameterizedType){
            ParameterizedType ptype = (ParameterizedType) genericType;
            Type oType = ptype.getOwnerType(); // == Optional.class
            ptype.getRawType(); // == Optional.class
            Type[] paraTypes = ptype.getActualTypeArguments();
            for(Type paraType: paraTypes) {
                System.out.println("para:" + paraType); // Double.class
            }
        }
        System.out.println("genericType = " + genericType.getClass());

    }
}
