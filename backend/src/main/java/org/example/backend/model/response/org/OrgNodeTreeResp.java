package org.example.backend.model.response.org;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrgNodeTreeResp {
    private Long id;
    private Long driveId;
    private String name;
    private Integer type;
    private List<OrgNodeTreeResp> children;
}
