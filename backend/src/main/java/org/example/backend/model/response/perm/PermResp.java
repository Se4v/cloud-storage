package org.example.backend.model.response.perm;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermResp {
    private Long id;
    private String name;
    private String code;
    private Integer type;
}
