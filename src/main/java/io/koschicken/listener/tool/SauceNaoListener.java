package io.koschicken.listener.tool;

import com.alibaba.fastjson.JSONObject;
import io.koschicken.bean.saucenao.Result;
import io.koschicken.bean.saucenao.ResultData;
import io.koschicken.bean.saucenao.ResultHeader;
import io.koschicken.bean.saucenao.Sauce;
import io.koschicken.utils.HttpUtils;
import io.koschicken.utils.saucenao.SauceNaoUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.results.GroupMemberInfo;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.component.mirai.message.MiraiMessageContent;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilder;
import love.forte.simbot.component.mirai.message.MiraiMessageContentBuilderFactory;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.koschicken.constants.Constants.COMMON_CONFIG;

@Slf4j
@Service
public class SauceNaoListener {

    @Autowired
    private MiraiMessageContentBuilderFactory factory;

    private static final String TEMP_DIR = "./temp/";
    private static final String SAUCENAO_FOLDER = "saucenao/";
    private static final String SAUCENAO_API = "https://saucenao.com/search.php?db=999&output_type=2&testmode=1&numres=16&url=";

    static {
        File sauceNaoDir = new File(TEMP_DIR + SAUCENAO_FOLDER);
        if (!sauceNaoDir.exists()) {
            try {
                FileUtils.forceMkdir(sauceNaoDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnGroup
    @Filter(value = "/saucenao", matchType = MatchType.STARTS_WITH)
    public void saucenao(GroupMsg groupMsg, MsgSender sender) throws IOException {
        String picUrl = groupMsg.getMsgContent().getCats("image").get(0).get("url");
        if (Objects.nonNull(picUrl)) {
            String requestUrl = SAUCENAO_API + URLEncoder.encode(picUrl, StandardCharsets.UTF_8) + "&api_key=" + COMMON_CONFIG.getSauceNaoApiKey();
            log.info(requestUrl);
            String s = HttpUtils.get(requestUrl);
            Sauce sauce = SauceNaoUtils.JSON2Sauce(s);
            List<Result> results = sauce.getResults();
            if (!CollectionUtils.isEmpty(results)) {
                ArrayList<MessageContent> msgList = new ArrayList<>();
                results.subList(0, Math.min(3, results.size() - 1)).forEach(result -> msgList.add(buildMessage(result)));
                MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
                messageContentBuilder.forwardMessage(forwardBuilder -> {
                    for (MessageContent messageContent : msgList) {
                        forwardBuilder.add(RandomUtils.nextBoolean() ? groupMsg.getAccountInfo() : randomGroupMember(groupMsg, sender), messageContent);
                    }
                });
                final MiraiMessageContent messageContent = messageContentBuilder.build();
                sender.SENDER.sendGroupMsg(groupMsg, messageContent);
            } else {
                sender.SENDER.sendGroupMsg(groupMsg, "冇");
            }
        }
    }

    private GroupMemberInfo randomGroupMember(GroupMsg groupMsg, MsgSender sender) {
        List<GroupMemberInfo> groupMemberList = sender.GETTER.getGroupMemberList(groupMsg.getGroupInfo().getGroupCode())
                .stream().collect(Collectors.toList());
        Collections.shuffle(groupMemberList);
        return groupMemberList.get(0);
    }

    private MessageContent buildMessage(Result result) {
        MiraiMessageContentBuilder messageContentBuilder = factory.getMessageContentBuilder();
        ResultHeader header = result.getHeader();
        double similarity = header.getSimilarity();
        String indexName = header.getIndexName().split("-")[0].trim().split(":")[1].trim();
        String thumbnail = header.getThumbnail();
        ResultData data = result.getData();
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
        return messageContentBuilder.image(thumbnail).text(message).build();
    }
}
