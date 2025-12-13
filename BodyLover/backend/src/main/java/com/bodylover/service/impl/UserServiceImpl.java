package com.bodylover.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bodylover.entity.User;
import com.bodylover.mapper.UserMapper;
import com.bodylover.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User register(User user) {
        // Check if accountId exists (Login ID must be unique)
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", user.getAccountId());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("Account ID already exists");
        }
        
        // Username is now just a nickname, no need to check uniqueness
        save(user);
        return user;
    }

    @Override
    public User login(String accountId, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", accountId);
        queryWrapper.eq("password", password); // In real app, use BCrypt
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("Invalid account ID or password");
        }
        return user;
    }
}
