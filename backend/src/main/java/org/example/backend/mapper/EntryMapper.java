package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.result.EntryWithBlobResult;

import java.util.List;

public interface EntryMapper extends BaseMapper<Entry> {
    List<EntryWithBlobResult> selectDescendantsWithBlobByEntryIds(Long entryId);

    List<EntryWithBlobResult> selectBatchWithBlobByEntryIds(List<Long> entryIds);

    List<Long> selectRecursiveChildEntryIdsBatch(List<Long> folderIds);

    Integer updateDeleteFlagBatch(List<Long> entryIds);
}
