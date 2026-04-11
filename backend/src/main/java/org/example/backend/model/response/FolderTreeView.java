package org.example.backend.model.response;

import lombok.Data;

import java.util.List;

@Data
public class FolderTreeView {
    private Long id;
    private String name;
    private List<FolderTreeView> children;
}
