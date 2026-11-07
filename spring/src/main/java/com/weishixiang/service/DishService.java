package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.weishixiang.common.BusinessException;
import com.weishixiang.entity.Category;
import com.weishixiang.entity.Dish;
import com.weishixiang.mapper.CategoryMapper;
import com.weishixiang.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;

    public List<Dish> listForWx(Long categoryId, String keyword) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Dish::getName, keyword)
                        .or()
                        .like(Dish::getDescription, keyword))
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getSales);
        List<Dish> list = dishMapper.selectList(wrapper);
        fillCategoryNames(list);
        return list;
    }

    public IPage<Dish> pageAdmin(long page, long size, Long categoryId, String keyword, Integer status) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<Dish>()
                .eq(categoryId != null, Dish::getCategoryId, categoryId)
                .eq(status != null, Dish::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(Dish::getName, keyword)
                        .or()
                        .like(Dish::getDescription, keyword))
                .orderByAsc(Dish::getSort)
                .orderByDesc(Dish::getId);
        IPage<Dish> result = dishMapper.selectPage(new Page<>(page, size), wrapper);
        fillCategoryNames(result.getRecords());
        return result;
    }

    public Dish detail(Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        Category category = categoryMapper.selectById(dish.getCategoryId());
        dish.setCategoryName(category == null ? "" : category.getName());
        return dish;
    }

    public Dish create(Dish dish) {
        validate(dish);
        dish.setId(null);
        if (dish.getSales() == null) {
            dish.setSales(0);
        }
        if (dish.getStatus() == null) {
            dish.setStatus(1);
        }
        if (dish.getSort() == null) {
            dish.setSort(0);
        }
        dishMapper.insert(dish);
        return dish;
    }

    public Dish update(Dish dish) {
        if (dish.getId() == null) {
            throw new BusinessException("菜品ID不能为空");
        }
        validate(dish);
        dishMapper.updateById(dish);
        return dish;
    }

    public void delete(Long id) {
        dishMapper.deleteById(id);
    }

    public void changeStatus(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new BusinessException("菜品不存在");
        }
        dish.setStatus(status != null && status == 1 ? 1 : 0);
        dishMapper.updateById(dish);
    }

    public Map<Long, Dish> mapByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashMap<>();
        }
        return dishMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Dish::getId, d -> d));
    }

    private void validate(Dish dish) {
        if (!StringUtils.hasText(dish.getName())) {
            throw new BusinessException("请输入菜品名称");
        }
        if (dish.getCategoryId() == null) {
            throw new BusinessException("请选择菜品分类");
        }
        if (dish.getPrice() == null || dish.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("菜品价格必须大于0");
        }
        if (categoryMapper.selectById(dish.getCategoryId()) == null) {
            throw new BusinessException("所选分类不存在");
        }
        dish.setName(dish.getName().trim());
        if (dish.getOriginalPrice() != null && dish.getOriginalPrice().compareTo(BigDecimal.ZERO) < 0) {
            dish.setOriginalPrice(BigDecimal.ZERO);
        }
    }

    private void fillCategoryNames(List<Dish> dishes) {
        Set<Long> categoryIds = dishes.stream()
                .map(Dish::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return;
        }
        Map<Long, String> names = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
        dishes.forEach(d -> d.setCategoryName(names.getOrDefault(d.getCategoryId(), "")));
    }
}
