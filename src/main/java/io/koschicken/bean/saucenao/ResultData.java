package io.koschicken.bean.saucenao;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * 通用的结果映射
 */
@Data
public class ResultData {
    // 源地址
    private String[] extUrls;
    // 其他信息
    private JSONObject extInfo;
}
