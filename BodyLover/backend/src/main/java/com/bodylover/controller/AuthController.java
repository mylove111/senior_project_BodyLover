package com.bodylover.controller;

import com.bodylover.entity.User;
import com.bodylover.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Allow frontend to access
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            User newUser = userService.register(user);
            result.put("code", 200);
            result.put("message", "Registration successful");
            result.put("data", newUser);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            String username = payload.get("username");
            String password = payload.get("password");
            User user = userService.login(username, password);
            result.put("code", 200);
            result.put("message", "Login successful");
            result.put("data", user);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
