package com.hand.document.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectUtils {

    public static String getFiledString(Object object, String id) {
        String value = "";
        try {
            Field field = object.getClass().getDeclaredField(id);
            field.setAccessible(true);
            value = (String) field.get(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static Object getMethod(Object object, String method) {
        String value = "";
        try {
            Method targetMethod = object.getClass().getDeclaredMethod(method, null);
            targetMethod.setAccessible(true);
            return targetMethod.invoke(object, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }


    public static int getFiledInteger(Object object, String id) {
        int value = 0;
        try {
            Field field = null;
            field = object.getClass().getDeclaredField(id);
            field.setAccessible(true);
            value = field.getInt(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean getFiledBoolean(Object object, String id) {
        boolean value = false;
        try {
            Field field = object.getClass().getDeclaredField(id);
            field.setAccessible(true);
            value = field.getBoolean(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static long getFiledLong(Object object, String id) {
        long value = 0l;
        try {
            Field field = object.getClass().getDeclaredField(id);
            field.setAccessible(true);
            value = field.getLong(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
