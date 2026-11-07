package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.entity.DiningTable;
import com.weishixiang.service.TableService;
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
@RequestMapping("/api/admin/table")
@RequiredArgsConstructor
public class AdminTableController {

    private final TableService tableService;

    @GetMapping("/list")
    public Result<List<DiningTable>> list() {
        return Result.ok(tableService.listAll());
    }

    @PostMapping
    public Result<DiningTable> create(@RequestBody DiningTable table) {
        return Result.ok("新增成功", tableService.create(table));
    }

    @PutMapping
    public Result<DiningTable> update(@RequestBody DiningTable table) {
        return Result.ok("修改成功", tableService.update(table));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return Result.ok();
    }
}
