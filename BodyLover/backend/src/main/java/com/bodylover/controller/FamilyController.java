package com.bodylover.controller;

import com.bodylover.service.FamilyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family")
@CrossOrigin(origins = "*")
public class FamilyController {

    @Autowired
    private FamilyRelationService familyService;

    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            Long requesterId = Long.valueOf(payload.get("requesterId").toString());
            String targetUsername = (String) payload.get("targetUsername");
            familyService.sendRequest(requesterId, targetUsername);
            result.put("code", 200);
            result.put("message", "Request sent successfully");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/requests")
    public Map<String, Object> getPendingRequests(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> requests = familyService.getPendingRequests(userId);
            result.put("code", 200);
            result.put("data", requests);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/members")
    public Map<String, Object> getMembers(@RequestParam Long userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Map<String, Object>> members = familyService.getFamilyMembers(userId);
            result.put("code", 200);
            result.put("data", members);
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PutMapping("/request/{id}")
    public Map<String, Object> handleRequest(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        Map<String, Object> result = new HashMap<>();
        try {
            String status = payload.get("status"); // ACCEPTED or REJECTED
            familyService.handleRequest(id, status);
            result.put("code", 200);
            result.put("message", "Success");
        } catch (Exception e) {
            result.put("code", 400);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
