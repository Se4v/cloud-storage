package org.example.backend.model.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileTypeDistributionView {
    public String name;
    public Long size;
    public String percent;
}
