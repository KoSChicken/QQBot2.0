package io.koschicken.bean.netease;

import com.alibaba.fastjson.JSONObject;
import io.koschicken.utils.HttpUtils;
import io.koschicken.utils.netease.NetEaseEncryptUtil;
import io.koschicken.utils.netease.NetEaseUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class NetEaseMusic {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 SE 2.X MetaSr 1.0";
    private static final String FAKE_CN_IP = "117.101.211.125";

    private Integer id;
    private String name;
    private List<Artist> artists;
    private Album album;
    private String url;
    private String musicUrl;

    public String getUrl() {
        return "https://music.163.com/#/song?id=" + id;
    }

    public String getMusicUrl() {
        return String.format("http://music.163.com/song/media/outer/url?id=%s.mp3", id);
    }

    public static NetEaseMusic searchWithoutLink(String keyWord, int pageSize, int pn) {
        String url = "https://music.163.com/weapi/cloudsearch/get/web?csrf_token=";
        HashMap<String, String> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        headers.put("referer", "https://music.163.com/search/");
        headers.put("accept", "*/*");
        headers.put("origin", "https://music.163.com");
        headers.put("accept-language", "zh-CN,zh;q=0.8");
        headers.put("X-Real-IP", FAKE_CN_IP);
        headers.put("X-Forwarded-For", FAKE_CN_IP);
        headers.put("user-agent", USER_AGENT);
        JSONObject obj = new JSONObject();
        obj.put("s", keyWord);
        obj.put("offset", pn - 1);
        obj.put("limit", pageSize);
        obj.put("type", "1"); // type 单曲1，歌手100，专辑10，MV1004，歌词1006，歌单1000，主播电台1009，用户1002
        obj.put("csrf_token", "");
        String params = NetEaseEncryptUtil.generateToken(obj.toString());
        String result = HttpUtils.postContent(url, headers, params);
        return NetEaseUtils.JSON2Song(result);
    }

    public static void main(String[] args) {
        NetEaseMusic music = searchWithoutLink("爬", 1, 1);
        System.out.printf(music.toString());
    }
}
