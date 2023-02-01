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
import java.time.LocalDateTime;
import java.util.Objects;

import static io.koschicken.constants.Constants.COMMON_CONFIG;
import static org.springframework.util.ResourceUtils.isUrl;

@Slf4j
@Data
public class Space {

    private static final String LIVE_TEMP_FOLDER = "./temp/bilibili/live/";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36 Edg/108.0.1462.76";
    private static LocalDateTime next = null;

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
        String url = "https://api.bilibili.com/x/space/acc/info?mid=" + mid;
        if (next != null && LocalDateTime.now().isBefore(next)) {
            return null;
        }
        String json = HttpUtils.get(url, COMMON_CONFIG.getBilibiliCookie());
        JSONObject jsonObject = JSON.parseObject(json);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {
            String data = jsonObject.getString("data");
            if (data != null) {
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
        } else {
            next = LocalDateTime.now().plusMinutes(15);
            String message = jsonObject.getString("message");
            log.error("获取用户信息失败，mid={}，message={}", mid, message);
        }
        return null;
    }
}
