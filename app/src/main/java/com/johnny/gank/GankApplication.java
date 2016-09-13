package com.johnny.gank;
/*
 * Copyright (C) 2016 Johnny Shieh Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.johnny.gank.di.component.AppComponent;
import com.johnny.gank.di.component.DaggerAppComponent;
import com.johnny.gank.di.module.AppModule;
import com.johnny.gank.util.AppUtil;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.Settings;
import com.squareup.leakcanary.LeakCanary;
import com.umeng.analytics.MobclickAgent;

import android.app.Application;
import android.util.Log;

/**
 * @author Johnny Shieh (JohnnyShieh17@gmail.com)
 * @version 1.0
 */
public class GankApplication extends Application{

    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // 友盟统计
        MobclickAgent.openActivityDurationTrack(false);
        MobclickAgent.enableEncrypt(true);
        // 阿里百川 web网页形式进行反馈信息汇总
        FeedbackAPI.initAnnoy(this, getString(R.string.ali_app_key));
        // AppUtil 初始化了一个Context对象
        AppUtil.init(this);
        // 日志工具 Simple, pretty and powerful logger for android
        // https://github.com/orhanobut/logger
        Logger.initialize(
            new Settings()
                .isShowMethodLink(true)
                .isShowThreadInfo(false)
                .setMethodOffset(0)
                .setLogPriority(BuildConfig.DEBUG ? Log.VERBOSE : Log.ASSERT)
        );
        // 初始化依赖注入组件
        initInjector();
        //内存监视对象
        LeakCanary.install(this);
    }

    private void initInjector() {
        mAppComponent = DaggerAppComponent.builder() // 内部类 返回当前内部类Builder对象
            .appModule(new AppModule(this)) // 获取AppModule  传入AppModule对象，检测然后返回 从而获取到AppMpdule
            .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

}
