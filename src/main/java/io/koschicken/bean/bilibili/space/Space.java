package io.koschicken.bean.bilibili.space;

import cn.hutool.core.net.URLEncodeUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.utils.HttpUtils;
import io.koschicken.utils.bilibili.BilibiliUtils;
import io.koschicken.utils.bilibili.WbiUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static io.koschicken.constants.Constants.commonConfig;
import static org.springframework.util.ResourceUtils.isUrl;

@Slf4j
@Data
public class Space {

    private static final String LIVE_TEMP_FOLDER = "./temp/bilibili/live/";

    /**
     * 用户ID
     */
    private String mid;
    /**
     * 昵称
     */
    private String name;
    /**
     * 性别
     */
    private String sex;
    /**
     * 头像地址
     */
    private String face;
    /**
     * 签名
     */
    private String sign;
    private Integer rank;
    /**
     * 等级 0-6
     */
    private Integer level;
    /**
     * 注册时间
     */
    private Long joinTime;
    private Long moral;
    /**
     * 封禁状态 0-正常 1-封禁
     */
    private Integer silence;
    /**
     * 硬币数 需要登录 默认为0
     */
    private Integer coins;
    /**
     * 是否具有粉丝勋章
     */
    private Boolean fansBadge;
    /**
     * 直播间信息
     */
    private LiveRoom liveRoom;

    public static Space getSpace(String mid) throws IOException, HttpException {
        String url = "https://api.bilibili.com/x/space/wbi/acc/info?mid=" + mid + "&token=&platform=web&" + generateWrid();
        String json = HttpUtils.get(url, commonConfig.getBilibiliCookie());
        JSONObject jsonObject = JSON.parseObject(json);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {
            String data = jsonObject.getString("data");
            if (data != null) {
                return buildSpace(data);
            }
        }
        throw new HttpException("获取用户信息失败。url: " + url);
    }

    private static String generateWrid() throws IOException {
        JSONObject wbiImg = navWbiImg();
        String imgUrl = wbiImg.getString("img_url");
        String imgKey = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);
        String subUrl = wbiImg.getString("sub_url");
        String subKey = subUrl.substring(subUrl.lastIndexOf("/") + 1);
        String mixinKey = WbiUtils.getMixinKey(imgKey, subKey);
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("foo", "one one four");
        map.put("bar", "五一四");
        map.put("baz", 1919810);
        map.put("wts", System.currentTimeMillis() / 1000);
        StringJoiner param = new StringJoiner("&");
        //排序 + 拼接字符串
        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> param.add(entry.getKey() + "=" + URLEncodeUtil.encode(entry.getValue().toString())));
        String s = param + mixinKey;
        String wbiSign = SecureUtil.md5(s);
        return param + "&w_rid=" + wbiSign;
    }

    private static JSONObject navWbiImg() throws IOException {
        String url = "https://api.bilibili.com/x/web-interface/nav";
        String json = HttpUtils.get(url, commonConfig.getBilibiliCookie());
        JSONObject jsonObject = JSON.parseObject(json);
        return jsonObject.getJSONObject("data").getJSONObject("wbi_img");
    }

    private static Space buildSpace(String data) throws IOException {
        Space space = JSON.parseObject(data, Space.class);
        if (Objects.nonNull(space)) {
            LiveRoom liveRoom = space.getLiveRoom();
            if (Objects.nonNull(liveRoom)) {
                String coverUrl = liveRoom.getCover().replace("http:", "https:");
                boolean isUrl = isUrl(coverUrl);
                if (isUrl) {
                    URL imageUrl = new URL(coverUrl);
                    String fileName = BilibiliUtils.getImageName(imageUrl);
                    File cover = new File(LIVE_TEMP_FOLDER + fileName);
                    FileUtils.deleteQuietly(cover);
                    FileUtils.touch(cover);
                    FileUtils.copyURLToFile(imageUrl, cover);
                    liveRoom.setCoverFile(cover);
                    space.setLiveRoom(liveRoom);
                }
            }
        }
        return space;
    }
}
