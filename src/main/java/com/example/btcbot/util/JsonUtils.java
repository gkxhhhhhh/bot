package com.example.btcbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON 工具类
 */
public class JsonUtils {

    private static final Gson GSON = new GsonBuilder().create();

    /**
     * 把对象转成 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        return GSON.toJson(obj);
    }


    /**
     * 把 JSON 字符串转成对象
     *
     * @param json  JSON 字符串
     * @param clazz 目标类型
     * @param <T>   泛型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty() || clazz == null) {
            return null;
        }
        return GSON.fromJson(json, clazz);
    }
}