package com.bodylover.service;

import com.bodylover.entity.Plan;
import com.baomidou.mybatisplus.extension.service.IService;
import java.time.LocalDate;
import java.util.List;

public interface PlanService extends IService<Plan> {
    List<Plan> getPlansByDate(Long userId, LocalDate date);
}
