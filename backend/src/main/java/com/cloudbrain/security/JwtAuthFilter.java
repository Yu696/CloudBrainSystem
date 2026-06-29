package com.cloudbrain.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloudbrain.entity.Role;
import com.cloudbrain.entity.User;
import com.cloudbrain.entity.UserRole;
import com.cloudbrain.mapper.RoleMapper;
import com.cloudbrain.mapper.UserMapper;
import com.cloudbrain.mapper.UserRoleMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && jwtUtil.validate(token)) {
            // K8: 检查 Token 是否在黑名单中（已登出）
            String jti = jwtUtil.getJti(token);
            String blacklistKey = "token:blacklist:" + jti;
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
                filterChain.doFilter(request, response);
                return;
            }

            String userId = jwtUtil.getUserId(token);
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUserId, userId));
            if (user != null && user.getStatus() == 1) {
                // 查询用户角色并构建 GrantedAuthority 列表
                List<GrantedAuthority> authorities = new ArrayList<>();
                List<UserRole> userRoles = userRoleMapper.selectList(
                        new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserId, userId));
                for (UserRole ur : userRoles) {
                    Role role = roleMapper.selectOne(
                            new LambdaQueryWrapper<Role>().eq(Role::getRoleId, ur.getRoleId()));
                    if (role != null && role.getRoleCode() != null) {
                        String authority = role.getRoleCode().toUpperCase();
                        if (!authority.startsWith("ROLE_")) {
                            authority = "ROLE_" + authority;
                        }
                        authorities.add(new SimpleGrantedAuthority(authority));
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                request.setAttribute("currentUserId", userId);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
