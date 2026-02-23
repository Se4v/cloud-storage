package org.example.backend.controller.user;

import org.example.backend.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/drive")
public class DriveController {

    @GetMapping("/usage")
    public Result<?> getPersonalDriveUsage() {
        return null;
    }
}
