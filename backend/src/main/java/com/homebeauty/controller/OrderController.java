package com.homebeauty.controller;

import com.homebeauty.common.Result;
import com.homebeauty.dto.CreateOrderRequest;
import com.homebeauty.entity.Order;
import com.homebeauty.entity.Review;
import com.homebeauty.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestBody CreateOrderRequest request) {
        log.debug("创建订单接口被调用: userId={}, serviceItemId={}", request.getUserId(), request.getServiceItemId());
        try {
            Order order = orderService.createOrder(request);
            return Result.success(order);
        } catch (Exception e) {
            log.error("创建订单失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderById(@PathVariable Long id) {
        log.debug("获取订单详情接口被调用: id={}", id);
        try {
            Order order = orderService.getOrderById(id);
            return Result.success(order);
        } catch (Exception e) {
            log.error("获取订单详情失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(@PathVariable Long userId) {
        log.debug("获取用户订单列表接口被调用: userId={}", userId);
        try {
            List<Order> orders = orderService.getUserOrders(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取用户订单列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/artisan/{artisanId}")
    public Result<List<Order>> getArtisanOrders(@PathVariable Long artisanId) {
        log.debug("获取手艺人订单列表接口被调用: artisanId={}", artisanId);
        try {
            List<Order> orders = orderService.getArtisanOrders(artisanId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取手艺人订单列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/dispatch")
    public Result<Order> dispatchOrder(@RequestParam Long orderId) {
        log.debug("派单接口被调用: orderId={}", orderId);
        try {
            Order order = orderService.dispatchOrder(orderId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("派单失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/accept")
    public Result<Order> acceptOrder(@RequestParam Long orderId,
                                     @RequestParam Long artisanId) {
        log.debug("接单接口被调用: orderId={}, artisanId={}", orderId, artisanId);
        try {
            Order order = orderService.acceptOrder(orderId, artisanId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("接单失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/checkin")
    public Result<Order> checkIn(@RequestParam Long orderId,
                                 @RequestParam String checkInCode) {
        log.debug("核销接口被调用: orderId={}, checkInCode={}", orderId, checkInCode);
        try {
            Order order = orderService.checkIn(orderId, checkInCode);
            return Result.success(order);
        } catch (Exception e) {
            log.error("核销失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/complete")
    public Result<Order> completeService(@RequestParam Long orderId) {
        log.debug("完成服务接口被调用: orderId={}", orderId);
        try {
            Order order = orderService.completeService(orderId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("完成服务失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/settle")
    public Result<Order> settleOrder(@RequestParam Long orderId) {
        log.debug("结算接口被调用: orderId={}", orderId);
        try {
            Order order = orderService.settleOrder(orderId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("结算失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public Result<Order> cancelOrder(@RequestParam Long orderId,
                                     @RequestParam String reason,
                                     @RequestParam Integer operatorType,
                                     @RequestParam Long operatorId) {
        log.debug("取消订单接口被调用: orderId={}, reason={}, operatorType={}", orderId, reason, operatorType);
        try {
            Order order = orderService.cancelOrder(orderId, reason, operatorType, operatorId);
            return Result.success(order);
        } catch (Exception e) {
            log.error("取消订单失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/review")
    public Result<Review> reviewOrder(@RequestParam Long orderId,
                                      @RequestParam Long userId,
                                      @RequestParam Long artisanId,
                                      @RequestParam Integer rating,
                                      @RequestParam(required = false) String content,
                                      @RequestParam(required = false) String imgs) {
        log.debug("评价订单接口被调用: orderId={}, userId={}, rating={}", orderId, userId, rating);
        try {
            Review review = orderService.reviewOrder(orderId, userId, artisanId, rating, content, imgs);
            return Result.success(review);
        } catch (Exception e) {
            log.error("评价订单失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
