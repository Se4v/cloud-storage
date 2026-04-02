package org.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.example.backend.model.entity.Entry;

import java.util.List;
import java.util.Map;

public interface EntryMapper extends BaseMapper<Entry> {
    List<Entry> selectDescendantsByFolderId(List<Long> folderIds);

    List<Map<String, Object>> selectFileCategoryStats();
}
