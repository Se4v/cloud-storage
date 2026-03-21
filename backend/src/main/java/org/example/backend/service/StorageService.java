package org.example.backend.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.args.UpdateStorageArgs;
import org.example.backend.model.entity.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StorageService {
    @Autowired
    private StorageMapper storageMapper;

    @Transactional
    public void updateStorageItems(UpdateStorageArgs args) {
        if (args == null) throw new BusinessException("");

        if (args.getStorageIds() == null) throw new BusinessException("");

        int enabled = Boolean.TRUE.equals(args.getIsEnabled()) ? 1 : 0;
        LambdaUpdateWrapper<Storage> storageUpdate = new LambdaUpdateWrapper<>();
        storageUpdate.set(args.getIsEnabled() != null, Storage::getEnabled, enabled)
                .in(Storage::getId, args.getStorageIds());
        int count = storageMapper.update(storageUpdate);
        if (count != args.getStorageIds().size()) throw new BusinessException("");
    }

    public List<Storage> listAllStorageItems() {
        return storageMapper.selectList(null);
    }
}
