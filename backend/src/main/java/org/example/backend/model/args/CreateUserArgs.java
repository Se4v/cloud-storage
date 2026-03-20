package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateUserArgs {
    private String username;
    private String realName;
    private String mobile;
    private Long storageQuota;
}
