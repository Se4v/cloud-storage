package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateNodeArgs;
import org.example.backend.model.args.UpdateNodeArgs;
import org.example.backend.service.OrgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgController {
    @Autowired
    private OrgService orgService;

    @PostMapping("/create")
    public Result<Void> createNode(@RequestBody CreateNodeArgs args) {
        return null;
    }

    @PostMapping("/delete")
    public Result<Void> deleteNodes(@RequestBody List<String> nodeIds) {
        return null;
    }

    @PostMapping("/update")
    public Result<Void> updateNode(@RequestBody UpdateNodeArgs args) {
        return null;
    }

    @GetMapping("/all")
    public Result<Void> listAllNodes() {
        return null;
    }

    public Result<Void> assignOrgRole() {
        return null;
    }

}
