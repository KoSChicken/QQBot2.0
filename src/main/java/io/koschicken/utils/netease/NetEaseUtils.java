package io.koschicken.utils.netease;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.netease.Album;
import io.koschicken.bean.netease.Artist;
import io.koschicken.bean.netease.NetEaseMusic;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class NetEaseUtils {

    public static NetEaseMusic JSON2Song(String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        Integer code = jsonObject.getInteger("code");
        if (code != 200) {
            return null;
        }
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray songs = result.getJSONArray("songs");
        NetEaseMusic netEaseMusic = songs.getObject(0, NetEaseMusic.class);
        JSONObject songJsonObject = songs.getJSONObject(0);
        JSONArray artists = songJsonObject.getJSONArray("ar");
        List<Artist> artistList = new ArrayList<>();
        for (int i = 0; i < artists.size(); i++) {
            artistList.add(artists.getObject(i, Artist.class));
        }
        netEaseMusic.setArtists(artistList);
        netEaseMusic.setAlbum(songJsonObject.getObject("al", Album.class));
        return netEaseMusic;
    }


    public String source(String id) {
        return String.format("http://music.163.com/song/media/outer/url?id=%s.mp3", id);
    }

    public static void main(String[] args) throws Exception {
//        String json = FileUtils.readFileToString(new File("D://1.txt"), StandardCharsets.UTF_8);
//        Song song = JSON2Song(json);
//        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
//        String music = catCodeUtil.toCat("music", "kind=neteaseCloud",
//                "title=" + song.getName(), "jumpUrl=" + song.getUrl(),
//                "summary=" + song.getArtists().get(0).getName());
//        System.out.println(music);
        NetEaseMusic netEaseMusic = NetEaseMusic.searchWithoutLink("Don't Deal with the Devil cuphead", 1, 1);
        System.out.println(netEaseMusic);
    }
}
