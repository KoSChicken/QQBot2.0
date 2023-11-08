package io.koschicken.config;

import lombok.Data;

@Data
public class GroupConfig {
    private boolean globalSwitch;// 总开关
    private boolean setuSwitch;// 涩图开关
    private boolean diceSwitch;// 骰子开关

    public void allSwitch(boolean b) {
        setGlobalSwitch(b);
        setDiceSwitch(b);
        setSetuSwitch(b);
    }
}
