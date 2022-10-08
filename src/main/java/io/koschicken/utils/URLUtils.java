package io.koschicken.utils;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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

    public static String pageDescription(String url) throws IOException {
        Element head = Jsoup.connect(url).get().head();
        String title = head.getElementsByTag("title").text().trim();
        String description = "";
        Elements meta = head.getElementsByTag("meta");
        Optional<Element> element = meta.stream().filter(e -> Objects.equals(e.attr("name"), "description")).findFirst();
        if (element.isPresent()) {
            description += element.get().attr("content").trim();
        }
        return title + "\n" + description;
    }

    public static void main(String[] args) throws IOException {
        String url = "https://bbs.saraba1st.com/2b/thread-2098295-1-1.html";
        log.info(pageDescription(url));
    }
}
