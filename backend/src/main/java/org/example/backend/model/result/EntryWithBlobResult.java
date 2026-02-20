package org.example.backend.model.result;

import lombok.Data;

@Data
public class EntryWithBlobResult {
    private Long entryId;
    private Long parentId;
    private String entryName;
    private Integer entryType;

    private String bucketName;
    private String objectKey;
}
