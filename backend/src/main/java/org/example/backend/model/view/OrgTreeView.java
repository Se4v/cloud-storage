package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrgTreeView {
    private Long id;
    private Long driveId;
    private String name;
    private Integer type;
    private List<OrgTreeView> children;
}
