package io.koschicken.bean.bilibili.space;

import lombok.Data;

import java.io.File;

@Data
public class LiveRoom {
    /**
     * 直播间状态
     * 0-无 1-有
     */
    private Integer roomStatus;
    /**
     * 直播状态
     * 0-未开播 1-直播中
     */
    private Integer liveStatus;
    /**
     * 直播间地址
     */
    private String url;
    /**
     * 直播间标题
     */
    private String title;
    /**
     * 直播间封面地址
     */
    private String cover;
    private File coverFile;
    /**
     * 人气
     */
    private Integer online;
    /**
     * 直播间ID
     */
    private Integer roomId;
    /**
     * 轮播状态
     * 0-未轮播 1-轮播
     */
    private Integer roundStatus;
    private Integer broadcastType;
}
