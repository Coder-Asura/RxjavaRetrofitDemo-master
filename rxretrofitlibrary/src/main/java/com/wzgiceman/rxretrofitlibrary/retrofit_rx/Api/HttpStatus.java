package com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author Created by Asura on 2018/2/7 10:37.
 */
public class HttpStatus {
    private static final int RESP_CODE_SUCCESS = 1;
    @SerializedName("status")
    @Expose
    private int code;
    @SerializedName("msg")
    @Expose
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * API是否请求失败
     *
     * @return 失败返回true, 成功返回false
     */
    public boolean isCodeInvalid() {
        return code != RESP_CODE_SUCCESS;
    }
}
