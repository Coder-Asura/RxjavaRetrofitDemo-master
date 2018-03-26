package com.asura.loadingdialoglibrary;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author Created by Liuxd on 2018/02/10 11:11.
 *         加载框
 */
public class LoadingDialog extends Dialog {
    private ImageView mImageView;
    private TextView mTextView;
    private Context mContext;

    public LoadingDialog(Context context) {
        this(context, R.style.loadingDialog);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
        init(context);
    }

    /**
     * 给Dialog设置提示信息
     *
     * @param message
     */
    public void setMessage(CharSequence message) {
        if (message != null && message.length() > 0) {
            mTextView.setVisibility(View.VISIBLE);
            mTextView.setText(message);
            mTextView.invalidate();
        }
    }

    private void init(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_loading_dialog, null);
        setContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        mImageView = findViewById(R.id.iv_loading);
        mTextView = findViewById(R.id.tv_message);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }

    @Override
    public void show() {
        super.show();
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_loading);
        if (animation != null) {
            animation.setInterpolator(new LinearInterpolator());
            //开始动画
            mImageView.setAnimation(animation);
            animation.start();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mImageView.clearAnimation();
    }
}
