package io.koschicken.utils.saucenao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.saucenao.Result;
import io.koschicken.bean.saucenao.ResultData;
import io.koschicken.bean.saucenao.ResultHeader;
import io.koschicken.bean.saucenao.Sauce;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SauceNaoUtils {

    private SauceNaoUtils() {
    }

    private static final Map<Integer, String> nameMap = new HashMap<>();

    static {
        nameMap.put(0, "h-mags");
        nameMap.put(1, "h-anime*");
        nameMap.put(2, "hcg");
        nameMap.put(3, "ddb-objects*");
        nameMap.put(4, "ddb-samples*");
        nameMap.put(5, "pixiv");
        nameMap.put(6, "pixivhistorical");
        nameMap.put(7, "anime*");
        nameMap.put(8, "seiga_illust - nico nico seiga");
        nameMap.put(9, "danbooru");
        nameMap.put(10, "drawr");
        nameMap.put(11, "nijie");
        nameMap.put(12, "yande.re");
        nameMap.put(13, "animeop*");
        nameMap.put(14, "IMDb*");
        nameMap.put(15, "Shutterstock*");
        nameMap.put(16, "FAKKU");
        nameMap.put(18, "H-MISC (nhentai)");
        nameMap.put(19, "2d_market");
        nameMap.put(20, "medibang");
        nameMap.put(21, "Anime");
        nameMap.put(22, "H-Anime");
        nameMap.put(23, "Movies");
        nameMap.put(24, "Shows");
        nameMap.put(25, "gelbooru");
        nameMap.put(26, "konachan");
        nameMap.put(27, "sankaku");
        nameMap.put(28, "anime-pictures");
        nameMap.put(29, "e621");
        nameMap.put(30, "idol complex");
        nameMap.put(31, "bcy illust");
        nameMap.put(32, "bcy cosplay");
        nameMap.put(33, "portalgraphics");
        nameMap.put(34, "dA");
        nameMap.put(35, "pawoo");
        nameMap.put(36, "madokami");
        nameMap.put(37, "mangadex");
        nameMap.put(38, "H-Misc (ehentai)");
        nameMap.put(39, "ArtStation");
        nameMap.put(40, "FurAffinity");
        nameMap.put(41, "Twitter");
        nameMap.put(42, "Furry Network");
    }

    public static String name(int id) {
        return StringUtils.isEmpty(nameMap.get(id)) ? "" : nameMap.get(id);
    }

    public static Sauce json2Sauce(String json) {
        Sauce sauce = JSON.parseObject(json, Sauce.class);
        List<Result> resultList = new ArrayList<>();
        JSONArray results = JSON.parseObject(json).getJSONArray("results");
        for (int i = 0; i < results.size(); i++) {
            Result result = new Result();
            JSONObject jsonObject = results.getJSONObject(i);
            ResultHeader header = jsonObject.getObject("header", ResultHeader.class);
            result.setHeader(header);
            ResultData data = jsonObject.getObject("data", ResultData.class);
            JSONObject resultData = jsonObject.getJSONObject("data");
            resultData.remove("ext_urls");
            data.setExtInfo(resultData);
            result.setData(data);
            resultList.add(result);
        }
        sauce.setResults(resultList);
        return sauce;
    }
}
