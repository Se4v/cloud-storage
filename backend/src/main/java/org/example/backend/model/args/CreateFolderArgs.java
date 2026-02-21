package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateFolderArgs {
    private Long driveId;
    private Long parentId;
    private String folderName;
}
