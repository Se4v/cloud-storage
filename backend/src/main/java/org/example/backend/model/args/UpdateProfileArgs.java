package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateProfileArgs {
    private Long userId;
    private String email;
    private String mobile;
}
