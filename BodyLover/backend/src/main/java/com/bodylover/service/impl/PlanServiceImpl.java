package com.bodylover.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bodylover.entity.Plan;
import com.bodylover.mapper.PlanMapper;
import com.bodylover.service.PlanService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    @Override
    public List<Plan> getPlansByDate(Long userId, LocalDate date) {
        return list(new QueryWrapper<Plan>()
                .eq("user_id", userId)
                .eq("scheduled_date", date)
                .orderByDesc("created_at"));
    }
}
