package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weishixiang.common.BusinessException;
import com.weishixiang.entity.Category;
import com.weishixiang.entity.Dish;
import com.weishixiang.mapper.CategoryMapper;
import com.weishixiang.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;

    public List<Category> listEnabled() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
    }

    public List<Category> listAll() {
        List<Category> list = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByAsc(Category::getSort)
                .orderByAsc(Category::getId));
        for (Category category : list) {
            category.setDishCount(dishMapper.selectCount(new LambdaQueryWrapper<Dish>()
                    .eq(Dish::getCategoryId, category.getId())));
        }
        return list;
    }

    public Category create(Category category) {
        validate(category);
        category.setId(null);
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Category category) {
        if (category.getId() == null) {
            throw new BusinessException("分类ID不能为空");
        }
        validate(category);
        categoryMapper.updateById(category);
        return category;
    }

    public void delete(Long id) {
        long dishCount = dishMapper.selectCount(new LambdaQueryWrapper<Dish>().eq(Dish::getCategoryId, id));
        if (dishCount > 0) {
            throw new BusinessException("该分类下还有 " + dishCount + " 道菜品，请先移除菜品");
        }
        categoryMapper.deleteById(id);
    }

    private void validate(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new BusinessException("请输入分类名称");
        }
        category.setName(category.getName().trim());
        if (category.getName().length() > 50) {
            throw new BusinessException("分类名称不能超过50个字符");
        }
    }
}
