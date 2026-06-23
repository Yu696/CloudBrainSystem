package com.cloudbrain.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudbrain.common.exception.BusinessException;
import com.cloudbrain.dto.request.LoginRequest;
import com.cloudbrain.dto.request.RegisterRequest;
import com.cloudbrain.dto.request.ResetPasswordRequest;
import com.cloudbrain.dto.request.UserUpdateRequest;
import com.cloudbrain.dto.response.LoginResponse;
import com.cloudbrain.dto.response.UserInfoVO;
import com.cloudbrain.entity.Patient;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.PatientMapper;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import com.cloudbrain.security.JwtUtil;
import com.cloudbrain.service.user.UserService;
import com.cloudbrain.util.UUIDUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PatientMapper patientMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = lambdaQuery().eq(User::getUsername, request.getUserName()).one();
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(),
                String.valueOf(user.getUserType()), null);

        user.setLastLoginTime(LocalDateTime.now());
        updateById(user);

        return new LoginResponse(token, buildUserInfo(user));
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User user = (User) authentication.getPrincipal();
        return buildUserInfo(user);
    }

    @Override
    @Transactional
    public void updateUser(UserUpdateRequest request) {
        User current = getRawUser();

        // 更新用户表
        if (request.getRealName() != null) current.setRealName(request.getRealName());
        if (request.getPhone() != null) current.setPhone(request.getPhone());
        if (request.getEmail() != null) current.setEmail(request.getEmail());
        updateById(current);

        // 如果是患者，同步更新 patient 表
        if (current.getUserType() == 2) {
            Patient patient = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, current.getUserId()));
            if (patient == null) {
                // 兼容历史数据：patient 记录不存在则新建
                patient = new Patient();
                patient.setPatientId(UUIDUtil.generatePatientId());
                patient.setUserId(current.getUserId());
                patient.setMedicalRecordNo(UUIDUtil.generateMedicalRecordNo());
                patient.setStatus(1);
            }
            // 校验唯一字段是否与其他患者冲突
            if (request.getIdCard() != null && !request.getIdCard().equals(patient.getIdCard())) {
                if (patientMapper.selectCount(
                        new LambdaQueryWrapper<Patient>().eq(Patient::getIdCard, request.getIdCard())) > 0) {
                    throw new BusinessException("该身份证号已被其他用户绑定，请检查后重试");
                }
                patient.setIdCard(request.getIdCard());
            }
            if (request.getPhone() != null && !request.getPhone().equals(patient.getPhone())) {
                if (patientMapper.selectCount(
                        new LambdaQueryWrapper<Patient>().eq(Patient::getPhone, request.getPhone())) > 0) {
                    throw new BusinessException("该手机号已被其他用户绑定，请更换手机号");
                }
                patient.setPhone(request.getPhone());
            }

            if (request.getName() != null) patient.setName(request.getName());
            else if (request.getRealName() != null) patient.setName(request.getRealName());
            if (request.getGender() != null) patient.setGender(request.getGender());
            if (request.getBirthDate() != null) patient.setBirthDate(request.getBirthDate());
            if (request.getEmergencyPhone() != null) patient.setEmergencyPhone(request.getEmergencyPhone());
            if (request.getAddress() != null) patient.setAddress(request.getAddress());
            if (request.getBloodType() != null) patient.setBloodType(request.getBloodType());
            if (request.getAllergyHistory() != null) patient.setAllergyHistory(request.getAllergyHistory());
            if (request.getGeneticDiseases() != null) patient.setGeneticDiseases(request.getGeneticDiseases());
            if (request.getMedicalHistory() != null) patient.setMedicalHistory(request.getMedicalHistory());

            if (patient.getId() == null) {
                patientMapper.insert(patient);
            } else {
                patientMapper.updateById(patient);
            }
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        User current = getRawUser();
        if (!passwordEncoder.matches(request.getOldPassword(), current.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        current.setPassword(passwordEncoder.encode(request.getNewPassword()));
        updateById(current);
    }

    @Override
    @Transactional
    public String register(RegisterRequest request) {
        Long count = lambdaQuery().eq(User::getUsername, request.getUserName()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUserId(UUIDUtil.generateUserId());
        user.setUsername(request.getUserName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setUserType(request.getUserType() != null ? request.getUserType() : 2);
        user.setStatus(1);
        save(user);

        // 自动分配默认角色
        String defaultRoleId = switch (user.getUserType()) {
            case 1 -> "ROLE_ADMIN";
            case 0 -> "ROLE_DOCTOR";
            case 2 -> "ROLE_PATIENT";
            default -> "ROLE_PATIENT";
        };
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(defaultRoleId);
        userRoleMapper.insert(userRole);

        // 患者类型自动创建 patient 档案（userId 关联，未填字段给空值占位）
        if (user.getUserType() == 2) {
            Patient patient = new Patient();
            patient.setPatientId(UUIDUtil.generatePatientId());
            patient.setUserId(user.getUserId());
            patient.setMedicalRecordNo(UUIDUtil.generateMedicalRecordNo());
            patient.setName(user.getRealName());
            patient.setIdCard("");
            patient.setGender(0);
            patient.setPhone(user.getPhone());
            patient.setEmergencyPhone("");
            patient.setAddress("");
            patient.setBloodType("");
            patient.setAllergyHistory("");
            patient.setGeneticDiseases("");
            patient.setMedicalHistory("");
            patient.setQrCodeUrl("");
            patient.setSource(1);
            patient.setStatus(1);
            patientMapper.insert(patient);
        }

        return user.getUserId();
    }

    /** 获取所有用户列表（含角色信息，仅管理员） */
    @Override
    public List<UserInfoVO> listAllUsers() {
        // 校验管理员权限
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        User current = (User) auth.getPrincipal();
        if (current.getUserType() == null || current.getUserType() != 1) {
            throw new BusinessException("无操作权限，仅管理员可执行此操作");
        }

        List<User> users = lambdaQuery().orderByAsc(User::getCreateTime).list();
        return users.stream().map(this::buildUserInfo).toList();
    }

    /** 获取当前用户实体（含密码，仅内部使用） */
    private User getRawUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException("未登录");
        }
        return (User) authentication.getPrincipal();
    }

    /** 构建带角色信息和患者档案的用户 VO */
    private UserInfoVO buildUserInfo(User user) {
        String roleName = null;
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, user.getUserId()));
        if (!userRoles.isEmpty()) {
            Role role = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>().eq(Role::getRoleId, userRoles.get(0).getRoleId()));
            roleName = role != null ? role.getRoleName() : null;
        }

        UserInfoVO.UserInfoVOBuilder builder = UserInfoVO.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .userType(user.getUserType())
                .role(roleName);

        // 患者类型：附加档案信息
        if (user.getUserType() != null && user.getUserType() == 2) {
            Patient patient = patientMapper.selectOne(
                    new LambdaQueryWrapper<Patient>().eq(Patient::getUserId, user.getUserId()));
            if (patient != null) {
                builder.patientId(patient.getPatientId())
                        .medicalRecordNo(patient.getMedicalRecordNo())
                        .name(patient.getName())
                        .idCard(patient.getIdCard())
                        .gender(patient.getGender())
                        .birthDate(patient.getBirthDate())
                        .emergencyPhone(patient.getEmergencyPhone())
                        .address(patient.getAddress())
                        .bloodType(patient.getBloodType())
                        .allergyHistory(patient.getAllergyHistory())
                        .geneticDiseases(patient.getGeneticDiseases())
                        .medicalHistory(patient.getMedicalHistory());
            }
        }

        return builder.build();
    }
}
