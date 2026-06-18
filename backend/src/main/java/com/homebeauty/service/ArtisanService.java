package com.homebeauty.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.homebeauty.dto.LoginRequest;
import com.homebeauty.dto.RegisterRequest;
import com.homebeauty.entity.Artisan;
import com.homebeauty.entity.ArtisanSkill;
import com.homebeauty.entity.ArtisanWorkSlot;
import com.homebeauty.mapper.ArtisanMapper;
import com.homebeauty.mapper.ArtisanSkillMapper;
import com.homebeauty.mapper.ArtisanWorkSlotMapper;
import com.homebeauty.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ArtisanService {

    @Resource
    private ArtisanMapper artisanMapper;

    @Resource
    private ArtisanSkillMapper artisanSkillMapper;

    @Resource
    private ArtisanWorkSlotMapper artisanWorkSlotMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private GeoLocationService geoLocationService;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest request) {
        log.debug("手艺人注册: phone={}", request.getPhone());

        QueryWrapper<Artisan> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        Artisan existingArtisan = artisanMapper.selectOne(wrapper);
        if (existingArtisan != null) {
            log.error("手机号已注册: phone={}", request.getPhone());
            throw new RuntimeException("该手机号已注册");
        }

        Artisan artisan = new Artisan();
        artisan.setPhone(request.getPhone());
        artisan.setPassword(passwordEncoder.encode(request.getPassword()));
        artisan.setRealName(request.getRealName());
        artisan.setIdCard(request.getIdCard());
        artisan.setAuditStatus(0);
        artisan.setWorkStatus(0);
        artisan.setStatus(1);
        artisan.setRating(BigDecimal.ZERO);
        artisan.setOrderCount(0);
        artisanMapper.insert(artisan);

        String token = JwtUtil.generateToken(artisan.getId(), "artisan");

        Map<String, Object> result = new HashMap<>();
        result.put("artisan", artisan);
        result.put("token", token);

        log.info("手艺人注册成功，等待审核: artisanId={}", artisan.getId());
        return result;
    }

    public Map<String, Object> login(LoginRequest request) {
        log.debug("手艺人登录: phone={}", request.getPhone());

        QueryWrapper<Artisan> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        Artisan artisan = artisanMapper.selectOne(wrapper);
        if (artisan == null) {
            log.error("手艺人不存在: phone={}", request.getPhone());
            throw new RuntimeException("账号或密码错误");
        }
        if (artisan.getStatus() != 1) {
            log.error("手艺人账号已禁用: artisanId={}", artisan.getId());
            throw new RuntimeException("账号已被禁用");
        }
        if (artisan.getAuditStatus() != 1) {
            log.error("手艺人账号未审核通过: artisanId={}, auditStatus={}", artisan.getId(), artisan.getAuditStatus());
            throw new RuntimeException("账号审核未通过，请等待审核");
        }
        if (!passwordEncoder.matches(request.getPassword(), artisan.getPassword())) {
            log.error("密码错误: artisanId={}", artisan.getId());
            throw new RuntimeException("账号或密码错误");
        }

        String token = JwtUtil.generateToken(artisan.getId(), "artisan");

        Map<String, Object> result = new HashMap<>();
        result.put("artisan", artisan);
        result.put("token", token);

        log.info("手艺人登录成功: artisanId={}", artisan.getId());
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWorkStatus(Long artisanId, Integer status, BigDecimal longitude, BigDecimal latitude) {
        log.debug("更新手艺人工作状态和位置: artisanId={}, status={}, longitude={}, latitude={}",
                artisanId, status, longitude, latitude);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan == null) {
            log.error("手艺人不存在: artisanId={}", artisanId);
            throw new RuntimeException("手艺人不存在");
        }

        artisan.setWorkStatus(status);
        artisan.setLongitude(longitude);
        artisan.setLatitude(latitude);
        artisanMapper.updateById(artisan);

        if (status == 1) {
            geoLocationService.addArtisanLocation(artisanId, longitude, latitude);
            log.debug("手艺人上线，位置已同步到Redis GEO: artisanId={}", artisanId);
        } else {
            geoLocationService.removeArtisanLocation(artisanId);
            log.debug("手艺人下线，位置已从Redis GEO移除: artisanId={}", artisanId);
        }

        log.info("手艺人工作状态更新成功: artisanId={}, status={}", artisanId, status);
    }

    @Transactional(rollbackFor = Exception.class)
    public void auditArtisan(Long artisanId, Integer auditStatus, String remark) {
        log.debug("审核手艺人: artisanId={}, auditStatus={}, remark={}", artisanId, auditStatus, remark);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan == null) {
            log.error("手艺人不存在: artisanId={}", artisanId);
            throw new RuntimeException("手艺人不存在");
        }

        artisan.setAuditStatus(auditStatus);
        artisan.setAuditRemark(remark);
        artisanMapper.updateById(artisan);

        log.info("手艺人审核完成: artisanId={}, auditStatus={}", artisanId, auditStatus);
    }

    public Artisan getArtisanById(Long id) {
        log.debug("获取手艺人详情: id={}", id);
        return artisanMapper.selectById(id);
    }

    public List<Artisan> getArtisanList(List<Long> ids) {
        log.debug("批量获取手艺人: ids={}", ids);
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        QueryWrapper<Artisan> wrapper = new QueryWrapper<>();
        wrapper.in("id", ids);
        return artisanMapper.selectList(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateSkill(Long artisanId, List<ArtisanSkill> skills) {
        log.debug("更新手艺人技能: artisanId={}, skillCount={}", artisanId, skills != null ? skills.size() : 0);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan == null) {
            log.error("手艺人不存在: artisanId={}", artisanId);
            throw new RuntimeException("手艺人不存在");
        }

        QueryWrapper<ArtisanSkill> wrapper = new QueryWrapper<>();
        wrapper.eq("artisan_id", artisanId);
        artisanSkillMapper.delete(wrapper);

        if (skills != null && !skills.isEmpty()) {
            for (ArtisanSkill skill : skills) {
                skill.setArtisanId(artisanId);
                artisanSkillMapper.insert(skill);
            }
        }

        log.info("手艺人技能更新成功: artisanId={}", artisanId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateWorkSlot(Long artisanId, List<ArtisanWorkSlot> slots) {
        log.debug("更新手艺人工作时段: artisanId={}, slotCount={}", artisanId, slots != null ? slots.size() : 0);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan == null) {
            log.error("手艺人不存在: artisanId={}", artisanId);
            throw new RuntimeException("手艺人不存在");
        }

        QueryWrapper<ArtisanWorkSlot> wrapper = new QueryWrapper<>();
        wrapper.eq("artisan_id", artisanId);
        artisanWorkSlotMapper.delete(wrapper);

        if (slots != null && !slots.isEmpty()) {
            for (ArtisanWorkSlot slot : slots) {
                slot.setArtisanId(artisanId);
                artisanWorkSlotMapper.insert(slot);
            }
        }

        log.info("手艺人工作时段更新成功: artisanId={}", artisanId);
    }

    public List<Artisan> getNearbyArtisans(BigDecimal longitude, BigDecimal latitude, Double radius) {
        log.debug("获取附近手艺人列表: longitude={}, latitude={}, radius={}km", longitude, latitude, radius);

        double radiusKm = radius != null ? radius : 10.0;
        List<Long> artisanIds = geoLocationService.findNearbyArtisans(longitude, latitude, radiusKm);

        List<Artisan> artisans = new java.util.ArrayList<>();
        if (artisanIds != null && !artisanIds.isEmpty()) {
            for (Long artisanId : artisanIds) {
                Artisan artisan = artisanMapper.selectById(artisanId);
                if (artisan != null && artisan.getAuditStatus() == 1 && artisan.getStatus() == 1) {
                    artisans.add(artisan);
                }
            }
        }

        log.debug("查询到附近{}个手艺人", artisans.size());
        return artisans;
    }
}
