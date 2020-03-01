package com.my.project.api.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author stranger_alone
 * @description TODO
 * @date 2020/2/23 下午9:12
 */
@Data
@Builder
public class ApiResponse implements Serializable {

    private Integer code = HttpStatus.OK.value();
    private String msg;

    public ApiResponse() {};

    public ApiResponse(Integer code) {
        this.code = code;
    }

    public ApiResponse(String msg) {
        this.msg = msg;
    }

    public ApiResponse(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ApiResponse buildErrorResponse(Integer code) {

        ApiResponse response = ApiResponse.builder()
                                          .code(code)
                                          .build();
        return response;
    }

    public static ApiResponse buildErrorResponse(String msg) {

        ApiResponse response = ApiResponse.builder()
                                          .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                          .msg(msg)
                                          .build();
        return response;
    }

    public static ApiResponse buildErrorResponse(Integer code, String msg) {

        ApiResponse response = ApiResponse.builder()
                                          .code(code)
                                          .msg(msg).build();
        return response;
    }
}
