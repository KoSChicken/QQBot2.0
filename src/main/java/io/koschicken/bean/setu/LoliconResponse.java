package io.koschicken.bean.setu;

import lombok.Data;

import java.util.List;

@Data
public class LoliconResponse {
    private String error;
    private List<Pixiv> data;
}
