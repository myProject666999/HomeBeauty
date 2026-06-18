package com.homebeauty.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.homebeauty.dto.CreateOrderRequest;
import com.homebeauty.entity.*;
import com.homebeauty.mapper.*;
import com.homebeauty.util.OrderNoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderLogMapper orderLogMapper;

    @Resource
    private ArtisanMapper artisanMapper;

    @Resource
    private ArtisanSkillMapper artisanSkillMapper;

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Resource
    private ReviewMapper reviewMapper;

    @Resource
    private GeoLocationService geoLocationService;

    @Resource
    private BookingSlotService bookingSlotService;

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(CreateOrderRequest request) {
        log.debug("开始创建订单: userId={}, serviceItemId={}", request.getUserId(), request.getServiceItemId());

        ServiceItem serviceItem = serviceItemMapper.selectById(request.getServiceItemId());
        if (serviceItem == null || serviceItem.getStatus() != 1) {
            log.error("服务项目不存在或已下架: serviceItemId={}", request.getServiceItemId());
            throw new RuntimeException("服务项目不存在或已下架");
        }

        Long artisanId = request.getArtisanId();
        if (artisanId != null) {
            log.debug("用户指定手艺人: artisanId={}", artisanId);
            Artisan artisan = artisanMapper.selectById(artisanId);
            if (artisan == null || artisan.getAuditStatus() != 1 || artisan.getStatus() != 1) {
                log.error("指定的手艺人不存在或未审核通过: artisanId={}", artisanId);
                throw new RuntimeException("指定的手艺人不存在或未审核通过");
            }

            QueryWrapper<ArtisanSkill> skillWrapper = new QueryWrapper<>();
            skillWrapper.eq("artisan_id", artisanId)
                    .eq("service_item_id", request.getServiceItemId())
                    .eq("status", 1);
            ArtisanSkill skill = artisanSkillMapper.selectOne(skillWrapper);
            if (skill == null) {
                log.error("手艺人没有该服务技能: artisanId={}, serviceItemId={}", artisanId, request.getServiceItemId());
                throw new RuntimeException("手艺人没有该服务技能");
            }

            if (!bookingSlotService.isSlotAvailable(artisanId, request.getAppointmentDate(), request.getAppointmentTime())) {
                log.error("手艺人该时段已被预约: artisanId={}, time={}", artisanId, request.getAppointmentTime());
                throw new RuntimeException("手艺人该时段已被预约");
            }
        } else {
            log.debug("用户未指定手艺人，系统自动匹配");
            artisanId = findNearbyAvailableArtisan(
                    request.getServiceItemId(),
                    request.getLongitude(),
                    request.getLatitude(),
                    request.getAppointmentDate(),
                    request.getAppointmentTime()
            );
            if (artisanId == null) {
                log.error("附近没有可用的手艺人");
                throw new RuntimeException("附近没有可用的手艺人，请稍后再试");
            }
            log.debug("系统匹配到手艺人: artisanId={}", artisanId);
        }

        boolean reserved = bookingSlotService.reserveSlot(
                artisanId,
                request.getAppointmentDate(),
                request.getAppointmentTime(),
                null,
                request.getUserId()
        );
        if (!reserved) {
            log.error("时段锁定失败，可能已被占用");
            throw new RuntimeException("时段锁定失败，请重新选择时间");
        }

        QueryWrapper<ArtisanSkill> skillWrapper = new QueryWrapper<>();
        skillWrapper.eq("artisan_id", artisanId)
                .eq("service_item_id", request.getServiceItemId())
                .eq("status", 1);
        ArtisanSkill artisanSkill = artisanSkillMapper.selectOne(skillWrapper);
        BigDecimal price = artisanSkill != null ? artisanSkill.getPrice() : serviceItem.getBasePrice();
        Integer duration = artisanSkill != null ? artisanSkill.getDuration() : serviceItem.getDefaultDuration();

        Order order = new Order();
        order.setOrderNo(OrderNoGenerator.generateOrderNo());
        order.setUserId(request.getUserId());
        order.setArtisanId(artisanId);
        order.setServiceItemId(request.getServiceItemId());
        order.setServiceName(serviceItem.getName());
        order.setPrice(price);
        order.setDuration(duration);
        order.setAppointmentDate(request.getAppointmentDate());
        order.setAppointmentTime(request.getAppointmentTime());
        order.setAddress(request.getAddress());
        order.setLongitude(request.getLongitude());
        order.setLatitude(request.getLatitude());
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setRemark(request.getRemark());
        order.setOrderStatus(request.getArtisanId() != null ? 1 : 0);
        order.setPayStatus(0);

        orderMapper.insert(order);
        log.debug("订单创建成功: orderId={}, orderNo={}", order.getId(), order.getOrderNo());

        String checkInCode = bookingSlotService.generateCheckInCode(order.getId());
        order.setCheckInCode(checkInCode);
        orderMapper.updateById(order);
        log.debug("核销码生成并保存: orderId={}, checkInCode={}", order.getId(), checkInCode);

        recordOrderLog(order.getId(), 0, request.getUserId(), "创建订单", "用户创建订单");

        log.info("订单创建完成: orderId={}", order.getId());
        return order;
    }

    public Long findNearbyAvailableArtisan(Long serviceItemId, BigDecimal longitude, BigDecimal latitude,
                                           LocalDate date, String timeSlot) {
        log.debug("查找附近可用手艺人: serviceItemId={}, date={}, timeSlot={}", serviceItemId, date, timeSlot);

        List<Long> nearbyArtisanIds = geoLocationService.findNearbyArtisans(longitude, latitude, 10.0);
        if (nearbyArtisanIds == null || nearbyArtisanIds.isEmpty()) {
            log.warn("附近10公里内没有手艺人");
            return null;
        }

        for (Long artisanId : nearbyArtisanIds) {
            Artisan artisan = artisanMapper.selectById(artisanId);
            if (artisan == null || artisan.getAuditStatus() != 1 || artisan.getStatus() != 1) {
                log.debug("手艺人不符合条件: artisanId={}, auditStatus={}, status={}",
                        artisanId, artisan != null ? artisan.getAuditStatus() : null, artisan != null ? artisan.getStatus() : null);
                continue;
            }

            if (artisan.getWorkStatus() != null && artisan.getWorkStatus() == 2) {
                log.debug("手艺人当前忙碌: artisanId={}", artisanId);
                continue;
            }

            QueryWrapper<ArtisanSkill> skillWrapper = new QueryWrapper<>();
            skillWrapper.eq("artisan_id", artisanId)
                    .eq("service_item_id", serviceItemId)
                    .eq("status", 1);
            ArtisanSkill skill = artisanSkillMapper.selectOne(skillWrapper);
            if (skill == null) {
                log.debug("手艺人没有该技能: artisanId={}, serviceItemId={}", artisanId, serviceItemId);
                continue;
            }

            if (!bookingSlotService.isSlotAvailable(artisanId, date, timeSlot)) {
                log.debug("手艺人该时段不可用: artisanId={}, timeSlot={}", artisanId, timeSlot);
                continue;
            }

            log.info("找到合适的手艺人: artisanId={}", artisanId);
            return artisanId;
        }

        log.warn("附近没有符合条件的可用手艺人");
        return null;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order dispatchOrder(Long orderId) {
        log.debug("开始系统自动派单: orderId={}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            log.error("订单状态不正确，无法派单: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单状态不正确，无法派单");
        }

        Long artisanId = findNearbyAvailableArtisan(
                order.getServiceItemId(),
                order.getLongitude(),
                order.getLatitude(),
                order.getAppointmentDate(),
                order.getAppointmentTime()
        );
        if (artisanId == null) {
            log.error("没有找到可用的手艺人进行派单: orderId={}", orderId);
            throw new RuntimeException("没有找到可用的手艺人，请稍后再试");
        }

        boolean reserved = bookingSlotService.reserveSlot(
                artisanId,
                order.getAppointmentDate(),
                order.getAppointmentTime(),
                orderId,
                order.getUserId()
        );
        if (!reserved) {
            log.error("时段锁定失败: artisanId={}, orderId={}", artisanId, orderId);
            throw new RuntimeException("时段锁定失败，请重试");
        }

        order.setArtisanId(artisanId);
        order.setOrderStatus(1);
        order.setDispatchTime(LocalDateTime.now());
        orderMapper.updateById(order);

        recordOrderLog(orderId, 1, null, "系统派单", "系统自动派单给手艺人: " + artisanId);

        log.info("订单派单完成: orderId={}, artisanId={}", orderId, artisanId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order acceptOrder(Long orderId, Long artisanId) {
        log.debug("手艺人接单: orderId={}, artisanId={}", orderId, artisanId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            log.error("订单状态不正确，无法接单: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单状态不正确，无法接单");
        }
        if (!artisanId.equals(order.getArtisanId())) {
            log.error("该订单不属于此手艺人: orderId={}, orderArtisanId={}, artisanId={}",
                    orderId, order.getArtisanId(), artisanId);
            throw new RuntimeException("该订单不属于此手艺人");
        }

        order.setOrderStatus(2);
        order.setAcceptTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Artisan artisan = artisanMapper.selectById(artisanId);
        if (artisan != null) {
            artisan.setWorkStatus(2);
            artisanMapper.updateById(artisan);
            log.debug("更新手艺人工作状态为忙碌: artisanId={}", artisanId);
        }

        recordOrderLog(orderId, 2, artisanId, "手艺人接单", "手艺人确认接单");

        log.info("手艺人接单成功: orderId={}, artisanId={}", orderId, artisanId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order checkIn(Long orderId, String checkInCode) {
        log.debug("到场核销: orderId={}, checkInCode={}", orderId, checkInCode);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 2) {
            log.error("订单状态不正确，无法核销: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单状态不正确，无法核销");
        }

        if (!bookingSlotService.verifyCheckInCode(orderId, checkInCode)) {
            log.error("核销码不正确: orderId={}, inputCode={}", orderId, checkInCode);
            throw new RuntimeException("核销码不正确");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setOrderStatus(3);
        order.setCheckInTime(now);
        order.setStartServiceTime(now);
        orderMapper.updateById(order);

        bookingSlotService.markCheckIn(orderId, order.getArtisanId());

        recordOrderLog(orderId, 2, order.getArtisanId(), "到场核销", "核销码验证通过，开始服务");

        log.info("到场核销成功: orderId={}", orderId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order completeService(Long orderId) {
        log.debug("完成服务: orderId={}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 3) {
            log.error("订单状态不正确，无法完成服务: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单状态不正确，无法完成服务");
        }

        order.setOrderStatus(4);
        order.setEndServiceTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Artisan artisan = artisanMapper.selectById(order.getArtisanId());
        if (artisan != null) {
            artisan.setWorkStatus(1);
            artisanMapper.updateById(artisan);
            log.debug("更新手艺人工作状态为空闲: artisanId={}", order.getArtisanId());
        }

        recordOrderLog(orderId, 2, order.getArtisanId(), "完成服务", "服务已完成，待结算");

        log.info("服务完成: orderId={}", orderId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order settleOrder(Long orderId) {
        log.debug("结算订单: orderId={}", orderId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != 4) {
            log.error("订单状态不正确，无法结算: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单状态不正确，无法结算");
        }

        order.setOrderStatus(5);
        order.setPayStatus(1);
        order.setPayAmount(order.getPrice());
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);

        Artisan artisan = artisanMapper.selectById(order.getArtisanId());
        if (artisan != null) {
            artisan.setOrderCount(artisan.getOrderCount() == null ? 1 : artisan.getOrderCount() + 1);
            artisanMapper.updateById(artisan);
            log.debug("更新手艺人订单数: artisanId={}, orderCount={}", order.getArtisanId(), artisan.getOrderCount());
        }

        bookingSlotService.removeCheckInCode(orderId);

        recordOrderLog(orderId, 1, null, "结算订单", "订单已结算，支付完成");

        log.info("订单结算完成: orderId={}", orderId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Order cancelOrder(Long orderId, String reason, Integer operatorType, Long operatorId) {
        log.debug("取消订单: orderId={}, reason={}, operatorType={}, operatorId={}",
                orderId, reason, operatorType, operatorId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() >= 4) {
            log.error("订单状态不正确，无法取消: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单已完成服务，无法取消");
        }

        Integer previousStatus = order.getOrderStatus();
        order.setOrderStatus(6);
        order.setCancelReason(reason);
        order.setCancelTime(LocalDateTime.now());
        orderMapper.updateById(order);

        bookingSlotService.releaseSlot(order.getArtisanId(), order.getAppointmentDate(), order.getAppointmentTime());

        if (previousStatus >= 2 && order.getArtisanId() != null) {
            Artisan artisan = artisanMapper.selectById(order.getArtisanId());
            if (artisan != null) {
                artisan.setWorkStatus(1);
                artisanMapper.updateById(artisan);
                log.debug("恢复手艺人工作状态为空闲: artisanId={}", order.getArtisanId());
            }
        }

        String operatorName = operatorType == 0 ? "用户" : (operatorType == 1 ? "系统" : "手艺人");
        recordOrderLog(orderId, operatorType, operatorId, "取消订单", operatorName + "取消订单，原因: " + reason);

        log.info("订单取消成功: orderId={}", orderId);
        return order;
    }

    @Transactional(rollbackFor = Exception.class)
    public Review reviewOrder(Long orderId, Long userId, Long artisanId, Integer rating, String content, String imgs) {
        log.debug("评价订单: orderId={}, userId={}, artisanId={}, rating={}", orderId, userId, artisanId, rating);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() < 4) {
            log.error("订单状态不正确，无法评价: orderId={}, status={}", orderId, order.getOrderStatus());
            throw new RuntimeException("订单未完成，无法评价");
        }
        if (!userId.equals(order.getUserId())) {
            log.error("只有下单用户才能评价: orderId={}, orderUserId={}, userId={}",
                    orderId, order.getUserId(), userId);
            throw new RuntimeException("只有下单用户才能评价");
        }

        QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
        reviewWrapper.eq("order_id", orderId);
        Review existingReview = reviewMapper.selectOne(reviewWrapper);
        if (existingReview != null) {
            log.error("该订单已评价: orderId={}", orderId);
            throw new RuntimeException("该订单已评价");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setArtisanId(artisanId);
        review.setRating(rating);
        review.setContent(content);
        review.setImgs(imgs);
        reviewMapper.insert(review);

        QueryWrapper<Review> artisanReviewWrapper = new QueryWrapper<>();
        artisanReviewWrapper.eq("artisan_id", artisanId);
        List<Review> allReviews = reviewMapper.selectList(artisanReviewWrapper);
        if (allReviews != null && !allReviews.isEmpty()) {
            BigDecimal totalRating = BigDecimal.ZERO;
            for (Review r : allReviews) {
                if (r.getRating() != null) {
                    totalRating = totalRating.add(BigDecimal.valueOf(r.getRating()));
                }
            }
            BigDecimal avgRating = totalRating.divide(BigDecimal.valueOf(allReviews.size()), 1, BigDecimal.ROUND_HALF_UP);
            Artisan artisan = artisanMapper.selectById(artisanId);
            if (artisan != null) {
                artisan.setRating(avgRating);
                artisanMapper.updateById(artisan);
                log.debug("更新手艺人平均评分: artisanId={}, avgRating={}", artisanId, avgRating);
            }
        }

        recordOrderLog(orderId, 0, userId, "评价订单", "用户评分: " + rating + "分");

        log.info("订单评价成功: orderId={}, reviewId={}", orderId, review.getId());
        return review;
    }

    private void recordOrderLog(Long orderId, Integer operatorType, Long operatorId, String action, String remark) {
        log.debug("记录订单日志: orderId={}, action={}", orderId, action);
        OrderLog orderLog = new OrderLog();
        orderLog.setOrderId(orderId);
        orderLog.setOperatorType(operatorType);
        orderLog.setOperatorId(operatorId);
        orderLog.setAction(action);
        orderLog.setRemark(remark);
        orderLogMapper.insert(orderLog);
    }

    public Order getOrderById(Long id) {
        log.debug("获取订单详情: id={}", id);
        return orderMapper.selectById(id);
    }

    public List<Order> getUserOrders(Long userId) {
        log.debug("获取用户订单列表: userId={}", userId);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return orderMapper.selectList(wrapper);
    }

    public List<Order> getArtisanOrders(Long artisanId) {
        log.debug("获取手艺人订单列表: artisanId={}", artisanId);
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("artisan_id", artisanId).orderByDesc("create_time");
        return orderMapper.selectList(wrapper);
    }
}
