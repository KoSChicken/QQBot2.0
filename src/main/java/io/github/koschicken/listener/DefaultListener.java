package io.github.koschicken.listener;

import love.forte.simboot.annotation.ContentTrim;
import love.forte.simboot.annotation.Filter;
import love.forte.simboot.annotation.Listener;
import love.forte.simbot.event.GroupMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class DefaultListener {

    @Listener
    @Filter(value = "xsp", targets = @Filter.Targets(atBot = true))
    @ContentTrim
    public void onChannelMessage(GroupMessageEvent event) {
        event.replyBlocking(":)");
    }
}
