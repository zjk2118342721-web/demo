package com.weishixiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weishixiang.entity.Review;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT r.id, r.order_id, r.user_id, r.rating, r.content, r.images, r.anonymous, r.reply, r.create_time " +
            "FROM review r WHERE EXISTS (SELECT 1 FROM order_item oi WHERE oi.order_id = r.order_id AND oi.dish_id = #{dishId}) " +
            "ORDER BY r.create_time DESC LIMIT 20")
    List<Review> listByDishId(@Param("dishId") Long dishId);
}
