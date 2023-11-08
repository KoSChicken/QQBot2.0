package io.koschicken.utils.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.bilibili.BiliUser;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.bean.bilibili.Video;
import io.koschicken.bot.Bilibili;
import io.koschicken.config.BilibiliConfig;
import io.koschicken.utils.HttpUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static io.koschicken.constants.Constants.CONFIG_DIR;

@Slf4j
public class BilibiliUtils {

    private BilibiliUtils() {
    }

    public static void bilibiliJSON() {
        File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
        // 群组设定
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            // 没有读取到配置文件
            try {
                FileUtils.touch(jsonFile);
            } catch (IOException e) {
                log.error("创建Bilibili配置文件失败", e);
            }
        } else {
            try {
                String jsonString = FileUtils.readFileToString(jsonFile, StandardCharsets.UTF_8);
                if (StringUtils.isNotEmpty(jsonString)) {
                    JSONObject jsonObject = JSON.parseObject(jsonString);
                    Set<String> keySet = jsonObject.keySet();
                    for (String key : keySet) {
                        String byGroup = jsonObject.getJSONArray(key).toJSONString();
                        List<Following> value = JSON.parseArray(byGroup, Following.class);
                        Bilibili.getInstance().put(key, value);
                    }
                }
            } catch (IOException | NullPointerException e) {
                log.error("读取Bilibili配置文件失败", e);
            }
        }
    }

    public static BiliUser searchByName(String name) throws IOException {
        String url = "https://api.bilibili.com/x/web-interface/search/type?search_type=bili_user&keyword=";
        String json = HttpUtils.get(url + name, BilibiliConfig.getInstance().getCookie());
        JSONObject jsonObject = JSON.parseObject(json);
        JSONObject data = jsonObject.getJSONObject("data");
        if (Objects.nonNull(data)) {
            JSONArray results = data.getJSONArray("result");
            if (!results.isEmpty()) {
                String user = results.getString(0);
                return JSON.parseObject(user, BiliUser.class);
            }
        }
        return null;
    }

    public static String getImageName(URL url) {
        String path = url.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static void main(String[] args) {
        try {
            String imageName = getImageName(new URL("http://i0.hdslb.com/bfs/live/new_room_cover/2b3955ab074e0d7fb9fcf849bb217de0b24c9d06.jpg"));
            log.info("{}", imageName);
        } catch (MalformedURLException e) {
            log.error("", e);
        }
        String url = "https://www.bilibili.com/video/BV1t34y1E7m3";
        String bv = url.substring(url.lastIndexOf("/") + 1);
        log.info("{}", bv);
        Video video = new Video(bv, true);
        log.info("{}", video);
        try {
            BiliUser user = searchByName("KoSChicken");
            log.info("{}", user);
        } catch (IOException e) {
            log.error("", e);
        }
    }
}
