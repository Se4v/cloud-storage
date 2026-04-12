package org.example.backend.model.response.stat;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileTypeDistributionResp {
    public String name;
    public Long size;
    public String percent;
}
