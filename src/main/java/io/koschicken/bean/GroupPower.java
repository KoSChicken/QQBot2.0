package io.koschicken.bean;

import lombok.Data;

@Data
public class GroupPower {
    private boolean globalSwitch;//总开关
    private boolean gachaSwitch;//扭蛋开关
    private boolean maiyaoSwitch;//买药小助手提示
    private boolean horseSwitch;//赛马开关
    private boolean setuSwitch;//涩图开关
    private boolean diceSwitch;//骰子开关

    public GroupPower allSwitch(boolean b) {
        setGlobalSwitch(b);
        setDiceSwitch(b);
        setGachaSwitch(b);
        setHorseSwitch(b);
        setMaiyaoSwitch(b);
        setSetuSwitch(b);
        return this;
    }
}
