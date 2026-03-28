package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.result.EntryWithBlobResult;

import java.util.List;

public interface EntryMapper extends BaseMapper<Entry> {
    List<Entry> selectRecursiveChildEntryIdsBatch(List<Long> folderIds);
}
