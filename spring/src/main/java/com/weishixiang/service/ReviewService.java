package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weishixiang.common.BusinessException;
import com.weishixiang.dto.ReviewDTO;
import com.weishixiang.entity.Order;
import com.weishixiang.entity.OrderItem;
import com.weishixiang.entity.Review;
import com.weishixiang.entity.User;
import com.weishixiang.mapper.OrderItemMapper;
import com.weishixiang.mapper.OrderMapper;
import com.weishixiang.mapper.ReviewMapper;
import com.weishixiang.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final UserMapper userMapper;

    public Review add(Long userId, ReviewDTO dto) {
        Order order = orderMapper.selectById(dto.getOrderId());
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() == null || order.getStatus() != 4) {
            throw new BusinessException("订单完成后才能评价");
        }
        long exists = reviewMapper.selectCount(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, dto.getOrderId()));
        if (exists > 0) {
            throw new BusinessException("该订单已评价");
        }
        Review review = new Review();
        review.setOrderId(dto.getOrderId());
        review.setUserId(userId);
        review.setRating(dto.getRating());
        review.setContent(dto.getContent() == null ? "" : dto.getContent().trim());
        review.setImages(dto.getImages() == null ? "" : String.join(",", dto.getImages()));
        review.setAnonymous(dto.getAnonymous() != null && dto.getAnonymous() == 1 ? 1 : 0);
        review.setReply("");
        reviewMapper.insert(review);
        fillReviewExtras(List.of(review));
        return review;
    }

    public List<Review> myList(Long userId) {
        List<Review> reviews = reviewMapper.selectList(new LambdaQueryWrapper<Review>()
                .eq(Review::getUserId, userId)
                .orderByDesc(Review::getCreateTime));
        fillReviewExtras(reviews);
        return reviews;
    }

    public List<Review> listByDish(Long dishId) {
        List<Review> reviews = reviewMapper.listByDishId(dishId);
        fillReviewExtras(reviews);
        for (Review review : reviews) {
            if (review.getAnonymous() != null && review.getAnonymous() == 1) {
                review.setUserNickname("匿名用户");
                review.setUserAvatar("");
                review.setUserId(null);
            }
        }
        return reviews;
    }

    public IPage<Review> pageAdmin(long page, long size, Integer rating, String keyword) {
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(rating != null, Review::getRating, rating)
                .like(StringUtils.hasText(keyword), Review::getContent, keyword)
                .orderByDesc(Review::getCreateTime);
        IPage<Review> result = reviewMapper.selectPage(new Page<>(page, size), wrapper);
        fillReviewExtras(result.getRecords());
        return result;
    }

    public void reply(Long id, String content) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            throw new BusinessException("评价不存在");
        }
        review.setReply(content == null ? "" : content.trim());
        reviewMapper.updateById(review);
    }

    public void delete(Long id) {
        reviewMapper.deleteById(id);
    }

    private void fillReviewExtras(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        Set<Long> userIds = reviews.stream().map(Review::getUserId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            for (User user : userMapper.selectBatchIds(userIds)) {
                userMap.put(user.getId(), user);
            }
        }
        Set<Long> orderIds = reviews.stream().map(Review::getOrderId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, Order> orderMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            for (Order order : orderMapper.selectBatchIds(orderIds)) {
                orderMap.put(order.getId(), order);
            }
        }
        Map<Long, List<OrderItem>> itemMap = new HashMap<>();
        if (!orderIds.isEmpty()) {
            List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                    .in(OrderItem::getOrderId, orderIds));
            for (OrderItem item : items) {
                itemMap.computeIfAbsent(item.getOrderId(), k -> new java.util.ArrayList<>()).add(item);
            }
        }
        for (Review review : reviews) {
            User user = userMap.get(review.getUserId());
            review.setUserNickname(user == null ? "顾客" : user.getNickname());
            review.setUserAvatar(user == null ? "" : user.getAvatar());
            Order order = orderMap.get(review.getOrderId());
            review.setOrderNo(order == null ? "" : order.getOrderNo());
            List<OrderItem> items = itemMap.getOrDefault(review.getOrderId(), List.of());
            review.setDishNames(items.stream()
                    .map(OrderItem::getDishName)
                    .collect(Collectors.joining("、")));
        }
    }
}
