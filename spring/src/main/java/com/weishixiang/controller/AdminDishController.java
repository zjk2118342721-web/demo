package com.weishixiang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.weishixiang.common.Result;
import com.weishixiang.entity.Dish;
import com.weishixiang.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dish")
@RequiredArgsConstructor
public class AdminDishController {

    private final DishService dishService;

    @GetMapping("/page")
    public Result<IPage<Dish>> page(@RequestParam(defaultValue = "1") long page,
                                   @RequestParam(defaultValue = "10") long size,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) Integer status) {
        return Result.ok(dishService.pageAdmin(page, size, categoryId, keyword, status));
    }

    @GetMapping("/{id}")
    public Result<Dish> detail(@PathVariable Long id) {
        return Result.ok(dishService.detail(id));
    }

    @PostMapping
    public Result<Dish> create(@RequestBody Dish dish) {
        return Result.ok("新增成功", dishService.create(dish));
    }

    @PutMapping
    public Result<Dish> update(@RequestBody Dish dish) {
        return Result.ok("修改成功", dishService.update(dish));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id, @RequestParam Integer status) {
        dishService.changeStatus(id, status);
        return Result.ok();
    }
}
