package io.koschicken.bean;

public class GroupPower {
    private boolean globalSwitch;//总开关
    private boolean gachaSwitch;//扭蛋开关
    private boolean maiyaoSwitch;//买药小助手提示
    private boolean horseSwitch;//赛马开关
    private boolean setuSwitch;//涩图开关
    private boolean diceSwitch;//骰子开关

    public boolean isGlobalSwitch() {
        return globalSwitch;
    }

    public GroupPower setGlobalSwitch(boolean globalSwitch) {
        this.globalSwitch = globalSwitch;
        return this;
    }

    public boolean isGachaSwitch() {
        return gachaSwitch;
    }

    public GroupPower setGachaSwitch(boolean gachaSwitch) {
        this.gachaSwitch = gachaSwitch;
        return this;
    }

    public boolean isMaiyaoSwitch() {
        return maiyaoSwitch;
    }

    public GroupPower setMaiyaoSwitch(boolean maiyaoSwitch) {
        this.maiyaoSwitch = maiyaoSwitch;
        return this;
    }

    public boolean isHorseSwitch() {
        return horseSwitch;
    }

    public GroupPower setHorseSwitch(boolean horseSwitch) {
        this.horseSwitch = horseSwitch;
        return this;
    }

    public boolean isSetuSwitch() {
        return setuSwitch;
    }

    public GroupPower setSetuSwitch(boolean setuSwitch) {
        this.setuSwitch = setuSwitch;
        return this;
    }

    public boolean isDiceSwitch() {
        return diceSwitch;
    }

    public GroupPower setDiceSwitch(boolean diceSwitch) {
        this.diceSwitch = diceSwitch;
        return this;
    }

    public GroupPower allSwitch(boolean b) {
        setGlobalSwitch(b);
        setDiceSwitch(b);
        setGachaSwitch(b);
        setHorseSwitch(b);
        setMaiyaoSwitch(b);
        setSetuSwitch(b);
        return this;
    }

    @Override
    public String toString() {
        return "GroupPower{" +
                "globalSwitch=" + globalSwitch +
                ", gachaSwitch=" + gachaSwitch +
                ", maiyaoSwitch=" + maiyaoSwitch +
                ", horseSwitch=" + horseSwitch +
                ", setuSwitch=" + setuSwitch +
                ", diceSwitch=" + diceSwitch +
                '}';
    }
}
