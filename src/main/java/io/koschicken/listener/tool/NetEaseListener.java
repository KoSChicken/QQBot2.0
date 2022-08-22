package io.koschicken.listener.tool;

import catcode.CatCodeUtil;
import io.koschicken.bean.netease.NetEaseMusic;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class NetEaseListener {

    @OnGroup
    @Filter(value = "/wyy", matchType = MatchType.STARTS_WITH)
    public void netEaseMusic(GroupMsg groupMsg, MsgSender sender) throws Exception {
        String keyword = groupMsg.getMsgContent().getMsg().replace("/wyy", "").trim();
        NetEaseMusic netEaseMusic = NetEaseMusic.searchWithoutLink(keyword, 1, 1);
        if (Objects.nonNull(netEaseMusic)) {
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String music = catCodeUtil.toCat("music", "kind=neteaseCloud", "title=" + netEaseMusic.getName(),
                    "musicUrl=" + netEaseMusic.getMusicUrl(),
                    "jumpUrl=" + netEaseMusic.getUrl(),
                    "pictureUrl=" + netEaseMusic.getAlbum().getPicUrl(),
                    "summary=" + netEaseMusic.getArtists().get(0).getName());
            sender.SENDER.sendGroupMsg(groupMsg, music);
        } else {
            sender.SENDER.sendGroupMsg(groupMsg, "冇");
        }
    }
}
