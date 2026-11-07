package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.entity.DiningTable;
import com.weishixiang.service.TableService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/table")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping("/list")
    public Result<List<DiningTable>> list() {
        return Result.ok(tableService.listAll());
    }
}
