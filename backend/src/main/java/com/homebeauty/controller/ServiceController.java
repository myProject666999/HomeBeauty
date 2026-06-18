package com.homebeauty.controller;

import com.homebeauty.common.Result;
import com.homebeauty.entity.ArtisanSkill;
import com.homebeauty.entity.ServiceCategory;
import com.homebeauty.entity.ServiceItem;
import com.homebeauty.service.ServiceItemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/service")
public class ServiceController {

    @Resource
    private ServiceItemService serviceItemService;

    @GetMapping("/categories")
    public Result<List<ServiceCategory>> getCategories() {
        log.debug("获取服务分类接口被调用");
        try {
            List<ServiceCategory> categories = serviceItemService.getCategoryList();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取服务分类失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/items")
    public Result<List<ServiceItem>> getServiceItems(@RequestParam(required = false) Long categoryId) {
        log.debug("获取服务项目列表接口被调用: categoryId={}", categoryId);
        try {
            List<ServiceItem> items = serviceItemService.getServiceItemList(categoryId);
            return Result.success(items);
        } catch (Exception e) {
            log.error("获取服务项目列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/item/{id}")
    public Result<ServiceItem> getServiceItemDetail(@PathVariable Long id) {
        log.debug("获取服务项目详情接口被调用: id={}", id);
        try {
            ServiceItem item = serviceItemService.getServiceItemDetail(id);
            return Result.success(item);
        } catch (Exception e) {
            log.error("获取服务项目详情失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/artisan/{artisanId}/skills")
    public Result<List<ArtisanSkill>> getArtisanSkills(@PathVariable Long artisanId) {
        log.debug("获取手艺人技能接口被调用: artisanId={}", artisanId);
        try {
            List<ArtisanSkill> skills = serviceItemService.getArtisanSkills(artisanId);
            return Result.success(skills);
        } catch (Exception e) {
            log.error("获取手艺人技能失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
