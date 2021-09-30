package io.koschicken.bean.setu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Consts;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Data
public class Pixiv {
    private static final String LOLICON_API = "https://api.lolicon.app/setu/v2";
    private Integer pid;
    private Integer p;
    private Integer uid;
    private String title;
    private String author;
    private boolean r18;
    private Integer width;
    private Integer height;
    private String[] tags;
    private String ext;
    private Long uploadDate;
    private Map<String, String> urls;
    private String keyword;
    private int num;

    public Pixiv() {
    }

    public Pixiv(String keyword, int num, Boolean r18) {
        this.keyword = keyword;
        this.num = num;
        this.r18 = r18;
    }

    public static LoliconResponse get(String keyword, int num, Boolean r18) throws IOException {
        LoliconResponse loliconResponse = fetchFromLolicon(num, keyword, null, r18);
        if (loliconResponse.getData() == null) {
            return fetchFromLolicon(num, null, keyword, r18);
        } else {
            return loliconResponse;
        }
    }

    private static LoliconResponse fetchFromLolicon(int num, String tag, String keyword, Boolean r18) throws IOException {
        String loliconApi = LOLICON_API + "?size=original&size=regular&num=" + num;
        if (StringUtils.hasText(tag)) {
            loliconApi += "&tag=" + URLEncoder.encode(tag, StandardCharsets.UTF_8);
        }
        if (StringUtils.hasText(keyword)) {
            loliconApi += "&keyword=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        }
        if (r18 != null) {
            loliconApi += "&r18=" + (Boolean.TRUE.equals(r18) ? 1 : 0);
        }
        log.info("这次请求的Lolicon地址： {}", loliconApi);
        ResponseHandler<String> myHandler = response -> EntityUtils.toString(response.getEntity(), Consts.UTF_8);
        String response = Request.Get(loliconApi).execute().handleResponse(myHandler);
        JSONObject jsonObject = JSON.parseObject(response);
        String error = jsonObject.getString("error");
        LoliconResponse loliconResponse = new LoliconResponse();
        loliconResponse.setError(error);
        JSONArray dataArray = jsonObject.getJSONArray("data");
        if (StringUtils.hasText(error)) {
            return loliconResponse;
        }
        if (!dataArray.isEmpty()) {
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
        pixiv.setUrls(JSONObject.parseObject(urls.toJSONString(), new TypeReference<>() {
        }));
        log.info("这次请求到的图片url： {}", urls.get("original"));
        return pixiv;
    }
}
