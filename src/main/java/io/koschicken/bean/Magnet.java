package io.koschicken.bean;

import lombok.Data;

@Data
public class Magnet {
    private String name;
    private String mag;
    private String shortMag;
    private String size;
    private String releaseTime;

    public String getShortMag() {
        return mag.substring(0, mag.indexOf("&"));
    }
}
