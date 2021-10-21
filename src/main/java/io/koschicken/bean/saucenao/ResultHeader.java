package io.koschicken.bean.saucenao;

import lombok.Data;

@Data
public class ResultHeader {
    private double similarity;
    private String thumbnail;
    private int indexId;
    private String indexName;
    private int dupes;
}
