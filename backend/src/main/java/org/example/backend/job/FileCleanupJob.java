package org.example.backend.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.mapper.EntryMapper;
import org.example.backend.mapper.StorageMapper;
import org.example.backend.model.entity.Entry;
import org.example.backend.model.entity.Storage;
import org.example.backend.service.EntryService;
import org.example.backend.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
public class FileCleanupJob {
    private final EntryMapper entryMapper;
    private final StorageMapper storageMapper;
    private final EntryService entryService;
    private final StorageService storageService;

    public FileCleanupJob(EntryMapper entryMapper, EntryService entryService,
                          StorageMapper storageMapper, StorageService storageService) {
        this.entryMapper = entryMapper;
        this.storageMapper = storageMapper;
        this.entryService = entryService;
        this.storageService = storageService;
    }

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupJob.class);

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanPermanentlyDeletedEntry() {
        logger.info("定时任务启动：开始清理永久删除的文件记录...");

        List<Entry> entries = entryMapper.selectList(
                Wrappers.<Entry>lambdaQuery().eq(Entry::getStatus, 3));

        if (CollectionUtils.isEmpty(entries)) {
            logger.info("定时任务完成：没有需要清理的记录。");
            return;
        }

        logger.info("定时任务：共发现 {} 条待清理的记录。", entries.size());

        try {
            List<Long> entryIds = entries.stream().map(Entry::getId).toList();
            entryService.permanentlyDeletedEntry(entryIds);
        } catch (Exception e) {
            logger.error("定时任务：清理文件失败", e);
        }

        logger.info("定时任务完成：本轮清理结束。");
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void markOverdueEntryAsPermanent() {

    }

    @Scheduled(cron = "0 0 4 * * ?")
    public void cleanStorageFile() {
        logger.info("定时任务启动：开始清理未使用物理文件...");

        List<Storage> storages = storageMapper.selectList(
                Wrappers.<Storage>lambdaQuery()
                        .eq(Storage::getRefCount, 0));

        if (CollectionUtils.isEmpty(storages)) {
            logger.info("定时任务完成：没有需要清理物理文件。");
            return;
        }

        logger.info("定时任务：共发现 {} 条待清理的记录。", storages.size());

        try {
            storageService.cleanStorageFile(storages);
        } catch (Exception e) {
            logger.error("定时任务：清理物理文件失败", e);
        }

        logger.info("定时任务完成：本轮清理结束。");
    }
}
