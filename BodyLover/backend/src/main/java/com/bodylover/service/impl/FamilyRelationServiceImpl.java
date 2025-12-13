package com.bodylover.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bodylover.entity.FamilyRelation;
import com.bodylover.entity.User;
import com.bodylover.mapper.FamilyRelationMapper;
import com.bodylover.mapper.UserMapper;
import com.bodylover.service.FamilyRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FamilyRelationServiceImpl extends ServiceImpl<FamilyRelationMapper, FamilyRelation> implements FamilyRelationService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public void sendRequest(Long requesterId, String targetAccountId, String relationType) {
        // Find target user by Account ID (Unique)
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("account_id", targetAccountId);
        User targetUser = userMapper.selectOne(userQuery);

        if (targetUser == null) {
            throw new RuntimeException("User not found");
        }
        if (targetUser.getId().equals(requesterId)) {
            throw new RuntimeException("Cannot add yourself");
        }

        // Check existing relation
        QueryWrapper<FamilyRelation> relationQuery = new QueryWrapper<>();
        relationQuery.and(wrapper -> wrapper
            .eq("requester_id", requesterId).eq("receiver_id", targetUser.getId())
            .or()
            .eq("requester_id", targetUser.getId()).eq("receiver_id", requesterId)
        );
        if (count(relationQuery) > 0) {
            throw new RuntimeException("Request already sent or already family");
        }

        FamilyRelation relation = new FamilyRelation();
        relation.setRequesterId(requesterId);
        relation.setReceiverId(targetUser.getId());
        relation.setRelationType(relationType);
        relation.setStatus("PENDING");
        relation.setCreatedAt(LocalDateTime.now());
        save(relation);
    }

    @Override
    public List<Map<String, Object>> getPendingRequests(Long userId) {
        QueryWrapper<FamilyRelation> query = new QueryWrapper<>();
        query.eq("receiver_id", userId);
        query.eq("status", "PENDING");
        List<FamilyRelation> relations = list(query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FamilyRelation r : relations) {
            User requester = userMapper.selectById(r.getRequesterId());
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("requesterName", requester.getUsername());
            map.put("requesterMode", requester.getMode());
            // Receiver views Requester: so we need reciprocal
            map.put("relationDisplay", getReciprocalRelation(r.getRelationType())); 
            result.add(map);
        }
        return result;
    }

    @Autowired
    private com.bodylover.mapper.PlanMapper planMapper;

    @Override
    public List<Map<String, Object>> getFamilyMembers(Long userId, String dateStr) {
        QueryWrapper<FamilyRelation> query = new QueryWrapper<>();
        query.and(wrapper -> wrapper.eq("requester_id", userId).or().eq("receiver_id", userId));
        query.eq("status", "ACCEPTED");
        List<FamilyRelation> relations = list(query);

        List<Map<String, Object>> result = new ArrayList<>();
        java.time.LocalDate targetDate = (dateStr != null && !dateStr.isEmpty()) 
            ? java.time.LocalDate.parse(dateStr) 
            : java.time.LocalDate.now();

        for (FamilyRelation r : relations) {
            boolean isRequester = r.getRequesterId().equals(userId);
            Long otherId = isRequester ? r.getReceiverId() : r.getRequesterId();
            User otherUser = userMapper.selectById(otherId);
            
            Map<String, Object> map = new HashMap<>();
            map.put("id", otherUser.getId());
            map.put("username", otherUser.getUsername());
            map.put("mode", otherUser.getMode());
            map.put("age", otherUser.getAge());
            
            // Calculate Display Relation
            String relation = r.getRelationType();
            if (isRequester) {
                map.put("relationDisplay", getForwardRelation(relation));
            } else {
                map.put("relationDisplay", getReciprocalRelation(relation));
            }

            // Calculate Progress
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.bodylover.entity.Plan> planQuery = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            planQuery.eq("user_id", otherId);
            planQuery.eq("scheduled_date", targetDate);
            List<com.bodylover.entity.Plan> todayPlans = planMapper.selectList(planQuery);

            int total = todayPlans.size();
            long completed = todayPlans.stream().filter(p -> "COMPLETED".equals(p.getStatus())).count();
            int progress = total == 0 ? 0 : (int)((completed * 100) / total);
            
            map.put("totalPlans", total);
            map.put("completedPlans", completed);
            map.put("progress", progress);

            result.add(map);
        }
        return result;
    }

    // Helper: If I am Requester (Initiator), who is the other person to me?
    // e.g. If type is FATHER_SON (Requester is Father), then other is Son.
    // e.g. If type is SON_FATHER (Requester is Son), then other is Father.
    private String getForwardRelation(String type) {
        if (type == null) return "Family";
        switch (type) {
            case "FATHER_SON": return "Son";
            case "MOTHER_SON": return "Son";
            case "GRANDFATHER_GRANDSON": return "Grandson";
            case "GRANDMOTHER_GRANDSON": return "Grandson";
            case "GRANDFATHER_GRANDDAUGHTER": return "Granddaughter";
            case "GRANDMOTHER_GRANDDAUGHTER": return "Granddaughter";
            
            // Inverse types (Requester is the child/grandchild)
            case "SON_FATHER": return "Father";
            case "SON_MOTHER": return "Mother";
            case "GRANDSON_GRANDFATHER": return "Grandfather";
            case "GRANDSON_GRANDMOTHER": return "Grandmother";
            case "GRANDDAUGHTER_GRANDFATHER": return "Grandfather";
            case "GRANDDAUGHTER_GRANDMOTHER": return "Grandmother";
            default: return "Relative";
        }
    }

    // Helper: If I am Receiver, who is the Requester to me?
    // e.g. If type is FATHER_SON (Requester is Father), then Requester is Father.
    // e.g. If type is SON_FATHER (Requester is Son), then Requester is Son.
    private String getReciprocalRelation(String type) {
        if (type == null) return "Family";
        switch (type) {
            case "FATHER_SON": return "Father";
            case "MOTHER_SON": return "Mother";
            case "GRANDFATHER_GRANDSON": return "Grandfather";
            case "GRANDMOTHER_GRANDSON": return "Grandmother";
            case "GRANDFATHER_GRANDDAUGHTER": return "Grandfather";
            case "GRANDMOTHER_GRANDDAUGHTER": return "Grandmother";
            
            // Inverse types
            case "SON_FATHER": return "Son";
            case "SON_MOTHER": return "Son";
            case "GRANDSON_GRANDFATHER": return "Grandson";
            case "GRANDSON_GRANDMOTHER": return "Grandson";
            case "GRANDDAUGHTER_GRANDFATHER": return "Granddaughter";
            case "GRANDDAUGHTER_GRANDMOTHER": return "Granddaughter";
            default: return "Relative";
        }
    }

    @Override
    public void handleRequest(Long relationId, String status) {
        FamilyRelation relation = getById(relationId);
        if (relation != null) {
            relation.setStatus(status);
            updateById(relation);
        }
    }
}
