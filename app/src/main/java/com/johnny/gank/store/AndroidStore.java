package com.johnny.gank.store;
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

import com.johnny.gank.action.ActionType;
import com.johnny.gank.action.Key;
import com.johnny.gank.action.RxAction;
import com.johnny.gank.data.ui.GankNormalItem;
import com.johnny.gank.dispatcher.Dispatcher;

import java.util.List;

import javax.inject.Inject;

/**
 * description
 *
 * @author Johnny Shieh (JohnnyShieh17@gmail.com)
 * @version 1.0
 *
 * 在一个Flux应用中，Stores和Views都是自控制的，他们不会响应外部的对象。
 * Actions通过Stores注册在Dispatcher中的回调接口获取，而不是通过set方法。
 */
public class AndroidStore extends RxStore {

    public static final String ID = "AndroidStore";

    private int mPage;
    private List<GankNormalItem> mGankList;

    @Inject
    public AndroidStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void onRxAction(RxAction action) {
        switch (action.getType()) {
            /**
             *  当有新的Action进来的时候，它会负责处理Action，
             */
            case ActionType.GET_ANDROID_LIST:
                mPage = action.get(Key.PAGE);
                /**
                 * 并转化成UI需要的数据。
                 */
                mGankList = action.get(Key.GANK_LIST);
                break;
            default:
                return;
        }
        /**
         * Store对外仅仅提供get方法，它的更新通过Dispatcher派发的Action来更新，
         */
        postChange(new RxStoreChange(ID, action));
    }

    /**
     * Store的设计是很精巧的（比较类似PresentationModel模式），
     * 每一个Store仅仅负责一片逻辑相关的UI区域，用来维护这片UI的状态
     * @return
     */
    public int getPage() {
        return mPage;
    }

    public List<GankNormalItem> getGankList() {
        return mGankList;
    }
}
