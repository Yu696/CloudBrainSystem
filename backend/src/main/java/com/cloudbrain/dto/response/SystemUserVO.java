package com.cloudbrain.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SystemUserVO {

    private String userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private Integer userType;
    private Integer adminType;
    private String roleId;
    private String roleName;
    private Integer status;
    private LocalDateTime createTime;
}
