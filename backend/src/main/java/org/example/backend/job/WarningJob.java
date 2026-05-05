package org.example.backend.job;

import org.example.backend.common.constant.DbConsts;
import org.example.backend.mapper.DriveMapper;
import org.example.backend.model.entity.Drive;
import org.example.backend.model.entity.Notice;
import org.example.backend.service.ConfigService;
import org.example.backend.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WarningJob {
    private final DriveMapper driveMapper;
    private final ConfigService configService;
    private final NoticeService noticeService;

    private static final Logger logger = LoggerFactory.getLogger(WarningJob.class);

    public WarningJob(DriveMapper driveMapper, ConfigService configService, NoticeService noticeService) {
        this.driveMapper = driveMapper;
        this.configService = configService;
        this.noticeService = noticeService;
    }

    @Scheduled(cron = "0 0 0/2 * * ?")
    public void storageWarning() {
        logger.info("定时任务启动：开始扫描到达存储上限的空间...");

        List<Drive> drives = driveMapper.selectList(null);
        double threshold = configService.getStorageWarningThreshold();
        List<Drive> warnings = drives.stream()
                .filter(drive -> drive.getUsedQuota() >= drive.getTotalQuota() * threshold)
                .toList();
        if (warnings.isEmpty()) {
            logger.info("定时任务完成：没有需要预警的存储空间");
            return;
        }

        try {
            List<Notice> notices = warnings.stream()
                    .map(warning -> {
                        return Notice.builder()
                                .title("空间预警")
                                .content("您的存储空间即将达到存储上限。请及时清理文件")
                                .type(2)
                                .targetId(warning.getUserId())
                                .isDeleted(DbConsts.DELETED_NO)
                                .isRead(DbConsts.READ_NO)
                                .build();
                    })
                    .toList();
            noticeService.createNotices(notices);
        } catch (Exception e) {
            logger.error("定时任务：存储空间预警失败", e);
        }

        logger.info("定时任务完成：预警结束。");
    }
}