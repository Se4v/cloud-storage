package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.result.EntryWithBlobResult;
import org.example.backend.model.result.RecycleDetailResult;

import java.util.List;

public interface EntryMapper extends BaseMapper<Entry> {
    List<EntryWithBlobResult> selectDescendantsWithBlobByEntryIds(Long entryId);

    List<EntryWithBlobResult> selectBatchWithBlobByEntryIds(List<Long> entryIds);

    List<Long> selectRecursiveChildEntryIdsBatch(List<Long> folderIds);

    Integer updateStatusBatch(List<Long> entryIds, Integer status);

    List<RecycleDetailResult> selectValidRecycleBinEntryByUserId(Long userId);

    List<RecycleDetailResult> selectRecycleBinEntryByEntryIds(List<Long> entryIds);
}
