package io.koschicken.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.fluent.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

@Slf4j
public class HttpUtils {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0";

    private HttpUtils() {
    }

    public static String get(String getUrl) throws IOException {
        return Request.Get(getUrl)
                .setHeader("User-Agent", USER_AGENT)
                .execute().returnContent().asString(StandardCharsets.UTF_8);
    }

    public static String get(String getUrl, String cookies) throws IOException {
        return Request.Get(getUrl)
                .setHeader("User-Agent", USER_AGENT)
                .setHeader("Cookie", cookies)
                .execute().returnContent().asString(StandardCharsets.UTF_8);
    }

    public static String postContent(String url, Map<String, String> headers, String param) {
        return postContent(url, headers, param, null);
    }

    public static String postContent(String url, Map<String, String> headers, String param, List<HttpCookie> listCookie) {
        ByteArrayOutputStream out;
        InputStream in = null;
        try {
            HttpURLConnection conn = connect(headers, url, listCookie);
            // 设置参数
            conn.setDoOutput(true); // 需要输出
            conn.setDoInput(true); // 需要输入
            conn.setUseCaches(false); // 不允许缓存
            conn.setRequestMethod("POST"); // 设置POST方式连接
            conn.connect();

            // 建立输入流，向指向的URL传入参数
            OutputStream dos = (conn.getOutputStream());
            dos.write(param.getBytes());
            dos.flush();
            dos.close();

            String encoding = conn.getContentEncoding();
            in = conn.getInputStream();
            // 判断服务器返回的数据是否支持gzip压缩
            if (encoding != null && encoding.contains("gzip")) {
                in = new GZIPInputStream(conn.getInputStream());
            }
            byte[] buffer = new byte[2048];
            out = new ByteArrayOutputStream();
            int len = in.read(buffer);
            while (len > 0) {
                out.write(buffer, 0, len);
                len = in.read(buffer);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("发送GET请求出现异常！" + e);
            return "";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                log.error("", e2);
            }
        }
    }

    private static HttpURLConnection connect(Map<String, String> headers, String url, List<HttpCookie> listCookie)
            throws IOException {
        URL realUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        // 设置Cookie
        if (listCookie != null) {
            StringBuilder sb = new StringBuilder();
            for (HttpCookie cookie : listCookie) {
                sb.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
            }
            String cookie = sb.toString();
            if (cookie.endsWith("; ")) {
                cookie = cookie.substring(0, cookie.length() - 2);
            }
            conn.setRequestProperty("Cookie", cookie);
        }
        return conn;
    }
}
