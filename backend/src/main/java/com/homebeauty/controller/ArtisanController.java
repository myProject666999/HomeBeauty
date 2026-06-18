package com.homebeauty.controller;

import com.homebeauty.common.Result;
import com.homebeauty.dto.LoginRequest;
import com.homebeauty.dto.RegisterRequest;
import com.homebeauty.entity.Artisan;
import com.homebeauty.entity.ArtisanSkill;
import com.homebeauty.entity.ArtisanWorkSlot;
import com.homebeauty.service.ArtisanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/artisan")
public class ArtisanController {

    @Resource
    private ArtisanService artisanService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        log.debug("手艺人注册接口被调用: phone={}", request.getPhone());
        try {
            Map<String, Object> result = artisanService.register(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("手艺人注册失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        log.debug("手艺人登录接口被调用: phone={}", request.getPhone());
        try {
            Map<String, Object> result = artisanService.login(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("手艺人登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Artisan> getArtisanById(@PathVariable Long id) {
        log.debug("获取手艺人详情接口被调用: id={}", id);
        try {
            Artisan artisan = artisanService.getArtisanById(id);
            return Result.success(artisan);
        } catch (Exception e) {
            log.error("获取手艺人详情失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/status")
    public Result<Void> updateWorkStatus(@RequestParam Long artisanId,
                                         @RequestParam Integer status,
                                         @RequestParam BigDecimal longitude,
                                         @RequestParam BigDecimal latitude) {
        log.debug("更新手艺人工作状态和位置接口被调用: artisanId={}, status={}", artisanId, status);
        try {
            artisanService.updateWorkStatus(artisanId, status, longitude, latitude);
            return Result.success();
        } catch (Exception e) {
            log.error("更新手艺人工作状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/audit")
    public Result<Void> auditArtisan(@RequestParam Long artisanId,
                                     @RequestParam Integer auditStatus,
                                     @RequestParam(required = false) String remark) {
        log.debug("审核手艺人接口被调用: artisanId={}, auditStatus={}", artisanId, auditStatus);
        try {
            artisanService.auditArtisan(artisanId, auditStatus, remark);
            return Result.success();
        } catch (Exception e) {
            log.error("审核手艺人失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/skills")
    public Result<Void> updateSkills(@RequestParam Long artisanId,
                                     @RequestBody List<ArtisanSkill> skills) {
        log.debug("更新手艺人技能接口被调用: artisanId={}, skillCount={}", artisanId, skills != null ? skills.size() : 0);
        try {
            artisanService.updateSkill(artisanId, skills);
            return Result.success();
        } catch (Exception e) {
            log.error("更新手艺人技能失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/nearby")
    public Result<List<Artisan>> getNearbyArtisans(@RequestParam BigDecimal longitude,
                                                   @RequestParam BigDecimal latitude,
                                                   @RequestParam(required = false, defaultValue = "10") Double radius) {
        log.debug("附近手艺人列表接口被调用: longitude={}, latitude={}, radius={}", longitude, latitude, radius);
        try {
            List<Artisan> artisans = artisanService.getNearbyArtisans(longitude, latitude, radius);
            return Result.success(artisans);
        } catch (Exception e) {
            log.error("获取附近手艺人列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/workslots")
    public Result<Void> updateWorkSlots(@RequestParam Long artisanId,
                                        @RequestBody List<ArtisanWorkSlot> slots) {
        log.debug("更新手艺人工作时段接口被调用: artisanId={}, slotCount={}", artisanId, slots != null ? slots.size() : 0);
        try {
            artisanService.updateWorkSlot(artisanId, slots);
            return Result.success();
        } catch (Exception e) {
            log.error("更新手艺人工作时段失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
