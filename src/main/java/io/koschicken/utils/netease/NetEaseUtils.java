package io.koschicken.utils.netease;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.netease.Album;
import io.koschicken.bean.netease.Artist;
import io.koschicken.bean.netease.NetEaseMusic;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class NetEaseUtils {

    private NetEaseUtils() {
    }

    public static NetEaseMusic JSON2Song(String json) {
        JSONObject jsonObject = JSONObject.parseObject(json);
        Integer code = jsonObject.getInteger("code");
        if (code != 200) {
            return null;
        }
        JSONObject result = jsonObject.getJSONObject("result");
        if (Objects.nonNull(result)) {
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
        } else {
            return null;
        }
    }
}
