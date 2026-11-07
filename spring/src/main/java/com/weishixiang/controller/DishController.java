package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.entity.Dish;
import com.weishixiang.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dish")
@RequiredArgsConstructor
public class DishController {

    private final DishService dishService;

    @GetMapping("/list")
    public Result<List<Dish>> list(@RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword) {
        return Result.ok(dishService.listForWx(categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Dish> detail(@PathVariable Long id) {
        return Result.ok(dishService.detail(id));
    }
}
