package io.koschicken.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HorseEvent {
    private String help = "在下面俩添加事件，?是几号马的占位符，？需要是英文状态";
    private List<String> badHorseEvent = new ArrayList<>();
    private List<String> goodHorseEvent = new ArrayList<>();

    public HorseEvent() {
        badHorseEvent.add("?号马滑倒了！");
        badHorseEvent.add("?号马自由了!");
        badHorseEvent.add("?号马踩到了xcw");
        badHorseEvent.add("?号马突然想上天摘星星");
        badHorseEvent.add("?号马掉入了时辰的陷阱");
        goodHorseEvent.add("?号马发现了前方的母马，加速加速！");
        goodHorseEvent.add("?号马使用了私藏的超级棒棒糖，加速加速！");
        goodHorseEvent.add("?号马已经没什么所谓了！");
        goodHorseEvent.add("?号马发现，赛道岂是如此不便之物！");
    }
}
