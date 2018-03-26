package com.wzgiceman.rxretrofitlibrary.retrofit_rx.http;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api.BaseApi;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.Api.BaseResultEntity;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.RxRetrofitApp;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.converter.CustomGsonConverterFactory;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.http.cookie.CookieInterceptor;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.listener.HttpOnNextListener;
import com.wzgiceman.rxretrofitlibrary.retrofit_rx.subscribers.ProgressSubscriber;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;


/**
 * http交互处理类
 * Created by WZG on 2016/7/16.
 */
public class HttpManager<T> {
    private volatile static HttpManager INSTANCE;

    //构造方法私有
    private HttpManager() {
    }

    //获取单例
    public static HttpManager getInstance() {
        if (INSTANCE == null) {
            synchronized (HttpManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new HttpManager();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 处理http请求
     *
     * @param basePar 封装的请求数据
     */
    public <T> void doHttpDeal(BaseApi<T> basePar) {
        //手动创建一个OkHttpClient并设置超时时间缓存等设置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(basePar.getConnectionTime(), TimeUnit.SECONDS);
        builder.addInterceptor(new CookieInterceptor(basePar.isCache(), basePar.getUrl()));
        if (RxRetrofitApp.isDebug()) {
            builder.addInterceptor(getHttpLoggingInterceptor());
        }
        // Gson生成json时候，会忽略掉值为null的key
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
        /*创建retrofit对象*/
        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
//                .addConverterFactory(GsonConverterFactory.create(gson))
                //自定义Gson转换工厂，统一处理错误码
                .addConverterFactory(CustomGsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .baseUrl(basePar.getBaseUrl())
                .build();

        /*rx处理*/
        ProgressSubscriber<T> subscriber = new ProgressSubscriber<T>(basePar);
        Observable<T> observable = basePar.getObservable(retrofit)
                 /*失败后的retry配置*/
//                .retryWhen(new RetryWhenNetworkException(basePar.getRetryCount(),
//                        basePar.getRetryDelay(), basePar.getRetryIncreaseDelay()))
                /*生命周期管理*/
                .compose(basePar.getRxAppCompatActivity().<BaseResultEntity<T>>bindUntilEvent(ActivityEvent.PAUSE))
                /*http请求线程*/
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                /*回调线程*/
                .observeOn(AndroidSchedulers.mainThread())
                /*结果判断*/
                .map(basePar);


        /*链接式对象返回*/
        HttpOnNextListener httpOnNextListener = basePar.getListener();
        if (httpOnNextListener != null) {
            httpOnNextListener.onNext(observable);
        }
        /*数据回调*/
        observable.subscribe(subscriber);
    }


    /**
     * 日志输出
     * 自行判定是否添加
     *
     * @return
     */
    private HttpLoggingInterceptor getHttpLoggingInterceptor() {
        //日志显示级别
        HttpLoggingInterceptor.Level level = HttpLoggingInterceptor.Level.BODY;
        //新建log拦截器
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("RxRetrofit", "Retrofit====Message:" + message);
            }
        });
        loggingInterceptor.setLevel(level);
        return loggingInterceptor;
    }
}
