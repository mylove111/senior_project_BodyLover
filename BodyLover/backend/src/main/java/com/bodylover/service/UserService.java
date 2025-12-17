package com.bodylover.service;

import com.bodylover.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

public interface UserService extends IService<User> {
    User register(User user);
    User login(String accountId, String password);
    void updatePoints(Long userId, Integer pointsDelta);
}
