package io.koschicken.bean.bilibili;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Following {
    private String uid;
    private String name;
    private boolean notification;
    private boolean noticed;
    private Long lastModifiedTime;
}
