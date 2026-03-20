package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateUserArgs {
    private Long id;
    private String realName;
    private String mobile;
    private String email;
    private Integer storageQuota;
    private Integer status;
}
