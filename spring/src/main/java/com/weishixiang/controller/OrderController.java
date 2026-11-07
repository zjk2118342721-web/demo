package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.common.UserContext;
import com.weishixiang.dto.OrderCreateDTO;
import com.weishixiang.entity.Order;
import com.weishixiang.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public Result<Order> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok("下单成功", orderService.create(UserContext.getUserId(), dto));
    }

    @GetMapping("/list")
    public Result<List<Order>> list(@RequestParam(required = false) Integer status) {
        return Result.ok(orderService.list(UserContext.getUserId(), status));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.detail(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/pay")
    public Result<Order> pay(@PathVariable Long id) {
        return Result.ok("支付成功", orderService.pay(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/cancel")
    public Result<Order> cancel(@PathVariable Long id) {
        return Result.ok(orderService.cancel(UserContext.getUserId(), id));
    }

    @PostMapping("/{id}/confirm")
    public Result<Order> confirm(@PathVariable Long id) {
        return Result.ok(orderService.confirm(UserContext.getUserId(), id));
    }
}
