package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weishixiang.common.BusinessException;
import com.weishixiang.entity.Cart;
import com.weishixiang.entity.Dish;
import com.weishixiang.mapper.CartMapper;
import com.weishixiang.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartMapper cartMapper;
    private final DishMapper dishMapper;

    public List<Cart> list(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime));
        if (carts.isEmpty()) {
            return carts;
        }
        Set<Long> dishIds = carts.stream().map(Cart::getDishId).collect(Collectors.toSet());
        Map<Long, Dish> dishMap = dishMapper.selectBatchIds(dishIds).stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));
        for (Cart cart : carts) {
            Dish dish = dishMap.get(cart.getDishId());
            if (dish == null) {
                cart.setDishName("菜品已失效");
                cart.setDishImage("");
                cart.setPrice(BigDecimal.ZERO);
                cart.setDishStatus(0);
                continue;
            }
            cart.setDishName(dish.getName());
            cart.setDishImage(dish.getImage());
            cart.setPrice(dish.getPrice());
            cart.setDishStatus(dish.getStatus());
        }
        return carts;
    }

    public Cart add(Long userId, Long dishId, Integer quantity) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null || dish.getStatus() == null || dish.getStatus() != 1) {
            throw new BusinessException("该菜品已下架");
        }
        int qty = quantity == null || quantity < 1 ? 1 : Math.min(quantity, 99);
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDishId, dishId)
                .last("LIMIT 1"));
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setDishId(dishId);
            cart.setQuantity(qty);
            cartMapper.insert(cart);
        } else {
            cart.setQuantity(Math.min(cart.getQuantity() + qty, 99));
            cartMapper.updateById(cart);
        }
        return cart;
    }

    public void updateQuantity(Long userId, Long dishId, Integer quantity) {
        Cart cart = cartMapper.selectOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDishId, dishId)
                .last("LIMIT 1"));
        if (cart == null) {
            return;
        }
        if (quantity == null || quantity <= 0) {
            cartMapper.deleteById(cart.getId());
            return;
        }
        cart.setQuantity(Math.min(quantity, 99));
        cartMapper.updateById(cart);
    }

    public void remove(Long userId, Long dishId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getDishId, dishId));
    }

    public void clear(Long userId) {
        cartMapper.delete(new LambdaQueryWrapper<Cart>().eq(Cart::getUserId, userId));
    }

    public int count(Long userId) {
        List<Cart> carts = cartMapper.selectList(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId));
        return carts.stream().mapToInt(c -> c.getQuantity() == null ? 0 : c.getQuantity()).sum();
    }
}
