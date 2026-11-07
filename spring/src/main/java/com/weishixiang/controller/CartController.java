package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.common.UserContext;
import com.weishixiang.dto.CartItemDTO;
import com.weishixiang.entity.Cart;
import com.weishixiang.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/list")
    public Result<List<Cart>> list() {
        return Result.ok(cartService.list(UserContext.getUserId()));
    }

    @PostMapping("/add")
    public Result<Cart> add(@Valid @RequestBody CartItemDTO dto) {
        return Result.ok(cartService.add(UserContext.getUserId(), dto.getDishId(), dto.getQuantity()));
    }

    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody CartItemDTO dto) {
        cartService.updateQuantity(UserContext.getUserId(), dto.getDishId(), dto.getQuantity());
        return Result.ok();
    }

    @DeleteMapping("/{dishId}")
    public Result<Void> remove(@PathVariable Long dishId) {
        cartService.remove(UserContext.getUserId(), dishId);
        return Result.ok();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.ok();
    }

    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.ok(cartService.count(UserContext.getUserId()));
    }
}
