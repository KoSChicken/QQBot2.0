package io.koschicken.bean;

import java.util.Arrays;

public class Pixiv {
    private String code;
    private String msg;
    private String title;
    private String artwork;
    private String author;
    private String artist;
    private String[] tags;
    private String type;
    private String fileName;
    private String original;
    private Integer quota;
    private boolean r18;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtwork() {
        return artwork;
    }

    public void setArtwork(String artwork) {
        this.artwork = artwork;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }

    public Integer getQuota() {
        return quota;
    }

    public void setQuota(Integer quota) {
        this.quota = quota;
    }

    public boolean isR18() {
        return r18;
    }

    public void setR18(boolean r18) {
        this.r18 = r18;
    }

    @Override
    public String toString() {
        return "Pixiv{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                ", title='" + title + '\'' +
                ", artwork='" + artwork + '\'' +
                ", author='" + author + '\'' +
                ", artist='" + artist + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", type='" + type + '\'' +
                ", fileName='" + fileName + '\'' +
                ", original='" + original + '\'' +
                ", quota=" + quota +
                ", r18=" + r18 +
                '}';
    }
}
