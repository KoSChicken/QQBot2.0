package io.koschicken.utils.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.bilibili.Following;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static io.koschicken.constants.Constants.CONFIG_DIR;
import static io.koschicken.intercept.BotIntercept.GROUP_BILIBILI_MAP;

public class BilibiliUtils {

    private BilibiliUtils(){
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
}
