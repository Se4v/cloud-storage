package org.example.backend.model.args;

import lombok.Data;

@Data
public class ChangePasswordArgs {
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;
}
