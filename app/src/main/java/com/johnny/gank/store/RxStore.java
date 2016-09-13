package com.johnny.gank.store;


import com.johnny.gank.action.RxActionCreator;
import com.johnny.gank.dispatcher.Dispatcher;
import com.johnny.gank.dispatcher.RxActionDispatch;

/**
 * This class must be extended by each store of the app in order to recieve the actions dispatched
 * by the {@link RxActionCreator}
 */
//Actions通过Stores注册在Dispatcher中的回调接口获取，而不是通过set方法。

/**
 * 根据action回调 所以有RxActionDispatch
 * 根据
 */
public abstract class RxStore implements RxActionDispatch {

  private final Dispatcher dispatcher;

  public RxStore(Dispatcher dispatcher) {
    this.dispatcher = dispatcher;
  }

  public void register() {
    dispatcher.subscribeRxStore(this);
  }

  public void unregister() {
    dispatcher.unsubscribeRxStore(this);
  }

  protected void postChange(RxStoreChange change) {
    dispatcher.postRxStoreChange(change);
  }

}
