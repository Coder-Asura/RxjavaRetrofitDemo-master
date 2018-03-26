package com.wzgiceman.rxretrofitlibrary.retrofit_rx.subscribers;

import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.asura.loadingdialoglibrary.LoadingDialog;
import com.google.gson.JsonSyntaxException;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;
import com.wzgiceman.rxretrofitlibrary.R;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api.BaseApi;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.RxRetrofitApp;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.ApiException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.exception.TokenOutDateException;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.http.cookie.CookieResulte;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.utils.AppUtil;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.utils.CookieDbUtil;

import java.lang.ref.SoftReference;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;


/**
 * 用于在Http请求开始时，自动显示一个ProgressDialog
 * 在Http请求结束是，关闭ProgressDialog
 * 调用者自己对请求数据进行处理
 * Created by WZG on 2016/7/16.
 */
public class ProgressSubscriber<T> implements Observer<T> {
    /*是否弹框*/
    private boolean showProgress = true;
    /* 回调接口*/
    private HttpOnNextListener<T> mSubscriberOnNextListener;
    /*软引用反正内存泄露*/
    private SoftReference<RxAppCompatActivity> mActivity;
    /*加载框可自己定义*/
    private LoadingDialog mLoadingDialog;
    /*请求数据*/
    private BaseApi<T> api;

    private Disposable mDisposable;


    /**
     * 构造
     *
     * @param api
     */
    public ProgressSubscriber(BaseApi<T> api) {
        this.api = api;
        this.mSubscriberOnNextListener = api.getListener();
        this.mActivity = new SoftReference<>(api.getRxAppCompatActivity());
        setShowProgress(api.isShowProgress());
        if (api.isShowProgress()) {
            initProgressDialog(api.isCancel());
        }
    }

    /**
     * 初始化加载框
     */
    private void initProgressDialog(boolean cancel) {
        Context context = mActivity.get();
        if (mLoadingDialog == null && context != null) {
            mLoadingDialog = new LoadingDialog(context);
            mLoadingDialog.setMessage(context.getResources().getString(R.string.requesting));
            if (cancel) {
                mLoadingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        if (mSubscriberOnNextListener != null) {
                            mSubscriberOnNextListener.onCancel();
                        }
                        onCancelProgress();
                    }
                });
            }
        }
    }


    /**
     * 显示加载框
     */
    private void showProgressDialog() {
        if (!isShowProgress()) {
            return;
        }
        Context context = mActivity.get();
        if (mLoadingDialog == null || context == null) {
            return;
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }


    /**
     * 隐藏
     */
    private void dismissProgressDialog() {
        if (!isShowProgress()) {
            return;
        }
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    /**
     * 对错误进行统一处理
     * 隐藏ProgressDialog
     *
     * @param e
     */
    @Override
    public void onError(Throwable e) {
        dismissProgressDialog();
        /*需要緩存并且本地有缓存才返回*/
        if (api.isCache()) {
            Observable.just(api.getUrl()).subscribe(new Observer<String>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(String s) {
                    /*获取缓存数据*/
                    CookieResulte cookieResulte = CookieDbUtil.getInstance().queryCookieBy(s);
                    if (cookieResulte == null) {
                        throw new ApiException("网络错误");
                    }
                    long time = (System.currentTimeMillis() - cookieResulte.getTime()) / 1000;
                    if (time < api.getCookieNoNetWorkTime()) {
                        if (mSubscriberOnNextListener != null) {
                            mSubscriberOnNextListener.onCacheNext(cookieResulte.getResulte());
                        }
                    } else {
                        CookieDbUtil.getInstance().deleteCookie(cookieResulte);
                        throw new ApiException("网络错误");
                    }
                }

                @Override
                public void onError(Throwable e) {
                    errorDo(e);
                }

                @Override
                public void onComplete() {

                }
            });
        } else {
            errorDo(e);
        }
    }

    /**
     * 完成，隐藏ProgressDialog
     */
    @Override
    public void onComplete() {
        dismissProgressDialog();
    }

    /*错误统一处理*/
    private void errorDo(Throwable e) {
        ApiException throwable = null;
        Context context = mActivity.get();
        if (context == null) {
            return;
        }
        Log.e("Alog", "错误", e);
        if (e instanceof UnknownHostException
                || e instanceof SocketTimeoutException
                || e instanceof ConnectException) {
            throwable = new ApiException(ApiException.ERROR_NO_NET);
        } else if (e instanceof TokenOutDateException) {
            if (mSubscriberOnNextListener != null) {
                mSubscriberOnNextListener.onLoginTokenTimeOut(e);
                return;
            }
        } else if (e instanceof JsonSyntaxException) {
            throwable = new ApiException(ApiException.ERROR_GSON);
        } else if (e instanceof ApiException) {
            throwable = (ApiException) e;
        } else {
            throwable = new ApiException(e.getMessage());
        }
        if (mSubscriberOnNextListener != null) {
            mSubscriberOnNextListener.onError(throwable);
        }
    }

    /**
     * 订阅开始时调用
     * 显示ProgressDialog
     */
    @Override
    public void onSubscribe(Disposable d) {
        mDisposable = d;
        showProgressDialog();
        /*缓存并且有网*/
        if (api.isCache() && AppUtil.isNetworkAvailable(RxRetrofitApp.getApplication())) {
             /*获取缓存数据*/
            CookieResulte cookieResulte = CookieDbUtil.getInstance().queryCookieBy(api.getUrl());
            if (cookieResulte != null) {
                long time = (System.currentTimeMillis() - cookieResulte.getTime()) / 1000;
                if (time < api.getCookieNetWorkTime()) {
                    if (mSubscriberOnNextListener != null) {
                        mSubscriberOnNextListener.onCacheNext(cookieResulte.getResulte());
                    }
                    onComplete();
                    mDisposable.dispose();
                }
            }
        }
    }

    /**
     * 将onNext方法中的返回结果交给Activity或Fragment自己处理
     *
     * @param t 创建Subscriber时的泛型类型
     */
    @Override
    public void onNext(T t) {
        if (mSubscriberOnNextListener != null) {
            mSubscriberOnNextListener.onNext(t);
        }
    }

    /**
     * 取消ProgressDialog的时候，取消对observable的订阅，同时也取消了http请求
     */
    public void onCancelProgress() {
        if (!mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }


    public boolean isShowProgress() {
        return showProgress;
    }

    /**
     * 是否需要弹框设置
     *
     * @param showProgress
     */
    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
    }
}