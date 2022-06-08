package io.koschicken.bean.bilibili;

import io.koschicken.utils.bilibili.BilibiliUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
@Data
public class BiliUser {

    private static final String USER_TEMP_FOLDER = "./temp/bilibili/user/";

    static {
        File videoFolder = new File(USER_TEMP_FOLDER);
        if (!videoFolder.exists()) {
            try {
                FileUtils.forceMkdir(videoFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String mid;
    private String uname;
    private String usign;
    private Integer fans;
    private Integer videos;
    private String upic;
    private File upicFile;

    public BiliUser(String mid, String uname, String usign, Integer fans, Integer videos, String upic) throws IOException {
        this.mid = mid;
        this.uname = uname;
        this.usign = usign;
        this.fans = fans;
        this.videos = videos;
        String fullUpic = upic.startsWith("http") ? upic.replace("http:", "https:") : "https://" + upic;
        this.upic = fullUpic;
        URL url = new URL(fullUpic);
        String imageName = BilibiliUtils.getImageName(url);
        File file = new File(USER_TEMP_FOLDER + imageName);
        FileUtils.deleteQuietly(file);
        FileUtils.touch(file);
        FileUtils.copyURLToFile(url, file);
        this.upicFile = file;
    }
}
