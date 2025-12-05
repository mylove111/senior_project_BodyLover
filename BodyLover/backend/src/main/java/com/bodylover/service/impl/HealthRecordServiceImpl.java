package com.bodylover.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bodylover.entity.HealthRecord;
import com.bodylover.mapper.HealthRecordMapper;
import com.bodylover.service.HealthRecordService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class HealthRecordServiceImpl extends ServiceImpl<HealthRecordMapper, HealthRecord> implements HealthRecordService {

    @Override
    public List<HealthRecord> getUserRecords(Long userId) {
        QueryWrapper<HealthRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("id"); // Sort by ID to get the absolute latest record
        return list(queryWrapper);
    }

    @Override
    public HealthRecord addRecord(HealthRecord record) {
        // Calculate BMI: weight (kg) / (height (m) * height (m))
        if (record.getHeight() != null && record.getWeight() != null) {
            BigDecimal heightInMeters = record.getHeight().divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            if (heightInMeters.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal bmi = record.getWeight().divide(heightInMeters.multiply(heightInMeters), 2, RoundingMode.HALF_UP);
                record.setBmi(bmi);
            }
        }
        if (record.getRecordDate() == null) {
            record.setRecordDate(LocalDate.now());
        }
        save(record);
        return record;
    }
}
