package com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception;

import com.wzgiceman.rxretrofitlibrary.R;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.RxRetrofitApp;

/**
 * 自定义错误信息，统一处理返回处理
 * Created by WZG on 2016/7/16.
 */
public class ApiException extends RuntimeException {
    private int errorCode;
    private String msg;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getInfo() {
        return errorCode + "-->" + msg;
    }

    public static final int ERROR_CUSTOM = 0x00;
    public static final int ERROR_NO_NET = 0x02;
    public static final int ERROR_GSON = 0x03;

    public ApiException(int resultCode) {
        this.errorCode = resultCode;
        this.msg = getExceptionMessage(resultCode);
    }

    public ApiException(String detailMessage) {
        super(detailMessage);
        this.errorCode = ERROR_CUSTOM;
        this.msg = detailMessage;
    }

    /**
     * 转换错误数据
     *
     * @param code 错误码
     * @return 错误提示
     */
    private static String getExceptionMessage(int code) {
        int messageId;
        switch (code) {
            case ERROR_CUSTOM:
                messageId = R.string.error_0;
                break;
            case ERROR_NO_NET:
                messageId = R.string.error_2;
                break;
            case ERROR_GSON:
                messageId = R.string.error_3;
                break;
            //自定义错误提示
//            case 30010:
//                messageId = R.string.error_30010;
//                break;
            default:
                messageId = R.string.error_default;
                break;
        }
        return RxRetrofitApp.getApplication().getResources().getString(messageId);
    }
}

