package io.koschicken.bean.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.utils.HttpUtils;
import lombok.Data;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.ResourceUtils.isUrl;

@Data
public class Live {

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

    //主播uid
    private final String mid;
    private int roomStatus;
    private int roundStatus;
    private int liveStatus;
    private String url;
    private String title;
    private File cover;
    private int online;
    private int roomId;
    private User user;

    public Live(String mid) throws IOException {
        this.mid = mid;
        fresh();
    }

    /**
     * 通过传入的up主uid返回一个json数组，其中包含直播间房间号，封面，人气，标题
     *
     * @param uid up的uid
     * @return json 示例如下
     * {"code":0,"message":"0","ttl":1,
     * "data":{
     * "roomStatus":1,   //0：无房间 1：有房间
     * "roundStatus":0,   //0：未轮播 1：轮播
     * "liveStatus":0,    //0：未开播 1：直播中
     * "url":"https://live.bilibili.com/92613",
     * "title":"万能的普瑞斯特",  //直播间标题
     * "cover":"http://i0.hdslb.com/bfs/live/bef09ae4739d7005332c10dbb91d55e6a8241275.jpg",  //直播间封面
     * "online":449411, //直播间人气
     * "roomid":92613,  //直播间ID
     * "broadcast_type":0,
     * "online_hidden":0}
     * }
     */
    public static String getLive(String uid) throws IOException {
        String url = "http://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid=" + uid;
        return HttpUtils.get(url);
    }

    public void fresh() throws IOException {
        user = new User(mid);
        String live = getLive(mid);
        JSONObject jsonObject = JSON.parseObject(live);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            roomStatus = data.getInteger("roomStatus");
            if (roomStatus == 0) {
                return; // 无房间
            }
            roundStatus = data.getInteger("roundStatus");
            liveStatus = data.getInteger("liveStatus");
            url = data.getString("url");
            title = data.getString("title");
            online = data.getInteger("online");
            roomId = data.getInteger("roomid");

            String coverFromJson = data.getString("cover");
            boolean isUrl = isUrl(coverFromJson);
            if (isUrl) {
                String fileName = getImageName(coverFromJson);
                if (cover == null || cover.getName().equals(fileName)) {
                    cover = new File(LIVE_TEMP_FOLDER + fileName);
                    FileUtils.deleteQuietly(cover);
                    FileUtils.touch(cover);
                    URL imageUrl = new URL(coverFromJson);
                    FileUtils.copyURLToFile(imageUrl, cover);
                }
            }
        }
    }

    private String getImageName(String url) {
        String regex = "http://i0.hdslb.com/bfs/live/(.*.jpg)";
        String result = null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);
        while (matcher.find()) {
            result = matcher.group(1);
        }
        return result;
    }
}
