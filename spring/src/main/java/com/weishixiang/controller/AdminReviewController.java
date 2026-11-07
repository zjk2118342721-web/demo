package com.weishixiang.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.weishixiang.common.Result;
import com.weishixiang.dto.ReplyDTO;
import com.weishixiang.entity.Review;
import com.weishixiang.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/review")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;

    @GetMapping("/page")
    public Result<IPage<Review>> page(@RequestParam(defaultValue = "1") long page,
                                      @RequestParam(defaultValue = "10") long size,
                                      @RequestParam(required = false) Integer rating,
                                      @RequestParam(required = false) String keyword) {
        return Result.ok(reviewService.pageAdmin(page, size, rating, keyword));
    }

    @PutMapping("/{id}/reply")
    public Result<Void> reply(@PathVariable Long id, @Valid @RequestBody ReplyDTO dto) {
        reviewService.reply(id, dto.getReply());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return Result.ok();
    }
}
