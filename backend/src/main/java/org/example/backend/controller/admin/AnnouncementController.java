package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateAnnouncementArgs;
import org.example.backend.model.args.DeleteAnnouncementArgs;
import org.example.backend.model.args.UpdateAnnouncementArgs;
import org.example.backend.model.entity.Notice;
import org.example.backend.model.view.AnnouncementView;
import org.example.backend.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {
    @Autowired
    private AnnouncementService announcementService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostMapping("/create")
    public Result<Void> createAnnouncement(@RequestBody CreateAnnouncementArgs args) {
        announcementService.createAnnouncement(args);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteAnnouncements(@RequestBody DeleteAnnouncementArgs args) {
        announcementService.deleteAnnouncement(args);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateAnnouncement(@RequestBody UpdateAnnouncementArgs args) {
        announcementService.updateAnnouncement(args);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<List<AnnouncementView>> listAllAnnouncements() {
        List<Notice> announcementList = announcementService.listAllAnnouncement();

        List<AnnouncementView> announcementViews = announcementList.stream()
                .map(announcement -> AnnouncementView.builder()
                        .id(announcement.getId())
                        .title(announcement.getTitle())
                        .content(announcement.getContent())
                        .expiredTime(announcement.getExpiredAt().format(formatter))
                        .build())
                .toList();

        return Result.success("", announcementViews);
    }
}
