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
    public void sendRequest(Long requesterId, String targetUsername) {
        // Find target user
        QueryWrapper<User> userQuery = new QueryWrapper<>();
        userQuery.eq("username", targetUsername);
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
            result.add(map);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getFamilyMembers(Long userId) {
        QueryWrapper<FamilyRelation> query = new QueryWrapper<>();
        query.and(wrapper -> wrapper.eq("requester_id", userId).or().eq("receiver_id", userId));
        query.eq("status", "ACCEPTED");
        List<FamilyRelation> relations = list(query);

        List<Map<String, Object>> result = new ArrayList<>();
        for (FamilyRelation r : relations) {
            Long otherId = r.getRequesterId().equals(userId) ? r.getReceiverId() : r.getRequesterId();
            User otherUser = userMapper.selectById(otherId);
            Map<String, Object> map = new HashMap<>();
            map.put("id", otherUser.getId());
            map.put("username", otherUser.getUsername());
            map.put("mode", otherUser.getMode());
            map.put("age", otherUser.getAge());
            result.add(map);
        }
        return result;
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
