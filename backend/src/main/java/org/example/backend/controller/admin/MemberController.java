package org.example.backend.controller.admin;

import org.example.backend.common.Result;
import org.example.backend.model.entity.Member;
import org.example.backend.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @PostMapping("/create")
    public Result<?> createMember() {

    }

    @PostMapping("/delete")
    public Result<?> deleteMember() {

    }

    @PostMapping("/update")
    public Result<?> updateMember() {

    }

    @GetMapping("/all")
    public Result<?> listAllMember() {

    }

    @GetMapping("/role")
    public Result<?> listOrgRole() {

    }

    @GetMapping("/org")
    public Result<?> listOrg() {

    }
}
