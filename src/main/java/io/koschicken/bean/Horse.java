package io.koschicken.bean;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.koschicken.constants.GameConstants.EMOJI_LIST;

@Data
public class Horse {

    private List<Integer> position; // 马的位置
    private List<Integer> type; // 马的种类，在静态文件夹下

    public Horse() {
        Random random = new Random();
        position = new ArrayList<>();
        position.add(0);
        position.add(0);
        position.add(0);
        position.add(0);
        position.add(0);
        type = new ArrayList<>();
        type.add(random.nextInt(EMOJI_LIST.length));
        type.add(random.nextInt(EMOJI_LIST.length));
        type.add(random.nextInt(EMOJI_LIST.length));
        type.add(random.nextInt(EMOJI_LIST.length));
        type.add(random.nextInt(EMOJI_LIST.length));
    }
}
