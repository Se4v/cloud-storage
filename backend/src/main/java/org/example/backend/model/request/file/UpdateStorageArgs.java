package org.example.backend.model.request.file;

import lombok.Data;

import java.util.List;

@Data
public class UpdateStorageArgs {
    private Boolean isEnabled;
    private List<Long> storageIds;
}
