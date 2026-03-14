package org.example.backend.model.args;

import lombok.Data;

@Data
public class CreateUserArgs {
    private String username;
    private String password;
    private String realName;
    private String mobile;
    private String email;
    private Integer storageQuota;
}
