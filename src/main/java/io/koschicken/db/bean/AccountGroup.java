package io.koschicken.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

@Data
@TableName("account_group")
public class AccountGroup implements Serializable {

    private static final long serialVersionUID = -7334313875124373777L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String account;

    @TableField(value = "group_code")
    private String groupCode;

    public AccountGroup(String account, String groupCode) {
        this.account = account;
        this.groupCode = groupCode;
    }
}
