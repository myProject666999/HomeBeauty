package com.homebeauty.controller;

import com.homebeauty.common.Result;
import com.homebeauty.dto.LoginRequest;
import com.homebeauty.dto.RegisterRequest;
import com.homebeauty.entity.Artisan;
import com.homebeauty.entity.User;
import com.homebeauty.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        log.debug("用户注册接口被调用: phone={}", request.getPhone());
        try {
            Map<String, Object> result = userService.register(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户注册失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        log.debug("用户登录接口被调用: phone={}", request.getPhone());
        try {
            Map<String, Object> result = userService.login(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        log.debug("获取用户信息接口被调用: id={}", id);
        try {
            User user = userService.getUserById(id);
            return Result.success(user);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/update")
    public Result<User> updateUser(@RequestBody User user) {
        log.debug("更新用户信息接口被调用: userId={}", user.getId());
        try {
            User updatedUser = userService.updateUser(user);
            return Result.success(updatedUser);
        } catch (Exception e) {
            log.error("更新用户信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/favorite")
    public Result<Void> favoriteArtisan(@RequestParam Long userId, @RequestParam Long artisanId) {
        log.debug("收藏手艺人接口被调用: userId={}, artisanId={}", userId, artisanId);
        try {
            userService.favoriteArtisan(userId, artisanId);
            return Result.success();
        } catch (Exception e) {
            log.error("收藏手艺人失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/favorite")
    public Result<Void> unfavoriteArtisan(@RequestParam Long userId, @RequestParam Long artisanId) {
        log.debug("取消收藏手艺人接口被调用: userId={}, artisanId={}", userId, artisanId);
        try {
            userService.unfavoriteArtisan(userId, artisanId);
            return Result.success();
        } catch (Exception e) {
            log.error("取消收藏手艺人失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/favorites")
    public Result<List<Artisan>> getFavoriteArtisans(@RequestParam Long userId) {
        log.debug("获取收藏列表接口被调用: userId={}", userId);
        try {
            List<Artisan> artisans = userService.getFavoriteArtisans(userId);
            return Result.success(artisans);
        } catch (Exception e) {
            log.error("获取收藏列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
