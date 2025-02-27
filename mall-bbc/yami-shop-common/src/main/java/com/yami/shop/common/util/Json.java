/*
 * Copyright (c) 2018-2999 广州市蓝海创新科技有限公司 All rights reserved.
 *
 * https://www.mall4j.com/
 *
 * 未经允许，不可做商业用途！
 *
 * 版权所有，侵权必究！
 */
package com.yami.shop.common.util;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * json 工具类
 * @author yami
 */
@Slf4j
public class Json {

    private static ObjectMapper objectMapper = new ObjectMapper();
    static {
        // 是否缩放排列输出，默认false，有些场合为了便于排版阅读则需要对输出做缩放排列
//        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 序列化日期时以timestamps输出，默认true
        // objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 序列化枚举是以toString()来输出，默认false，即默认以name()来输出     如：ENUM01("enum_01") 默认为 ENUM01，true则为enum_01
        // objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        // 序列化枚举是以ordinal()来输出，默认false   如：ENUM01("enum_01") 默认为 ENUM01，true则为 0 非常危险
        // objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        // 序列化Map时对key进行排序操作，默认false
        // objectMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        // 序列化char[]时以json数组输出，默认false
        //objectMapper.enable(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS);
        //序列化BigDecimal时之间输出原始数字还是科学计数，默认false，即是否以toPlainString()科学计数方式来输出
        // 1E+20 -> 100000000000000000000
//        objectMapper.enable(Feature.WRITE_BIGDECIMAL_AS_PLAIN);

        // 如果为空则不输出
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 对于空的对象转json的时候不抛出错误
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 禁用序列化日期为timestamps
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 禁用遇到未知属性抛出异常
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 视空字符传为null
//        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 低层级配置
//        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // 允许属性名称没有引号
//        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        // 允许单引号
//        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // 取消对非ASCII字符的转码
        objectMapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
        // 取消过时objectMapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false)
        objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 对象转json
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("对象转json错误:", e);
        }
        return null;
    }

    /**
     * json转对象
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null) {
            return null;
        }
        T result = null;
        try {
            result = objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("json转对象错误:", e);
        }
        return result;
    }

    /**
     * json转换换成对象
     * @param src src
     * @param clazz clazz
     * @return Class
     */
    public static <T> T parseObject(byte[] src, Class<T> clazz) {
        T result = null;
        try {
            result = objectMapper.readValue(src, clazz);
        }
        catch (Exception e) {
            e.printStackTrace();
            log.error("parseObject() error: {}", e.getMessage());
        }
        return result;
    }

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }


    /**
     *      * https://stackoverflow.com/questions/6349421/how-to-use-jackson-to-deserialise-an-array-of-objects
     *      * List<MyClass> myObjects = Arrays.asList(mapper.readValue(json, MyClass[].class))
     *      * works up to 10 time faster than TypeRefence.
     * @return
     */
    public static <T> List<T> parseArray(String json, Class<T[]> clazz){
        if (json == null) {
            return null;
        }
        T[] result = null;
        try {
            result = objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        if (result == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(result);
    }


    /**
     * 转换成json节点，即map
     * @param jsonStr
     * @return
     */
    public static JsonNode parseJson(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(jsonStr);
        } catch (Exception e) {
            log.error("Exception:", e);
        }
        return jsonNode;
    }

//    public static void main(String[] args){
//        String arr = "[1.01,1.03,1.23]";
//
//        List<Double> doubles = parseArray(arr, Double[].class);
//        for (Double aDouble : doubles) {
//            System.out.println(aDouble);
//        }
//
//    }
}
