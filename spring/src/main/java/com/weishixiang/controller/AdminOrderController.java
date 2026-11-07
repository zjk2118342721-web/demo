package com.weishixiang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.weishixiang.common.Result;
import com.weishixiang.entity.Order;
import com.weishixiang.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping("/page")
    public Result<IPage<Order>> page(@RequestParam(defaultValue = "1") long page,
                                     @RequestParam(defaultValue = "10") long size,
                                     @RequestParam(required = false) Integer status,
                                     @RequestParam(required = false) String keyword) {
        return Result.ok(orderService.pageAdmin(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        return Result.ok(orderService.detailAdmin(id));
    }

    @PutMapping("/{id}/status")
    public Result<Order> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return Result.ok("订单状态已更新", orderService.updateStatus(id, status));
    }
}
