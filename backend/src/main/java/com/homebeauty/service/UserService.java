package com.homebeauty.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.homebeauty.dto.LoginRequest;
import com.homebeauty.dto.RegisterRequest;
import com.homebeauty.entity.Artisan;
import com.homebeauty.entity.Favorite;
import com.homebeauty.entity.User;
import com.homebeauty.mapper.ArtisanMapper;
import com.homebeauty.mapper.FavoriteMapper;
import com.homebeauty.mapper.UserMapper;
import com.homebeauty.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ArtisanMapper artisanMapper;

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> register(RegisterRequest request) {
        log.debug("用户注册: phone={}", request.getPhone());

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        User existingUser = userMapper.selectOne(wrapper);
        if (existingUser != null) {
            log.error("手机号已注册: phone={}", request.getPhone());
            throw new RuntimeException("该手机号已注册");
        }

        User user = new User();
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setStatus(1);
        userMapper.insert(user);

        String token = JwtUtil.generateToken(user.getId(), "user");

        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        log.info("用户注册成功: userId={}", user.getId());
        return result;
    }

    public Map<String, Object> login(LoginRequest request) {
        log.debug("用户登录: phone={}", request.getPhone());

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("phone", request.getPhone());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            log.error("用户不存在: phone={}", request.getPhone());
            throw new RuntimeException("账号或密码错误");
        }
        if (user.getStatus() != 1) {
            log.error("用户账号已禁用: userId={}", user.getId());
            throw new RuntimeException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("密码错误: userId={}", user.getId());
            throw new RuntimeException("账号或密码错误");
        }

        String token = JwtUtil.generateToken(user.getId(), "user");

        user.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("token", token);

        log.info("用户登录成功: userId={}", user.getId());
        return result;
    }

    public User getUserById(Long id) {
        log.debug("获取用户信息: id={}", id);
        return userMapper.selectById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public User updateUser(User user) {
        log.debug("更新用户信息: userId={}", user.getId());
        userMapper.updateById(user);
        log.info("用户信息更新成功: userId={}", user.getId());
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void favoriteArtisan(Long userId, Long artisanId) {
        log.debug("用户收藏手艺人: userId={}, artisanId={}", userId, artisanId);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan == null) {
            log.error("手艺人不存在: artisanId={}", artisanId);
            throw new RuntimeException("手艺人不存在");
        }

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("artisan_id", artisanId);
        Favorite existingFavorite = favoriteMapper.selectOne(wrapper);
        if (existingFavorite != null) {
            log.warn("已收藏该手艺人: userId={}, artisanId={}", userId, artisanId);
            return;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setArtisanId(artisanId);
        favoriteMapper.insert(favorite);

        log.info("收藏成功: userId={}, artisanId={}", userId, artisanId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void unfavoriteArtisan(Long userId, Long artisanId) {
        log.debug("用户取消收藏手艺人: userId={}, artisanId={}", userId, artisanId);

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("artisan_id", artisanId);
        favoriteMapper.delete(wrapper);

        log.info("取消收藏成功: userId={}, artisanId={}", userId, artisanId);
    }

    public List<Artisan> getFavoriteArtisans(Long userId) {
        log.debug("获取用户收藏列表: userId={}", userId);

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        List<Artisan> artisans = new ArrayList<>();
        if (favorites != null && !favorites.isEmpty()) {
            for (Favorite favorite : favorites) {
                Artisan artisan = artisanMapper.selectById(favorite.getArtisanId());
                if (artisan != null) {
                    artisans.add(artisan);
                }
            }
        }

        log.debug("获取收藏列表成功，共{}个手艺人", artisans.size());
        return artisans;
    }
}
