package com.weishixiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weishixiang.entity.Order;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT DATE_FORMAT(create_time, '%Y-%m-%d') AS d, COUNT(*) AS orderCount, " +
            "IFNULL(SUM(total_amount), 0) AS amount " +
            "FROM orders WHERE status IN (1,2,3,4) AND create_time >= #{start} " +
            "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d')")
    List<Map<String, Object>> dailyTrend(@Param("start") LocalDateTime start);

    @Select("SELECT c.name AS name, IFNULL(SUM(oi.quantity), 0) AS value " +
            "FROM order_item oi " +
            "JOIN orders o ON o.id = oi.order_id " +
            "JOIN dish d ON d.id = oi.dish_id " +
            "JOIN category c ON c.id = d.category_id " +
            "WHERE o.status IN (1,2,3,4) " +
            "GROUP BY c.id, c.name ORDER BY value DESC")
    List<Map<String, Object>> categorySales();

    @Select("SELECT oi.dish_name AS name, IFNULL(SUM(oi.quantity), 0) AS value " +
            "FROM order_item oi JOIN orders o ON o.id = oi.order_id " +
            "WHERE o.status IN (1,2,3,4) " +
            "GROUP BY oi.dish_id, oi.dish_name ORDER BY value DESC LIMIT 8")
    List<Map<String, Object>> topDishes();
}
