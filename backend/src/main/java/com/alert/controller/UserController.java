package com.alert.controller;

import com.alert.aop.OperationLog;
import com.alert.common.Result;
import com.alert.entity.User;
import com.alert.security.UserPrincipal;
import com.alert.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/me")
    public Result<User> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = userService.getById(principal.getUserId());
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<User>> list(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageList(page, size, keyword));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("添加用户")
    public Result<User> create(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("编辑用户")
    public Result<?> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        user.setPassword(null); // 不更新密码
        userService.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("删除用户")
    public Result<?> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("修改用户状态")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @OperationLog("修改用户角色")
    public Result<?> updateRole(@PathVariable Long id, @RequestParam String role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        userService.updateById(user);
        return Result.success();
    }
}
