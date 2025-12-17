package com.bodylover.controller;

import com.bodylover.entity.User;
import com.bodylover.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public Map<String, Object> getUserInfo(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            User user = userService.getById(id);
            if (user == null) throw new RuntimeException("User not found");
            result.put("code", 200);
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/points/deduct")
    public Map<String, Object> deductPoints(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long userId = Long.valueOf(payload.get("userId").toString());
            Integer points = Integer.valueOf(payload.get("points").toString()); // Points to deduct (positive value)

            User user = userService.getById(userId);
            if (user != null) {
                int current = user.getPoints() != null ? user.getPoints() : 0;
                if (current < points) {
                    throw new RuntimeException("Insufficient points");
                }
                userService.updatePoints(userId, -points);
                result.put("code", 200);
                result.put("message", "Points deducted");
                result.put("currentPoints", current - points);
            } else {
                throw new RuntimeException("User not found");
            }
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
