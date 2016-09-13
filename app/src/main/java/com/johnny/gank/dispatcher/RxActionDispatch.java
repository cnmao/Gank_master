package com.johnny.gank.dispatcher;

import com.johnny.gank.action.RxAction;

/**
 * This interface must be implemented by the store
 */
public interface RxActionDispatch {

  /**
   * 在AndroidFlux中Dispatcher是就是一个发布-订阅模式。
   * Store会在这里注册自己的回调接口，Dispatcher会把Action分发到注册的Store，
   * 所以它会提供一些公有方法来注册监听和分发消息。
   *
   * Actions通过Stores注册在Dispatcher中的回调接口获取，而不是通过set方法。
   * @param action
     */
  void onRxAction(RxAction action);
}
