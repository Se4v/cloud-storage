package org.example.backend.controller.admin;

import org.example.backend.common.result.Result;
import org.example.backend.model.request.notice.NodeCreationReq;
import org.example.backend.model.request.org.OrgNodeDeletionReq;
import org.example.backend.model.request.org.OrgNodeUpdateReq;
import org.example.backend.model.response.NodeView;
import org.example.backend.service.OrgService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgManageController {
    private final OrgService orgService;

    public OrgManageController(OrgService orgService) {
        this.orgService = orgService;
    }

    @PostMapping("/create")
    public Result<?> createOrgNode(@RequestBody NodeCreationReq req) {
        orgService.createOrgNode(req);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<?> deleteOrgNodes(@RequestBody OrgNodeDeletionReq req) {
        orgService.deleteOrgNodes(req);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<?> updateOrgNode(@RequestBody OrgNodeUpdateReq req) {
        orgService.updateOrgNode(req);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<?> listAllOrgNodes() {
        List<NodeView> resp = orgService.listAllOrgNodes();
        return Result.success(resp);
    }
}
