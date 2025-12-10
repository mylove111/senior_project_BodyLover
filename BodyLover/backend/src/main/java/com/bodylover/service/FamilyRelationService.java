package com.bodylover.service;

import com.bodylover.entity.FamilyRelation;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Map;

public interface FamilyRelationService extends IService<FamilyRelation> {
    void sendRequest(Long requesterId, String targetUsername, String relationType);
    List<Map<String, Object>> getPendingRequests(Long userId);
    List<Map<String, Object>> getFamilyMembers(Long userId, String date);
    void handleRequest(Long relationId, String status);
}
