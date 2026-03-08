package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.result.RecycleDetailResult;
import org.example.backend.model.view.RecycleView;
import org.example.backend.service.RecycleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/recycle")
public class RecycleController {
    @Autowired
    private RecycleService recycleService;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Result<List<RecycleView>> listEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        List<RecycleDetailResult> results = recycleService.listEntries(userDetails.getUserId());

        List<RecycleView> recycleViews = results.stream()
                .map(result -> {
                    RecycleView view = new RecycleView();

                    view.setId(String.valueOf(result.getEntryId()));
                    view.setName(result.getEntryName());
                    view.setType(result.getEntryType() == 1 ? "file" : "folder");
                    view.setPath(result.getDriveType() == 1 ? "个人空间/项目文件" : "企业空间/" + result.getDriveName());
                    view.setDeleteTime(result.getDeletedAt().format(formatter));
                    view.setExpireTime(result.getExpiredAt().format(formatter));
                    view.setSize(String.valueOf(result.getFileSize()));

                    return view;
                })
                .toList();

        return Result.success("", recycleViews);
    }


    public Result<Void> restoreEntries(@RequestBody List<Long> ids) {
        recycleService.restoreEntries(ids);
        return Result.success();
    }

    public Result<Void> deleteEntries(@RequestBody List<Long> ids) {
        recycleService.permanentlyDeleteEntries(ids);
        return Result.success();
    }

    public Result<Void> clearEntries() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        recycleService.clearEntries(userDetails.getUserId());

        return Result.success();
    }
}
