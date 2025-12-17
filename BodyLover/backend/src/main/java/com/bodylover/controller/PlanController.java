package com.bodylover.controller;

import com.bodylover.entity.Plan;
import com.bodylover.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
@CrossOrigin(origins = "*")
public class PlanController {

    @Autowired
    private PlanService planService;
    
    @Autowired
    private com.bodylover.service.UserService userService;

    // Get plans for a specific user (defaults to today if no date provided)
    @GetMapping
    public Map<String, Object> getPlans(@RequestParam Long userId, @RequestParam(required = false) String date) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Plan> plans;
            LocalDate targetDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
            plans = planService.getPlansByDate(userId, targetDate);
            
            result.put("code", 200);
            result.put("data", plans);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping
    public Map<String, Object> addPlan(@RequestBody Plan plan) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (plan.getScheduledDate() == null) {
                plan.setScheduledDate(LocalDate.now());
            }
            if (plan.getStatus() == null) {
                plan.setStatus("PENDING");
            }
            planService.save(plan);
            result.put("code", 200);
            result.put("message", "Added successfully");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
    @PutMapping("/{id}")
    public Map<String, Object> updatePlanStatus(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            Plan plan = planService.getById(id);
            if (plan != null) {
                String status = (String) payload.get("status");
                if (status != null) plan.setStatus(status);
                
                if ("COMPLETED".equals(status) && payload.containsKey("actualMinutes")) {
                    int minutes = Integer.valueOf(payload.get("actualMinutes").toString());
                    plan.setActualMinutes(minutes);

                    // Calculate Points
                    // Calculate Points based on Activity and Duration (User Request)
                    int points = 0;
                    String category = plan.getActivityCategory();
                    
                    if (category != null) {
                        double multiplier = 0.5; // Default low (like Walk/Stairs/Back)

                        switch (category.toUpperCase()) {
                            // Teenager Activities
                            case "RUN":      multiplier = 2.0; break; // 20 pts / 10 min
                            case "WALK":     multiplier = 0.5; break; // 5 pts / 10 min
                            case "YOGA":     multiplier = 1.5; break; // 15 pts / 10 min
                            case "STAIRS":   multiplier = 0.5; break; // 5 pts / 10 min
                            case "JUMPING":  multiplier = 1.0; break; // 10 pts / 10 min
                            
                            // Adult Activities
                            case "CHEST":    multiplier = 1.0; break; // 10 pts / 10 min
                            case "BACK":     multiplier = 0.5; break; // 5 pts / 10 min
                            case "SHOULDERS":multiplier = 1.0; break; // 10 pts / 10 min
                            case "ARMS":     multiplier = 1.5; break; // 15 pts / 10 min
                            case "LEGS":     multiplier = 1.0; break; // 10 pts / 10 min
                            case "CORE":     multiplier = 2.0; break; // 20 pts / 10 min
                            
                            default: multiplier = 0.5;
                        }
                        
                        points = (int) (minutes * multiplier);
                    } else {
                         points = minutes / 2; // Default if no category
                    }
                    
                    if (points > 0) {
                        userService.updatePoints(plan.getUserId(), points);
                    }
                }
                
                planService.updateById(plan);
                result.put("code", 200);
                result.put("message", "Updated successfully");
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStats(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Get last 7 days stats
            // For simplicity, we fetch all completed plans and aggregate in memory (or use SQL group by)
            // Here we use a simple in-memory aggregation for prototype speed
            List<Plan> allPlans = planService.list(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Plan>()
                .eq("user_id", userId)
                .eq("status", "COMPLETED")
                .ge("scheduled_date", LocalDate.now().minusDays(6))
            );

            Map<String, Double> stats = new HashMap<>();
            // Initialize last 7 days with 0
            for (int i = 6; i >= 0; i--) {
                stats.put(LocalDate.now().minusDays(i).toString(), 0.0);
            }

            for (Plan p : allPlans) {
                String date = p.getScheduledDate().toString();
                if (stats.containsKey(date)) {
                    double hours = p.getActualMinutes() != null ? p.getActualMinutes() / 60.0 : (p.getDurationHours() != null ? p.getDurationHours().doubleValue() : 0);
                    stats.put(date, stats.get(date) + hours);
                }
            }

            // Convert to list format for ECharts
            // ECharts expects data sorted by date
            List<Map<String, Object>> chartData = new java.util.ArrayList<>();
            stats.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey().substring(5)); // MM-dd
                    item.put("value", entry.getValue());
                    chartData.add(item);
                });

            result.put("code", 200);
            result.put("data", chartData);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
