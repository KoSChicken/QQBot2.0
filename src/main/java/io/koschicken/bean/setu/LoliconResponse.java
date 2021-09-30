package io.koschicken.bean.setu;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoliconResponse {
    private String error;
    private List<Pixiv> data;
    private String customMsg;
}
