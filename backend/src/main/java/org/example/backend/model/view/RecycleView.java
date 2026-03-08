package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecycleView {
    private String id;
    private String name;
    private String type;
    private String path;
    private String size;
    private String deleteTime;
    private String expireTime;
}
