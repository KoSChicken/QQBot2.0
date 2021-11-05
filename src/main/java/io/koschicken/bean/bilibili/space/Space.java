package io.koschicken.bean.bilibili.space;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.utils.HttpUtils;
import io.koschicken.utils.bilibili.BilibiliUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static org.springframework.util.ResourceUtils.isUrl;

@Slf4j
@Data
public class Space {

    private static final String LIVE_TEMP_FOLDER = "./temp/bilibili/live/";

    static {
        File liveFolder = new File(LIVE_TEMP_FOLDER);
        if (!liveFolder.exists()) {
            try {
                FileUtils.forceMkdir(liveFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    public static Space getSpace(String mid) throws IOException {
        String url = "http://api.bilibili.com/x/space/acc/info?mid=" + mid;
        String json = HttpUtils.get(url, COMMON_CONFIG.getBilibiliCookie());
        JSONObject jsonObject = JSON.parseObject(json);
        String data = jsonObject.getString("data");
        if (data != null) {
            Space space = JSON.parseObject(data, Space.class);
            LiveRoom liveRoom = space.getLiveRoom();
            String coverUrl = liveRoom.getCover();
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
            return space;
        }
        return null;
    }
}
