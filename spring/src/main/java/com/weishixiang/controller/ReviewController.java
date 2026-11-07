package com.weishixiang.controller;

import com.weishixiang.common.Result;
import com.weishixiang.common.UserContext;
import com.weishixiang.dto.ReviewDTO;
import com.weishixiang.entity.Review;
import com.weishixiang.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/add")
    public Result<Review> add(@Valid @RequestBody ReviewDTO dto) {
        return Result.ok("评价成功", reviewService.add(UserContext.getUserId(), dto));
    }

    @GetMapping("/my")
    public Result<List<Review>> my() {
        return Result.ok(reviewService.myList(UserContext.getUserId()));
    }

    @GetMapping("/dish/{dishId}")
    public Result<List<Review>> listByDish(@PathVariable Long dishId) {
        return Result.ok(reviewService.listByDish(dishId));
    }
}
