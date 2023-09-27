package com.th.test;

public class UserServiceImpl implements UserService {
    @Override
    public boolean login(String name, String password) {
        System.out.println("嘻嘻");
        System.out.println(name + "   " + password);
        return true;
    }
}
