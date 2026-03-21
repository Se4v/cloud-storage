package org.example.backend.model.args;

import lombok.Data;

import java.util.List;

@Data
public class DeleteMemberArgs {
    private List<Long> memberIds;
}
