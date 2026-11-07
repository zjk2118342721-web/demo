package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.entity.Category;
import com.weishixiang.service.CategoryService;
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
@RequestMapping("/api/admin/category")
@RequiredArgsConstructor
public class AdminCategoryController {

    private final CategoryService categoryService;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.listAll());
    }

    @PostMapping
    public Result<Category> create(@RequestBody Category category) {
        return Result.ok("新增成功", categoryService.create(category));
    }

    @PutMapping
    public Result<Category> update(@RequestBody Category category) {
        return Result.ok("修改成功", categoryService.update(category));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.ok();
    }
}
