package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.args.CreateMemberArgs;
import org.example.backend.model.args.DeleteMemberArgs;
import org.example.backend.model.args.UpdateMemberArgs;
import org.example.backend.model.view.MemberNodeView;
import org.example.backend.model.view.MemberRoleView;
import org.example.backend.model.view.MemberView;
import org.example.backend.model.view.RoleView;
import org.example.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/create")
    public Result<Void> createMember(CreateMemberArgs args) {

    }

    @PostMapping("/delete")
    public Result<Void> deleteMembers(DeleteMemberArgs args) {

    }

    @PostMapping("/update")
    public Result<Void> updateMember(UpdateMemberArgs args) {

    }

    @GetMapping("/all")
    public Result<List<MemberView>> listAllMembers() {

    }

    @GetMapping("/role")
    public Result<List<MemberRoleView>> listOrgRoles() {

    }

    @GetMapping("/org")
    public Result<List<MemberNodeView>> listOrgNodes() {

    }
}
