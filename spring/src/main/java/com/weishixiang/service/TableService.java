package com.weishixiang.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.weishixiang.common.BusinessException;
import com.weishixiang.entity.DiningTable;
import com.weishixiang.mapper.DiningTableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TableService {

    private final DiningTableMapper tableMapper;

    public List<DiningTable> listAll() {
        return tableMapper.selectList(new LambdaQueryWrapper<DiningTable>()
                .orderByAsc(DiningTable::getTableNo));
    }

    public DiningTable create(DiningTable table) {
        validate(table);
        long exists = tableMapper.selectCount(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getTableNo, table.getTableNo()));
        if (exists > 0) {
            throw new BusinessException("桌号已存在");
        }
        table.setId(null);
        if (table.getSeats() == null || table.getSeats() < 1) {
            table.setSeats(4);
        }
        if (table.getStatus() == null) {
            table.setStatus(0);
        }
        tableMapper.insert(table);
        return table;
    }

    public DiningTable update(DiningTable table) {
        if (table.getId() == null) {
            throw new BusinessException("餐桌ID不能为空");
        }
        validate(table);
        DiningTable exists = tableMapper.selectOne(new LambdaQueryWrapper<DiningTable>()
                .eq(DiningTable::getTableNo, table.getTableNo())
                .ne(DiningTable::getId, table.getId())
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException("桌号已存在");
        }
        tableMapper.updateById(table);
        return table;
    }

    public void delete(Long id) {
        tableMapper.deleteById(id);
    }

    private void validate(DiningTable table) {
        if (!StringUtils.hasText(table.getTableNo())) {
            throw new BusinessException("请输入桌号");
        }
        table.setTableNo(table.getTableNo().trim());
    }
}
