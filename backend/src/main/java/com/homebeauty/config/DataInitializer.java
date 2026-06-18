package com.homebeauty.config;

import com.homebeauty.entity.Artisan;
import com.homebeauty.mapper.ArtisanMapper;
import com.homebeauty.service.GeoLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private ArtisanMapper artisanMapper;

    @Resource
    private GeoLocationService geoLocationService;

    @Override
    public void run(String... args) {
        log.info("开始初始化手艺人位置信息到Redis GEO...");
        
        try {
            List<Artisan> artisans = artisanMapper.selectList(null);
            for (Artisan artisan : artisans) {
                if (artisan.getLongitude() != null && artisan.getLatitude() != null 
                        && artisan.getAuditStatus() != null && artisan.getAuditStatus() == 1
                        && artisan.getStatus() != null && artisan.getStatus() == 1) {
                    geoLocationService.addArtisanLocation(
                            artisan.getId(), 
                            artisan.getLongitude(), 
                            artisan.getLatitude()
                    );
                    log.info("同步手艺人位置: id={}, name={}, lng={}, lat={}", 
                            artisan.getId(), artisan.getRealName(), 
                            artisan.getLongitude(), artisan.getLatitude());
                }
            }
            log.info("手艺人位置信息初始化完成，共同步{}个手艺人", artisans.size());
        } catch (Exception e) {
            log.error("初始化手艺人位置信息失败", e);
        }
    }
}
