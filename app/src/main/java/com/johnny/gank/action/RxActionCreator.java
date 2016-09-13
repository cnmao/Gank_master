package com.johnny.gank.action;


import com.johnny.gank.dispatcher.Dispatcher;
import com.johnny.gank.util.SubscriptionManager;

import android.support.annotation.NonNull;

import rx.Subscription;

/**
 * This class must be extended in order to give useful functionality to create RxAction.
 * 创建的Create和SubscriptionManager放置到一起了
 */

/**
 * API:
 * 在Flux架构中，ActionCreator虽然和Flux数据流关系不大，但对于App来说，实际上是非常重要的一部分。
 * 它是对UserCase的抽象，每一个UserCase都可以抽象成ActionCreator的一个方法。
 *
 * 扩展的ActionCreator语义
 就像刚开始分析的那样，ActionCreator负责的是数据的准备和预处理，而网络请求只是个例。
 所以，实际上ActionCreator给我们提供了一个处理一般业务逻辑的地方。
 这些业务和Flux数据流和UI逻辑没有关系，更多的是App的业务逻辑，
 比如在给一个手机号发送验证码之前（这也是一个网络请求，但是需要手机号作为payload），需要验证用户输入的手机号是否合法。
 再比如，做本地图片选择器的时候，需要调用图片库获取图片数据，这些类似操作，都是App的业务逻辑负责准备数据的事情，
 并不是App的UI逻辑，所以应该放在ActionCreator来做（
 Store对处理这类逻辑有很强的诱惑力，在Store中处理也是可以的，
 但是要记的不要在Store里面直接更新Store状态，要通过Action来更新）。
 */

/**
 * 这段话主要是说一般网络写操作在ActionCreator中来做，网络读操作在Store中来做。
 * 理由是，ActionCreator在做网络请求时的职责，1). 规范化Action的创建 2). 协调乐观的网络操作，处理请求成功或者失败的情况的
 * 。但是在网络读操作的时候不需要这些操作，所以也不需要放在ActionCreator中完成。
 */
public abstract class RxActionCreator {

  private final Dispatcher dispatcher;
  private final SubscriptionManager manager;

  public RxActionCreator(Dispatcher dispatcher, SubscriptionManager manager) {
    this.dispatcher = dispatcher;
    this.manager = manager;
  }

  public void addRxAction(RxAction rxAction, Subscription subscription) {
    manager.add(rxAction, subscription);


  }

  public boolean hasRxAction(RxAction rxAction) {
    return manager.contains(rxAction);
  }

  public void removeRxAction(RxAction rxAction) {
    manager.remove(rxAction);
  }

  public RxAction newRxAction(@NonNull String actionId, @NonNull Object... data) {
    if (actionId.isEmpty()) {
      throw new IllegalArgumentException("Type must not be empty");
    }

    if (data.length % 2 != 0) {
      throw new IllegalArgumentException("Data must be a valid list of key,value pairs");
    }

    RxAction.Builder actionBuilder = RxAction.type(actionId);   // 内部类建造者模式，返回当前类别对象
    int i = 0;
    while (i < data.length) {
      String key = (String) data[i++];
      Object value = data[i++];
      actionBuilder.bundle(key, value);
    }
    return actionBuilder.build();
  }

  /**
   * 并通过Dispatcher发送给Store
   * @param action
     */
  public void postRxAction(@NonNull RxAction action) {
    dispatcher.postRxAction(action);
  }

  public void postError(@NonNull RxAction action, Throwable throwable) {
    dispatcher.postRxAction(RxError.newRxError(action, throwable));
  }























}
