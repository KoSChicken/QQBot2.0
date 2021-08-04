package io.koschicken.listener.game;

import catcode.CatCodeUtil;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.MsgSender;
import love.forte.simbot.filter.MatchType;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DiceListener {

    @OnGroup
    @Filter(value = "#豹？", matchType = MatchType.EQUALS)
    public void bao(GroupMsg msg, MsgSender sender) {
        List<String> diceResult = new ArrayList<>();
        boolean allSame = true; // 豹子flag
        int sum = 0;
        for (int i = 0; i < 3; i++) {
            int roll = roll();
            sum += roll;
            diceResult.add(String.valueOf(roll));
            if (i != 0 && roll != Integer.parseInt(diceResult.get(i - 1))) {
                allSame = false;
            }
        }
        String result = result(allSame, sum);
        sender.SENDER.sendGroupMsg(msg, "豹子".equals(result) ? "豹了" : "没豹");
    }

    private int roll() {
        return RandomUtils.nextInt(1, 7);
    }

    private String result(boolean allSame, int sum) {
        if (allSame) {
            return "豹子";
        } else {
            if (sum >= 4 && sum <= 10) {
                return "小";
            } else if (sum >= 11 && sum <= 17) {
                return "大";
            }
            return "";
        }
    }

    @OnGroup
    @Filter(value = "#roll(.*)[-dD](.*)", matchType = MatchType.REGEX_MATCHES)
    public void roll(GroupMsg msg, MsgSender sender) {
        try {
            String regex = "#roll(.*)[-dD](.*)";
            String message = msg.getMsg();
            Pattern p = Pattern.compile(regex);
            Matcher m = p.matcher(message);
            int count = 1;
            int limit = 4;
            while (m.find()) {
                count = Math.max(Integer.parseInt(m.group(1).trim()), 1);
                limit = Math.max(Integer.parseInt(m.group(2).trim()), 4);
            }
            if (count > 20) {
                sender.SENDER.sendGroupMsg(msg, "你正常点，没那么多骰子给你扔。");
                return;
            }
            StringBuilder sb = new StringBuilder();
            String accountCode = msg.getAccountInfo().getAccountCode();
            CatCodeUtil catCodeUtil = CatCodeUtil.getInstance();
            String at = catCodeUtil.getStringTemplate().at(accountCode);
            sb.append(at).append("roll出了");
            for (int i = 0; i < count; i++) {
                int singleDice = RandomUtils.nextInt(1, limit + 1);
                sb.append("[").append(singleDice).append("]");
                if (i != count - 1) {
                    sb.append(", ");
                }
            }
            sb.append("点，本次使用了").append(count).append("个").append(limit).append("面骰。");
            sender.SENDER.sendGroupMsg(msg, sb.toString());
        } catch (NumberFormatException e) {
            sender.SENDER.sendGroupMsg(msg, "格式错误");
        }
    }
}
