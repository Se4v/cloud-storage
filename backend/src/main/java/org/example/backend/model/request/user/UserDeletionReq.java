package org.example.backend.model.request.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserDeletionReq {
    @NotEmpty
    private List<Long> userIds;
}
