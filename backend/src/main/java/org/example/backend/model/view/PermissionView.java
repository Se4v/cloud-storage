package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermissionView {
    private Long id;
    private String name;
    private String code;
    private String type;
}
