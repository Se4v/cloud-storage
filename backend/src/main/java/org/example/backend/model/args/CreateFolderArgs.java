package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateFolderArgs {
    private Long nodeId;
    private Long parentId;
    private String folderName;
}
