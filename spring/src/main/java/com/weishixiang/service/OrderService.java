package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weishixiang.common.BusinessException;
import com.weishixiang.dto.CartItemDTO;
import com.weishixiang.dto.OrderCreateDTO;
import com.weishixiang.entity.Cart;
import com.weishixiang.entity.Dish;
import com.weishixiang.entity.Order;
import com.weishixiang.entity.OrderItem;
import com.weishixiang.entity.Review;
import com.weishixiang.entity.User;
import com.weishixiang.mapper.CartMapper;
import com.weishixiang.mapper.DishMapper;
import com.weishixiang.mapper.OrderItemMapper;
import com.weishixiang.mapper.OrderMapper;
import com.weishixiang.mapper.ReviewMapper;
import com.weishixiang.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final DishMapper dishMapper;
    private final ReviewMapper reviewMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Random RANDOM = new Random();

    @Transactional
    public Order create(Long userId, OrderCreateDTO dto) {
        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (CartItemDTO item : dto.getItems()) {
            if (item.getDishId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("下单菜品信息有误");
            }
            quantities.merge(item.getDishId(), Math.min(item.getQuantity(), 99), Integer::sum);
        }
        if (quantities.isEmpty()) {
            throw new BusinessException("请至少选择一个菜品");
        }

        List<Dish> dishes = dishMapper.selectBatchIds(quantities.keySet());
        if (dishes.size() != quantities.size()) {
            throw new BusinessException("部分菜品不存在，请刷新后重试");
        }
        for (Dish dish : dishes) {
            if (dish.getStatus() == null || dish.getStatus() != 1) {
                throw new BusinessException("「" + dish.getName() + "」已下架，请重新选择");
            }
        }

        BigDecimal total = BigDecimal.ZERO;
        for (Dish dish : dishes) {
            total = total.add(dish.getPrice().multiply(BigDecimal.valueOf(quantities.get(dish.getId()))));
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTableNo(StringUtils.hasText(dto.getTableNo()) ? dto.getTableNo().trim() : "");
        order.setTotalAmount(total);
        order.setStatus(0);
        order.setRemark(dto.getRemark() == null ? "" : dto.getRemark().trim());
        orderMapper.insert(order);

        for (Dish dish : dishes) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setDishId(dish.getId());
            item.setDishName(dish.getName());
            item.setDishImage(dish.getImage());
            item.setPrice(dish.getPrice());
            item.setQuantity(quantities.get(dish.getId()));
            orderItemMapper.insert(item);
        }

        // 下单后从购物车移除已购买菜品
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .in(Cart::getDishId, quantities.keySet()));

        return detail(userId, order.getId());
    }

    @Transactional
    public Order pay(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != 0) {
            throw new BusinessException("当前订单无需支付");
        }
        order.setStatus(1);
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        List<OrderItem> items = listItems(orderId);
        for (OrderItem item : items) {
            dishMapper.changeSales(item.getDishId(), item.getQuantity());
        }
        return detail(userId, orderId);
    }

    @Transactional
    public Order cancel(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() == null) {
            throw new BusinessException("订单状态异常");
        }
        if (order.getStatus() >= 2) {
            throw new BusinessException("订单已在制作中，请联系店员协助取消");
        }
        if (order.getStatus() == 5) {
            throw new BusinessException("订单已取消");
        }
        if (order.getStatus() == 1) {
            List<OrderItem> items = listItems(orderId);
            for (OrderItem item : items) {
                dishMapper.changeSales(item.getDishId(), -item.getQuantity());
            }
        }
        order.setStatus(5);
        orderMapper.updateById(order);
        return detail(userId, orderId);
    }

    public Order confirm(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        if (order.getStatus() == null || order.getStatus() != 3) {
            throw new BusinessException("当前订单不能确认完成");
        }
        order.setStatus(4);
        order.setCompleteTime(LocalDateTime.now());
        orderMapper.updateById(order);
        return detail(userId, orderId);
    }

    public List<Order> list(Long userId, Integer status) {
        List<Order> orders = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(status != null, Order::getStatus, status)
                .orderByDesc(Order::getCreateTime));
        fillExtras(orders);
        return orders;
    }

    public Order detail(Long userId, Long orderId) {
        Order order = getOwned(userId, orderId);
        fillExtras(List.of(order));
        return order;
    }

    public IPage<Order> pageAdmin(long page, long size, Integer status, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(status != null, Order::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Order::getOrderNo, keyword)
                        .or()
                        .like(Order::getTableNo, keyword))
                .orderByDesc(Order::getCreateTime);
        IPage<Order> result = orderMapper.selectPage(new Page<>(page, size), wrapper);
        fillExtras(result.getRecords());
        return result;
    }

    public Order detailAdmin(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        fillExtras(List.of(order));
        return order;
    }

    @Transactional
    public Order updateStatus(Long orderId, Integer status) {
        if (status == null || status < 2 || status > 5) {
            throw new BusinessException("不支持的订单状态");
        }
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() >= 4) {
            throw new BusinessException("订单已结束，无法变更状态");
        }
        if (status <= order.getStatus()) {
            throw new BusinessException("订单状态不能回退");
        }
        if (status == 5) {
            if (order.getStatus() >= 1) {
                for (OrderItem item : listItems(orderId)) {
                    dishMapper.changeSales(item.getDishId(), -item.getQuantity());
                }
            }
        }
        if (status == 4) {
            order.setCompleteTime(LocalDateTime.now());
        }
        order.setStatus(status);
        orderMapper.updateById(order);
        return detailAdmin(orderId);
    }

    private Order getOwned(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("订单不存在");
        }
        return order;
    }

    private List<OrderItem> listItems(Long orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, orderId));
    }

    private void fillExtras(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return;
        }
        Set<Long> orderIds = orders.stream().map(Order::getId).collect(Collectors.toSet());

        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds));
        Map<Long, List<OrderItem>> itemMap = new HashMap<>();
        for (OrderItem item : items) {
            itemMap.computeIfAbsent(item.getOrderId(), k -> new ArrayList<>()).add(item);
        }

        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .in(Review::getOrderId, orderIds));
        Map<Long, Review> reviewMap = reviews.stream()
                .collect(Collectors.toMap(Review::getOrderId, r -> r, (a, b) -> a));

        Set<Long> userIds = orders.stream().map(Order::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> nicknames = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userMapper.selectBatchIds(userIds)) {
                nicknames.put(user.getId(), user.getNickname());
            }
        }

        for (Order order : orders) {
            order.setItems(itemMap.getOrDefault(order.getId(), List.of()));
            order.setUserNickname(nicknames.getOrDefault(order.getUserId(), "顾客"));
            Review review = reviewMap.get(order.getId());
            order.setReviewed(review != null);
            if (review != null) {
                order.setReviewRating(review.getRating());
                order.setReviewContent(review.getContent());
            }
        }
    }

    private String generateOrderNo() {
        String time = LocalDateTime.now().format(ORDER_NO_FORMAT);
        return "WSX" + time + String.format("%04d", RANDOM.nextInt(10000));
    }
}
