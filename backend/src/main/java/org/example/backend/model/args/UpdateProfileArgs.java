package org.example.backend.model.args;

import lombok.Data;

@Data
public class UpdateProfileArgs {
    private String email;
    private String mobile;
}
