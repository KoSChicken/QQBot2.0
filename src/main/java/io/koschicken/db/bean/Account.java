package io.koschicken.db.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("account")
public class Account implements Serializable {

    private static final long serialVersionUID = 3424842641174723836L;

    @TableId(value = "account")
    private String account;

    @TableField(value = "sign_flag")
    private Boolean signFlag;

    @TableField(value = "live_switch")
    private Boolean liveSwitch;

    @TableField(value = "coin")
    private Long coin;

    @TableField(value = "roll")
    private Integer roll;

    @TableField(value = "cygames_win")
    private Integer cygamesWin;

    @TableField(value = "nekogun")
    private Integer nekogun;
}
