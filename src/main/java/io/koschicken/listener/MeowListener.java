package io.koschicken.listener;

import catcode.CatCodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.FileUtils;
import org.apache.http.Consts;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class MeowListener {
    private static final String MEOW_DIR = "./temp/MEOW/";
    private static final String MEOW = "http://aws.random.cat/meow";
    private static final String UA = "User-Agent";
    private static final String UA_STRING = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3";

    @OnGroup
    @Filter(value = "喵(.*?)", matchType = MatchType.REGEX_MATCHES)
    public void meow(GroupMsg msg, MsgSender sender) throws IOException {
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(MEOW).addHeader(UA, UA_STRING).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        String fileUrl = jsonObject.getString("file");
        String fileSuffix = fileUrl.substring(fileUrl.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        File file = new File(MEOW_DIR + uuid + fileSuffix);
        if (!Objects.equals(fileSuffix, ".gif")) {
            Thumbnails.of(new URL(fileUrl.replace("https", "http"))).scale(1)
                    .outputQuality(0.25).toFile(file);
        } else {
            FileUtils.copyURLToFile(new URL(fileUrl.replace("https", "http")), file);
        }
        CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
        String image = catCodeUtil.getStringTemplate().image(file.getAbsolutePath());
        sender.SENDER.sendGroupMsg(msg, image);
    }
}
