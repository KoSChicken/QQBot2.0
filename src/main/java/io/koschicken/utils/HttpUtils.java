package io.koschicken.utils;

import org.apache.http.client.fluent.Request;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

public class HttpUtils {

    public static String get(String getUrl) throws IOException {
        return Request.Get(getUrl)
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
                .setHeader("Cookie", COMMON_CONFIG.getBilibiliCookie())
                .execute().returnContent().asString(StandardCharsets.UTF_8);
    }
}
