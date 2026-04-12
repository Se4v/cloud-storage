package org.example.backend.model.response.org;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NodeView {
    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    private Long parentId;
    @NotNull
    private String parentName;
    @NotNull
    @PositiveOrZero
    private Long storageQuota;
    @NotNull
    private Integer isEnabled;
    @NotNull
    private String adminUsername;
}
