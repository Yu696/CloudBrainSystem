# 云脑诊疗平台 — 阶段一 API 文档

## 通用说明

### 响应格式

所有接口统一返回 `Result<T>` 结构：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| code | int | 200=成功，400=业务异常，401=未认证，403=无权限，404=未找到，500=服务器错误 |
| message | String | 提示信息 |
| data | T | 响应数据 |

### 认证方式

除登录/注册外，其他接口需在请求头携带 JWT Token：

```
Authorization: Bearer <token>
```

Token 通过登录接口获取，有效期 24 小时。

---

## 一、用户中心 — `/api/user`

### 1.1 用户登录

登录验证，返回 JWT Token 和用户信息。

- **URL**: `POST /api/user/login`
- **无需认证**

**Request Body**：

```json
{
  "userName": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**Response** `Result<LoginResponse>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userInfo": {
      "userId": "USR_admin001",
      "userName": "admin",
      "realName": "系统管理员",
      "phone": "13800000000",
      "email": null,
      "avatarUrl": null,
      "userType": 1,
      "role": "超级管理员"
    }
  }
}
```

**错误码**：
- 400: 用户名或密码错误 / 账号已被禁用

---

### 1.2 用户注册

注册新用户，返回用户 ID。

- **URL**: `POST /api/user/register`
- **无需认证**

**Request Body**：

```json
{
  "userName": "zhangsan",
  "password": "123456",
  "realName": "张三",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "userType": 2
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | 是 | 用户名，唯一 |
| password | String | 是 | 密码 |
| realName | String | 是 | 真实姓名 |
| phone | String | 否 | 手机号，需符合 `^1[3-9]\d{9}$` |
| email | String | 否 | 邮箱 |
| userType | Integer | 否 | 类型：1=管理员 2=患者 3=医生，默认 2 |

**Response** `Result<Map<String, String>>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": "USR_xxxxxxxxxxxx"
  }
}
```

**错误码**：
- 400: 用户名已存在 / 参数校验不通过

---

### 1.3 获取当前用户信息

获取当前登录用户的详细信息（含角色名称）。

- **URL**: `GET /api/user/info`
- **需认证**

**Response** `Result<UserInfoVO>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "userId": "USR_admin001",
    "userName": "admin",
    "realName": "系统管理员",
    "phone": "13800000000",
    "email": null,
    "avatarUrl": null,
    "userType": 1,
    "role": "超级管理员"
  }
}
```

| UserInfoVO 字段 | 类型 | 说明 |
|-----------------|------|------|
| userId | String | 用户唯一标识 |
| userName | String | 用户名 |
| realName | String | 真实姓名 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| avatarUrl | String | 头像 URL |
| userType | Integer | 类型：0=医生 1=管理员 2=患者 |
| role | String | 角色名称（多角色逗号分隔） |

**错误码**：
- 401: 未登录

---

### 1.4 更新个人信息

更新当前登录用户的个人信息（真实姓名、手机号、邮箱）。

- **URL**: `PUT /api/user/update`
- **需认证**

**Request Body**：

```json
{
  "realName": "新姓名",
  "phone": "13900000001",
  "email": "new@example.com"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| realName | String | 否 | 真实姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |

**Response**：`Result<String>` — 返回 `"修改成功"`

**错误码**：
- 401: 未登录

---

### 1.5 重置密码

验证旧密码后重置为新密码。

- **URL**: `PUT /api/user/reset-password`
- **需认证**

**Request Body**：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

**Response**：`Result<String>` — 返回 `"重置成功"`

**错误码**：
- 400: 旧密码错误
- 401: 未登录

---

### 1.6 获取所有用户列表（管理员）

获取所有用户列表，每位用户附带角色信息。

- **URL**: `GET /api/user/list-all`
- **需认证**，仅管理员（userType=1）可用

**Response** `Result<List<UserInfoVO>>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "userId": "USR_admin001",
      "userName": "admin",
      "realName": "系统管理员",
      "phone": "13800000000",
      "email": null,
      "avatarUrl": null,
      "userType": 1,
      "role": "超级管理员"
    },
    {
      "userId": "USR_doc001",
      "userName": "doctor1",
      "realName": "张伟",
      "phone": "13800000001",
      "email": null,
      "avatarUrl": null,
      "userType": 0,
      "role": "医生"
    }
  ]
}
```

**错误码**：
- 400: 无操作权限，仅管理员可执行此操作
- 401: 未登录

---

## 二、角色权限 — `/api/role`

### 2.1 获取角色列表

获取所有角色，按创建时间升序。

- **URL**: `GET /api/role/list`
- **需认证**

**Response** `Result<List<Role>>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "roleId": "ROLE_ADMIN",
      "roleName": "超级管理员",
      "roleCode": "admin",
      "description": "系统最高权限",
      "status": 1,
      "createTime": "2026-06-15T12:00:00",
      "updateTime": "2026-06-15T12:00:00"
    }
  ]
}
```

| Role 字段 | 类型 | 说明 |
|-----------|------|------|
| roleId | String | 角色唯一标识 |
| roleName | String | 角色名称 |
| roleCode | String | 角色编码 |
| description | String | 角色描述 |
| status | Integer | 状态：0=禁用 1=启用 |
| createTime | LocalDateTime | 创建时间 |
| updateTime | LocalDateTime | 更新时间 |

---

### 2.2 查询用户角色

查询指定用户的角色信息（返回第一个角色）。

- **URL**: `GET /api/role/user-role?userId=USR_doc001`
- **需认证**

**Query Parameters**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | 是 | 用户唯一标识 |

**Response** `Result<Role>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "roleId": "ROLE_DOCTOR",
    "roleName": "医生",
    "roleCode": "doctor",
    "description": "医生角色",
    "status": 1,
    "createTime": "2026-06-15T12:00:00",
    "updateTime": "2026-06-15T12:00:00"
  }
}
```

**错误码**：
- 400: 该用户未分配角色 / 角色不存在

---

### 2.3 分配角色（管理员）

为指定用户分配角色，校验用户、角色存在性及重复分配。

- **URL**: `POST /api/role/assign`
- **需认证**，仅管理员可操作

**Request Body**：

```json
{
  "userId": "USR_doc001",
  "roleId": "ROLE_DOCTOR"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userId | String | 是 | 用户 ID |
| roleId | String | 是 | 角色 ID |

**Response**：`Result<String>` — 返回 `"分配成功"`

**错误码**：
- 400: 用户不存在 / 角色不存在 / 该用户已拥有此角色 / 无操作权限

---

### 2.4 查询角色权限

查询指定角色的权限列表，按排序字段升序返回。

- **URL**: `GET /api/role/permissions?roleId=ROLE_ADMIN`
- **需认证**

**Query Parameters**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | String | 是 | 角色 ID |

**Response** `Result<List<Permission>>`：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "permissionId": "1",
      "permissionName": "用户管理",
      "permissionCode": "user:manage",
      "parentId": "0",
      "type": 0,
      "path": null,
      "sortOrder": 1
    }
  ]
}
```

| Permission 字段 | 类型 | 说明 |
|-----------------|------|------|
| permissionId | String | 权限唯一标识 |
| permissionName | String | 权限名称 |
| permissionCode | String | 权限编码 |
| parentId | String | 父权限 ID，默认 "0" |
| type | Integer | 类型：0=菜单 1=按钮 |
| path | String | 前端路由/API 路径 |
| sortOrder | Integer | 排序号 |

---

### 2.5 更新角色权限（管理员）

先清空原权限关联，再批量插入新权限。

- **URL**: `PUT /api/role/update-permission`
- **需认证**，仅管理员可操作

**Request Body**：

```json
{
  "roleId": "ROLE_DOCTOR",
  "permissionIds": ["3", "31", "32", "33", "2", "23", "4", "42", "43"]
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roleId | String | 是 | 角色 ID |
| permissionIds | String[] | 是 | 权限 ID 列表，至少一项 |

**Response**：`Result<String>` — 返回 `"更新成功"`

**错误码**：
- 400: 无操作权限 / 参数校验不通过

---

## 三、数据模型摘要

### UserInfoVO

| 字段 | 类型 | 说明 |
|------|------|------|
| userId | String | 用户唯一标识 |
| userName | String | 用户名 |
| realName | String | 真实姓名 |
| phone | String | 手机号 |
| email | String | 邮箱 |
| avatarUrl | String | 头像 URL |
| userType | Integer | 类型：0=医生 1=管理员 2=患者 |
| role | String | 角色名称（多角色用逗号分隔） |

### Role

| 字段 | 类型 | 说明 |
|------|------|------|
| roleId | String | 角色唯一标识，如 ROLE_ADMIN |
| roleName | String | 角色名称 |
| roleCode | String | 角色编码 |
| description | String | 角色描述 |
| status | Integer | 状态：0=禁用 1=启用 |

### Permission

| 字段 | 类型 | 说明 |
|------|------|------|
| permissionId | String | 权限唯一标识 |
| permissionName | String | 权限名称 |
| permissionCode | String | 权限编码 |
| parentId | String | 父权限 ID |
| type | Integer | 类型：0=菜单 1=按钮 |
| path | String | 前端路由/API 路径 |
| sortOrder | Integer | 排序号 |

### Result\<T\>

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 200=成功，4xx=客户端错误，5xx=服务端错误 |
| message | String | 提示信息 |
| data | T | 业务数据 |
