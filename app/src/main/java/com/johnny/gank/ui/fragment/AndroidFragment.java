package com.johnny.gank.ui.fragment;
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
import com.johnny.gank.action.AndroidActionCreator;
import com.johnny.gank.action.RxError;
import com.johnny.gank.data.ui.GankNormalItem;
import com.johnny.gank.di.component.AndroidFragmentComponent;
import com.johnny.gank.dispatcher.Dispatcher;
import com.johnny.gank.stat.StatName;
import com.johnny.gank.store.AndroidStore;
import com.johnny.gank.store.RxStoreChange;
import com.johnny.gank.ui.activity.MainActivity;
import com.johnny.gank.ui.activity.WebviewActivity;
import com.johnny.gank.ui.adapter.CategoryGankAdapter;
import com.johnny.gank.ui.widget.LoadMoreView;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

/**
 * description
 *
 * @author Johnny Shieh (JohnnyShieh17@gmail.com)
 * @version 1.0
 */
public class AndroidFragment extends CategoryGankFragment {

    public static final String TAG = AndroidFragment.class.getSimpleName();

    @Inject AndroidStore mStore;
    @Inject AndroidActionCreator mActionCreator;
    @Inject Dispatcher mDispatcher;

    protected AndroidFragmentComponent mComponent;

    public static AndroidFragment newInstance() {
        return new AndroidFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initInjector() {
        mComponent = ((MainActivity)getActivity()).getMainActivityComponent().androidFragmentComponent();
        mComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View contentView = createView(inflater, container); // 几个页面都有相同的界面，那就写个方法放在父类中呗
        mAdapter.setOnItemClickListener(new CategoryGankAdapter.OnItemClickListener() {
            @Override
            public void onClickNormalItem(View view, GankNormalItem normalItem) {
                WebviewActivity.openUrl(getActivity(), normalItem.url, normalItem.desc);
            }
        });

        initInjector();
        mDispatcher.subscribeRxStore(mStore);   // mStore = AndroidStore
        mDispatcher.subscribeRxView(this);
        return contentView;
    }

    @Override
    public void onDestroyView() {
        mDispatcher.unsubscribeRxStore(mStore);
        mDispatcher.unsubscribeRxView(this);
        super.onDestroyView();
    }

    @Override
    protected void refreshList() {
        mActionCreator.getAndroidList(1);
    }

    @Override
    protected void loadMore() {
        /**
         * 在Flux中，要始终注意数据是单向流动的，所以要抵制住这种诱惑。
         * Flux的处理方式是在数据开始加载的时候，发送Action给Store，让Store来处理当前的UI状态，
         * 比如显示一个“正在加载…”的对话框。在网络请求成功/失败之后，再发送新的Action给Store处理请求结果。
         * （额外的好处是，避免了常见的由于回调接口造成的内存泄漏问题）
         */
        /**
         * 本来是在ActionCreator中进行请求网络的时候需要 加载中。。。。 提示。 不过提前到了Control——view中了！
         */
        vLoadMore.setStatus(LoadMoreView.STATUS_LOADING);
        /**
         * 按钮被点击触发回调方法，在回调方法中调用ActionCreator提供的有语义的的方法
         */
        mActionCreator.getAndroidList(mAdapter.getCurPage() + 1);
    }

    /**
     * 下边两个方法都是RxViewDispatcher接口的回调
     * 在subscribeView的时候进行绑定的。
     * @param change
     */
    @Override
    public void onRxStoreChanged(@NonNull RxStoreChange change) {
        switch (change.getStoreId()) {
            case AndroidStore.ID:
                /**
                 * 所有订阅了这个Action的Store会接收到订阅的Action并消化Action
                 */
                if(1 == mStore.getPage()) {
                    vRefreshLayout.setRefreshing(false);
                }
                /**
                 * 更新UI的过程中会从Store获取所有需要的数据
                 */
                mAdapter.updateData(mStore.getPage(), mStore.getGankList());
                mLoadingMore = false;
                vLoadMore.setStatus(LoadMoreView.STATUS_INIT);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRxError(@NonNull RxError error) {
        switch (error.getAction().getType()) {
            case ActionType.GET_ANDROID_LIST:
                vRefreshLayout.setRefreshing(false);
                mLoadingMore = false;
                vLoadMore.setStatus(LoadMoreView.STATUS_FAIL);
                break;
            default:
                break;
        }
    }

    @Override
    protected String getStatPageName() {
        return StatName.PAGE_ANDROID;
    }
}
