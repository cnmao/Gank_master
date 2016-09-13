package com.johnny.gank.action;
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

import com.johnny.gank.core.http.GankService;
import com.johnny.gank.data.response.GankData;
import com.johnny.gank.data.ui.GankNormalItem;
import com.johnny.gank.dispatcher.Dispatcher;
import com.johnny.gank.util.SubscriptionManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * The Action Creator used to pull a category gank data.
 *
 * @author Johnny Shieh (JohnnyShieh17@gmail.com)
 * @version 1.0
 */
abstract class CategoryGankActionCreator extends RxActionCreator{

    private static final int DEFAULT_PAGE_COUNT = 17;

    public CategoryGankActionCreator(Dispatcher dispatcher,
        SubscriptionManager manager) {
        super(dispatcher, manager);
    }

    protected abstract String getActionId();

    protected int getPageCount() {
        return DEFAULT_PAGE_COUNT;
    }

    /**
     * 如图所示，在FluxApp中，网络操作是在ActionCreator部分执行的。
     * 之所以这样是因为，Flux的整体架构是一个单向数据流，
     * 数据从Action开始最终经过Dispatcher、Store流向View，
     * 在这个过程中ActionCreator属于数据的预处理和准备阶段。
     * 所以像网络请求、文件处理、数据预处理等等都应该在这里完成，
     * 最后产出一个结果通过Action启动整个数据流。
     * @param category
     * @param page
     */
    protected void getGankList(final String category, final int page) {
        /**
         * ActionCreator会根据传入参数创建Action并通过Dispatcher发送给Store
         */
        final RxAction rxAction = newRxAction(getActionId()); // 根据不同的子类ID 进行构建不同的Action ， 然后调用父顶类的 magger对象
        if(hasRxAction(rxAction)) {
            return; // 最后产出一个结果通过Action启动整个数据流。
        }

        addRxAction(rxAction, GankService.Factory.getGankService()
            .getGank(category, getPageCount(), page)
            .map(new Func1<GankData, List<GankNormalItem>>() {
                @Override
                public List<GankNormalItem> call(GankData gankData) {
                    if(null == gankData || null == gankData.results || 0 == gankData.results.size()) {
                        return null;
                    }
                    return GankNormalItem.newGankList(gankData.results, page);
                }
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Action1<List<GankNormalItem>>() {
                @Override
                public void call(List<GankNormalItem> gankNormalItems) {
                    rxAction.getData().put(Key.GANK_LIST, gankNormalItems); //无论是用户与应用交互还是网络API的响应，
                    // 当新的数据输入系统的时候， 这个数据被包装到 Action — 一个包含了数据和Action类型的对象。
                    // 我们经常会创建一个类(ActionCreator)包含各种帮助方法，这些方法不仅创建Action，并且把Action传递给Dispatcher。
                    rxAction.getData().put(Key.PAGE, page);
                    /**
                     * 最后产出一个结果通过Action启动整个数据流。
                     */
                    postRxAction(rxAction);
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {
                    postError(rxAction, throwable);
                }
            }));
    }
}
