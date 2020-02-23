package com.my.project.api.core;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/23 下午9:12
 */
@Data
@Builder
public class ApiResponse extends Error implements Serializable {

    private String code;
    private String msg;

    public ApiResponse() {};

    public ApiResponse(String code) {
        this.code = code;
    }

    public ApiResponse(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
