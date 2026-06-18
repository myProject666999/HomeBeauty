package com.homebeauty.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.homebeauty.entity.ArtisanSkill;
import com.homebeauty.entity.ServiceCategory;
import com.homebeauty.entity.ServiceItem;
import com.homebeauty.mapper.ArtisanSkillMapper;
import com.homebeauty.mapper.ServiceCategoryMapper;
import com.homebeauty.mapper.ServiceItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class ServiceItemService {

    @Resource
    private ServiceCategoryMapper serviceCategoryMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Resource
    private ArtisanSkillMapper artisanSkillMapper;

    public List<ServiceCategory> getCategoryList() {
        log.debug("获取服务分类列表");

        QueryWrapper<ServiceCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByAsc("sort");
        List<ServiceCategory> categories = serviceCategoryMapper.selectList(wrapper);

        log.debug("获取服务分类列表成功，共{}个分类", categories != null ? categories.size() : 0);
        return categories;
    }

    public List<ServiceItem> getServiceItemList(Long categoryId) {
        log.debug("获取服务项目列表: categoryId={}", categoryId);

        QueryWrapper<ServiceItem> wrapper = new QueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        wrapper.eq("status", 1).orderByAsc("sort");
        List<ServiceItem> items = serviceItemMapper.selectList(wrapper);

        log.debug("获取服务项目列表成功，共{}个项目", items != null ? items.size() : 0);
        return items;
    }

    public ServiceItem getServiceItemDetail(Long id) {
        log.debug("获取服务项目详情: id={}", id);

        ServiceItem serviceItem = serviceItemMapper.selectById(id);

        log.debug("获取服务项目详情成功: {}", serviceItem != null ? serviceItem.getName() : null);
        return serviceItem;
    }

    public List<ArtisanSkill> getArtisanSkills(Long artisanId) {
        log.debug("获取手艺人技能列表: artisanId={}", artisanId);

        QueryWrapper<ArtisanSkill> wrapper = new QueryWrapper<>();
        wrapper.eq("artisan_id", artisanId).eq("status", 1);
        List<ArtisanSkill> skills = artisanSkillMapper.selectList(wrapper);

        log.debug("获取手艺人技能列表成功，共{}个技能", skills != null ? skills.size() : 0);
        return skills;
    }
}
