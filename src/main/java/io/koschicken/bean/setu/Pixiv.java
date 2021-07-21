package io.koschicken.bean.setu;

import lombok.Data;

import java.util.Map;

@Data
public class Pixiv {
    private Integer pid;
    private Integer p;
    private Integer uid;
    private String title;
    private String author;
    private boolean r18;
    private Integer width;
    private Integer height;
    private String[] tags;
    private String ext;
    private Long uploadDate;
    private Map<String, String> urls;
}
