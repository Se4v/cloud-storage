package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecycleBinView {
    private String id;
    private String name;
    private String drive;
    private String size;
    private String deleteTime;
    private String expireTime;
}
