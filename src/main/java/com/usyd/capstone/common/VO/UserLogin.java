package com.usyd.capstone.common.VO;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class UserLogin {
    // 变量名要小写
    private String email;

    private String password;

    private int userRole; //1 = normal user 2 = admin user 3 = super user
}

