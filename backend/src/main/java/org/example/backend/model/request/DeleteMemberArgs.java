package org.example.backend.model.request;

import lombok.Data;

import java.util.List;

@Data
public class DeleteMemberArgs {
    private List<Long> memberIds;
}
