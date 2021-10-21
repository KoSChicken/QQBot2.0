package io.koschicken.bean.saucenao;

import lombok.Data;

import java.util.List;

@Data
public class Sauce {
    private SauceHeader header;
    private List<Result> results;
}
