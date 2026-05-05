package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.file.EntryDeletionReq;
import org.example.backend.model.request.file.EntryRestoreReq;
import org.example.backend.model.response.file.RecycleResp;
import org.example.backend.service.RecycleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recycle/manage")
public class RecycleManageController {
    private final RecycleService recycleService;

    public RecycleManageController(RecycleService recycleService) {
        this.recycleService = recycleService;
    }

    /**
     * 获取回收站条目列表
     * @return 回收站文件/目录列表
     */
    @GetMapping
    public Result<?> listEntries() {
        List<RecycleResp> resp = recycleService.listAllEntries();
        return Result.success(resp);
    }

    /**
     * 批量还原回收站条目
     * @param req 还原请求参数
     * @return 统一响应结果
     */
    @PostMapping("/restore")
    public Result<?> restoreEntries(@RequestBody EntryRestoreReq req) {
        recycleService.restoreEntries(req);
        return Result.success();
    }

    /**
     * 批量彻底删除回收站条目
     * @param req 彻底删除请求参数
     * @return 统一响应结果
     */
    @PostMapping("/delete")
    public Result<?> deleteEntries(@RequestBody EntryDeletionReq req) {
        recycleService.deleteEntries(req);
        return Result.success();
    }
}
