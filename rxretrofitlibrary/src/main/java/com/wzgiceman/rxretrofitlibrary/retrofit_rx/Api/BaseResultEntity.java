package com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api;

import com.google.gson.annotations.Expose;

/**
 * 回调信息统一封装类
 * Created by WZG on 2016/7/16.
 */
public class BaseResultEntity<T> {
    //  判断标示
    @Expose
    private int status;
    //    提示信息
    @Expose
    private String msg;
    //显示数据（用户需要关心的数据）
    @Expose
    private T data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "BaseResultEntity{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
