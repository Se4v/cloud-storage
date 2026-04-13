package org.example.backend.model.response.file;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EntryResp {
    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Integer type;
    @NotNull
    private Long size;
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
