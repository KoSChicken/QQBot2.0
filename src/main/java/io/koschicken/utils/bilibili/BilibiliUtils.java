package io.koschicken.utils.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.bilibili.BiliUser;
import io.koschicken.bean.bilibili.Following;
import io.koschicken.utils.HttpUtils;
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

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.intercept.BotIntercept.GROUP_BILIBILI_MAP;

public class BilibiliUtils {

    private BilibiliUtils() {
    }

    public static void bilibiliJSON() {
        File jsonFile = new File(CONFIG_DIR + "/bilibili.json");
        //群组设定
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            //没有读取到配置文件
            try {
                FileUtils.touch(jsonFile);
            } catch (IOException e) {
                e.printStackTrace();
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
                        GROUP_BILIBILI_MAP.put(key, value);
                    }
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    public static BiliUser searchByName(String name) throws IOException {
        String url = "http://api.bilibili.com/x/web-interface/search/type?search_type=bili_user&keyword=";
        String json = HttpUtils.get(url + name, COMMON_CONFIG.getBilibiliCookie());
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

    public static void main(String[] args) throws MalformedURLException {
        String url = "http://i0.hdslb.com/bfs/live/new_room_cover/2b3955ab074e0d7fb9fcf849bb217de0b24c9d06.jpg";
        String imageName = getImageName(new URL(url));
        System.out.println(imageName);
    }
}
