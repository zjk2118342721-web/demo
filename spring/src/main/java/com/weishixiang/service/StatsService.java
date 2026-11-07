package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weishixiang.entity.Order;
import com.weishixiang.entity.Review;
import com.weishixiang.mapper.DishMapper;
import com.weishixiang.mapper.OrderMapper;
import com.weishixiang.mapper.ReviewMapper;
import com.weishixiang.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final DishMapper dishMapper;
    private final ReviewMapper reviewMapper;

    public Map<String, Object> dashboard() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime weekStart = LocalDate.now().minusDays(6).atStartOfDay();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .ne(Order::getStatus, 5)
                .ge(Order::getCreateTime, todayStart)));

        List<Order> todayPaid = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .in(Order::getStatus, 1, 2, 3, 4)
                .ge(Order::getPayTime, todayStart));
        BigDecimal todayRevenue = todayPaid.stream()
                .map(Order::getTotalAmount)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("todayRevenue", todayRevenue);

        data.put("totalUsers", userMapper.selectCount(null));
        data.put("pendingOrders", orderMapper.selectCount(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 1)));
        data.put("onlineDishes", dishMapper.selectCount(new LambdaQueryWrapper<com.weishixiang.entity.Dish>()
                .eq(com.weishixiang.entity.Dish::getStatus, 1)));

        List<Review> reviews = reviewMapper.selectList(null);
        double avg = reviews.stream()
                .filter(r -> r.getRating() != null)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
        data.put("avgRating", BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));

        data.put("trend", buildTrend(weekStart));
        data.put("categorySales", orderMapper.categorySales());
        data.put("topDishes", orderMapper.topDishes());

        List<Order> recent = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreateTime)
                .last("LIMIT 6"));
        data.put("recentOrders", recent);
        return data;
    }

    private List<Map<String, Object>> buildTrend(LocalDateTime start) {
        List<Map<String, Object>> rows = orderMapper.dailyTrend(start);
        Map<String, Map<String, Object>> rowMap = new HashMap<>();
        for (Map<String, Object> row : rows) {
            rowMap.put(String.valueOf(row.get("d")), row);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate date = start.toLocalDate().plusDays(i);
            Map<String, Object> row = rowMap.get(date.toString());
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date.format(labelFormatter));
            item.put("orderCount", row == null ? 0 : row.get("orderCount"));
            item.put("amount", row == null ? BigDecimal.ZERO : row.get("amount"));
            result.add(item);
        }
        return result;
    }
}
