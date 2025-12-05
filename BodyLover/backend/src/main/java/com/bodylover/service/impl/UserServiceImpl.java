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
        // Simple check if username exists
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        if (count(queryWrapper) > 0) {
            throw new RuntimeException("Username already exists");
        }
        save(user);
        return user;
    }

    @Override
    public User login(String username, String password) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", password); // In real app, use BCrypt
        User user = getOne(queryWrapper);
        if (user == null) {
            throw new RuntimeException("Invalid username or password");
        }
        return user;
    }
}
