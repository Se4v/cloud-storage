package org.example.backend.model.view;

import lombok.Data;

@Data
public class RecycleView {
    private String id;
    private String name;
    private String type;
    private String path;
    private String deleteTime;
    private String expireTime;
    private String size;
}
