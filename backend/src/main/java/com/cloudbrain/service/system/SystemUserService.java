package com.cloudbrain.service.system;

import com.cloudbrain.dto.request.SystemUserAddRequest;
import com.cloudbrain.dto.request.SystemUserStatusRequest;
import com.cloudbrain.dto.request.SystemUserUpdateRequest;
import com.cloudbrain.dto.response.SystemUserVO;

import java.util.List;
import java.util.Map;

public interface SystemUserService {

    /** 新增系统用户，创建 user + system_user + user_role 记录 */
    String addSystemUser(SystemUserAddRequest request);

    /** 系统用户列表，返回所有系统管理员用户 */
    List<SystemUserVO> listSystemUsers();

    /** 用户详情，含角色信息 */
    SystemUserVO getSystemUserDetail(String userId);

    /** 修改系统用户（用户名、角色） */
    void updateSystemUser(SystemUserUpdateRequest request);

    /** 启用/禁用系统用户 */
    void updateStatus(SystemUserStatusRequest request);

    /** 删除系统用户，校验关联数据 */
    void deleteSystemUser(String userId);
}
