package org.example.backend.model.response.file;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StorageView {
    private Long id;
    private String name;
    private String uploadTime;
    private Long size;
    private Boolean isEnabled;
    private Integer refCount;
}
