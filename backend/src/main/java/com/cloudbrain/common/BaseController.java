package com.cloudbrain.common;

/** 控制器基类，统一路径前缀 /api */
public abstract class BaseController {

    protected <T> Result<T> success() {
        return Result.ok();
    }

    protected <T> Result<T> success(T data) {
        return Result.ok(data);
    }

    protected <T> Result<T> success(String message, T data) {
        return Result.ok(message, data);
    }

    protected <T> Result<T> fail(String message) {
        return Result.fail(message);
    }
}
