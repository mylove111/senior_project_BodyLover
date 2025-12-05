package com.bodylover.service;

import com.bodylover.entity.HealthRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface HealthRecordService extends IService<HealthRecord> {
    List<HealthRecord> getUserRecords(Long userId);
    HealthRecord addRecord(HealthRecord record);
}
