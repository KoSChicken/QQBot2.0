package io.koschicken.bean.saucenao;

import io.koschicken.utils.saucenao.SauceNaoUtils;
import lombok.Data;

@Data
public class ResultIndex {

    private int status;
    private int parentId;
    private int id;
    private int result;
    private String name;
    private boolean disabled;

    public String getName() {
        return SauceNaoUtils.name(id);
    }
}
