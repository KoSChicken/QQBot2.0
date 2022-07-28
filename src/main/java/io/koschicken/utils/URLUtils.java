package io.koschicken.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {

    public static Map<String, String> getQueryMap(String query) {
        if (Objects.nonNull(query)) {
            String[] params = query.split("&");
            Map<String, String> map = new HashMap<>();

            for (String param : params) {
                String name = param.split("=")[0];
                String value = param.split("=")[1];
                map.put(name, value);
            }
            return map;
        }
        return new HashMap<>();
    }

    public static Float string2Float(String str) {
        Pattern pattern = Pattern.compile("([1-9]\\d*\\.?\\d*)|(0\\.\\d*[1-9])");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String group = matcher.group();
            return Float.parseFloat(group);
        }
        return null;
    }

    public static void main(String[] args) {
        Float aFloat = string2Float("qweqa@张三");
        System.out.println(aFloat);
    }
}
