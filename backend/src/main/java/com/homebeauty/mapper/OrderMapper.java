package com.homebeauty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.homebeauty.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
