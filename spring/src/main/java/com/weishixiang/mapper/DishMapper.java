package com.weishixiang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.weishixiang.entity.Dish;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DishMapper extends BaseMapper<Dish> {

    @Update("UPDATE dish SET sales = GREATEST(sales + #{delta}, 0) WHERE id = #{id}")
    int changeSales(@Param("id") Long id, @Param("delta") Integer delta);
}
