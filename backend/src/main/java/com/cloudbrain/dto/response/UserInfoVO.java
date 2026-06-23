package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class UserInfoVO {
    private String userId;
    private String userName;
    private String realName;
    private String phone;
    private String email;
    private String avatarUrl;
    private Integer userType;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
}
