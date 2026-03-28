package org.example.backend.model.view;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginView {
    private String token;
    private Long personalDriveId;
}
