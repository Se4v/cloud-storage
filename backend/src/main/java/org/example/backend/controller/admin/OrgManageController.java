package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.request.CreateNodeArgs;
import org.example.backend.model.request.DeleteNodeArgs;
import org.example.backend.model.request.UpdateNodeArgs;
import org.example.backend.model.response.NodeView;
import org.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgManageController {
    @Autowired
    private OrgService orgService;

    @PostMapping("/create")
    public Result<Void> createOrgNode(@RequestBody CreateNodeArgs args) {
        orgService.createOrgNode(args);
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteOrgNodes(@RequestBody DeleteNodeArgs args) {
        orgService.deleteOrgNodes(args);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateOrgNode(@RequestBody UpdateNodeArgs args) {
        orgService.updateOrgNode(args);
        return Result.success();
    }

    @GetMapping("/all")
    public Result<List<NodeView>> listAllOrgNodes() {
        List<NodeView> nodeViewList = orgService.listAllOrgNodes();
        return Result.success(nodeViewList);
    }

}
