package com.bodylover.controller;

import com.bodylover.entity.HealthRecord;
import com.bodylover.service.HealthRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "*")
public class HealthRecordController {

    @Autowired
    private HealthRecordService healthRecordService;

    @GetMapping
    public Map<String, Object> getRecords(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<HealthRecord> records = healthRecordService.getUserRecords(userId);
            result.put("code", 200);
            result.put("data", records);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> addRecord(@RequestBody HealthRecord record) {
        Map<String, Object> result = new HashMap<>();
        try {
            HealthRecord newRecord = healthRecordService.addRecord(record);
            result.put("code", 200);
            result.put("message", "Record added successfully");
            result.put("data", newRecord);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
