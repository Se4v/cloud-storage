package org.example.backend.model.response.file;

import lombok.Data;

import java.util.List;

@Data
public class FolderTreeResp {
    private Long id;
    private String name;
    private List<FolderTreeResp> children;
}
