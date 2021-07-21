package io.koschicken.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import io.koschicken.bean.setu.LoliconResponse;
import io.koschicken.bean.setu.Pixiv;
import org.apache.http.Consts;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetuUtils {
    private static final String LOLICON_API = "https://api.lolicon.app/setu/v2";
    private static final Logger LOGGER = LoggerFactory.getLogger(SetuUtils.class);

    private SetuUtils() {
    }

    public static LoliconResponse getSetu(String tag, int num, Boolean r18) throws IOException {
        return fetchFromLolicon(num, tag, r18);
    }

    private static LoliconResponse fetchFromLolicon(int num, String tag, Boolean r18) throws IOException {
        String loliconApi = LOLICON_API + "?size=original&size=regular&num=" + num;
        if (!StringUtils.isEmpty(tag)) {
            loliconApi += "&tag=" + URLEncoder.encode(tag, StandardCharsets.UTF_8);
        }
        if (r18 != null) {
            loliconApi += "&r18=" + (Boolean.TRUE.equals(r18) ? 1 : 0);
        }
        LOGGER.info("这次请求的Lolicon地址： {}", loliconApi);
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(loliconApi).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        String error = jsonObject.getString("error");
        LoliconResponse loliconResponse = new LoliconResponse();
        loliconResponse.setError(error);
        if (StringUtils.isEmpty(error)) {
            JSONArray dataArray = jsonObject.getJSONArray("data");
            List<Pixiv> data = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject pic = dataArray.getJSONObject(i);
                Pixiv pixiv = fillData(pic);
                data.add(pixiv);
            }
            loliconResponse.setData(data);
        }
        return loliconResponse;
    }

    private static Pixiv fillData(JSONObject data) {
        Pixiv pixiv = new Pixiv();
        pixiv.setPid(data.getInteger("pid"));
        pixiv.setP(data.getInteger("p"));
        pixiv.setUid(data.getInteger("uid"));
        pixiv.setTitle(data.getString("title"));
        pixiv.setAuthor(data.getString("author"));
        pixiv.setR18(Boolean.parseBoolean(data.getString("r18")));
        pixiv.setWidth(data.getInteger("width"));
        pixiv.setHeight(data.getInteger("height"));
        JSONArray tags = data.getJSONArray("tags");
        pixiv.setTags(Arrays.stream(tags.toArray()).map(Object::toString).toArray(String[]::new));
        pixiv.setExt(data.getString("ext"));
        pixiv.setUploadDate(data.getLong("uploadDate"));
        JSONObject urls = data.getJSONObject("urls");
        pixiv.setUrls(JSONObject.parseObject(urls.toJSONString(), new TypeReference<>() {}));
        LOGGER.info("这次请求到的图片url： {}", urls.get("original"));
        return pixiv;
    }
}
