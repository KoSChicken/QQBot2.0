package io.koschicken.utils.saucenao;

import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.saucenao.Result;
import io.koschicken.bean.saucenao.ResultData;
import io.koschicken.bean.saucenao.ResultHeader;
import io.koschicken.bean.saucenao.Sauce;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
class SauceNaoUtilsTest {

    @Test
    void json2Sauce() throws IOException {
        String json = FileUtils.readFileToString(new File("D://sauce.json"), StandardCharsets.UTF_8);
        Sauce sauce = SauceNaoUtils.json2Sauce(json);
        log.info(sauce.toString());
        List<Result> results = sauce.getResults();
        for (Result result : results) {
            ResultHeader header = result.getHeader();
            double similarity = header.getSimilarity();
            String indexName = header.getIndexName().split("-")[0].trim().split(":")[1].trim();
            String thumbnail = header.getThumbnail();
            URL url = new URL(thumbnail);
            String path = url.getPath();
            String fileExtension = FilenameUtils.getExtension(path);
            ResultData data = result.getData();
            try {
                File originalFile = new File("./temp/" + "saucenao/" + FilenameUtils.getName(path) + "." + fileExtension);
                if (!originalFile.exists()) {
                    saveThumbnail(thumbnail, originalFile);
                }
                StringBuilder message = new StringBuilder();
                message.append("\n");
                String[] extUrls = data.getExtUrls();
                if (extUrls != null) {
                    for (String extUrl : extUrls) {
                        message.append(extUrl).append("\n");
                    }
                }
                message.append("相似度：").append(similarity).append("\n")
                        .append("类别：").append(indexName).append("\n")
                        .append("附加信息：").append("\n");
                JSONObject extInfo = data.getExtInfo();
                extInfo.keySet().forEach(k -> message.append(k).append(": ").append(extInfo.getString(k)).append("\n"));
                log.info(message.toString());
            } catch (IOException e) {
                log.error("error: ", e);
            }
        }
    }

    private static void saveThumbnail(String thumbnail, File target) throws IOException {
        HttpEntity entity = Request.Get(thumbnail)
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/20100101 Firefox/80.0")
                .setHeader("Referer", "https://saucenao.com/")
                .execute().returnResponse().getEntity();
        FileUtils.copyInputStreamToFile(entity.getContent(), target);
    }
}