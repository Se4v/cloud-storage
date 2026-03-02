package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.common.security.GlobalUserDetails;
import org.example.backend.model.args.CreateNodeArgs;
import org.example.backend.model.args.UpdateNodeArgs;
import org.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgController {
    @Autowired
    private OrgService orgService;

    @PostMapping("/create")
    public Result<Void> createNode(@RequestBody CreateNodeArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        orgService.createNode(args, userDetails.getUserId());
        return Result.success();
    }

    @PostMapping("/delete")
    public Result<Void> deleteNodes(@RequestBody List<Long> nodeIds) {
        orgService.deleteNodes(nodeIds);
        return Result.success();
    }

    @PostMapping("/update")
    public Result<Void> updateNode(@RequestBody UpdateNodeArgs args) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        GlobalUserDetails userDetails = (GlobalUserDetails) auth.getPrincipal();

        orgService.updateNode(args, userDetails.getUserId());

        return Result.success();
    }

    @GetMapping("/all")
    public Result<Void> listAllNodes() {
        return null;
    }

    public Result<Void> assignOrgRole() {
        return null;
    }

}
