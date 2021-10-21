package io.koschicken.bean.saucenao;

import lombok.Data;

import java.util.Map;

@Data
public class SauceHeader {
    private String userId;
    private int accountType;
    private int shortLimit;
    private int longLimit;
    private int shortRemain;
    private int longRemain;
    private int status;
    private int resultsRequested;
    private Map<String, ResultIndex> index;
    private int searchDepth;
    private double minimumSimilarity;
    private String queryImageDisplay;
    private String queryImage;
    private int resultReturned;
}
