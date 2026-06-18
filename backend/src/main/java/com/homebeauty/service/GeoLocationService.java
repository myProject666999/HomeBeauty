package com.homebeauty.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GeoLocationService {

    private static final String ARTISAN_GEO_KEY = "homebeauty:artisan:geo";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public void addArtisanLocation(Long artisanId, BigDecimal longitude, BigDecimal latitude) {
        log.debug("添加手艺人位置: artisanId={}, longitude={}, latitude={}", artisanId, longitude, latitude);
        redisTemplate.opsForGeo().add(ARTISAN_GEO_KEY,
                new org.springframework.data.geo.Point(longitude.doubleValue(), latitude.doubleValue()),
                artisanId.toString());
    }

    public void removeArtisanLocation(Long artisanId) {
        log.debug("移除手艺人位置: artisanId={}", artisanId);
        redisTemplate.opsForGeo().remove(ARTISAN_GEO_KEY, artisanId.toString());
    }

    public void updateArtisanLocation(Long artisanId, BigDecimal longitude, BigDecimal latitude) {
        log.debug("更新手艺人位置: artisanId={}, longitude={}, latitude={}", artisanId, longitude, latitude);
        removeArtisanLocation(artisanId);
        addArtisanLocation(artisanId, longitude, latitude);
    }

    public List<Long> findNearbyArtisans(BigDecimal longitude, BigDecimal latitude, double radiusKm) {
        log.debug("查询附近手艺人: longitude={}, latitude={}, radius={}km", longitude, latitude, radiusKm);
        
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<Object>> results = redisTemplate.opsForGeo().radius(
                ARTISAN_GEO_KEY,
                new org.springframework.data.geo.Point(longitude.doubleValue(), latitude.doubleValue()),
                new org.springframework.data.geo.Distance(radiusKm, org.springframework.data.geo.Metrics.KILOMETERS),
                args
        );

        List<Long> artisanIds = new ArrayList<>();
        if (results != null && results.getContent() != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<Object>> result : results.getContent()) {
                RedisGeoCommands.GeoLocation<Object> location = result.getContent();
                Long artisanId = Long.parseLong(location.getName().toString());
                artisanIds.add(artisanId);
                log.debug("找到附近手艺人: artisanId={}, 距离={}m", artisanId, 
                        result.getDistance().getValue() * 1000);
            }
        }
        
        log.debug("附近手艺人查询完成，共找到{}个手艺人", artisanIds.size());
        return artisanIds;
    }

    public Double getDistance(BigDecimal lon1, BigDecimal lat1, BigDecimal lon2, BigDecimal lat2) {
        List<String> points = new ArrayList<>();
        points.add("user");
        points.add("artisan");
        
        redisTemplate.opsForGeo().add("homebeauty:temp:geo", 
                new org.springframework.data.geo.Point(lon1.doubleValue(), lat1.doubleValue()), "user");
        redisTemplate.opsForGeo().add("homebeauty:temp:geo", 
                new org.springframework.data.geo.Point(lon2.doubleValue(), lat2.doubleValue()), "artisan");
        
        org.springframework.data.geo.Distance distance = redisTemplate.opsForGeo().distance(
                "homebeauty:temp:geo", "user", "artisan", org.springframework.data.geo.Metrics.KILOMETERS);
        
        redisTemplate.delete("homebeauty:temp:geo");
        
        return distance != null ? distance.getValue() : null;
    }
}
