package com.johnny.gank.dispatcher;

import com.johnny.gank.action.RxAction;
import com.johnny.gank.action.RxError;
import com.johnny.gank.store.RxStoreChange;
import com.johnny.gank.util.LoggerManager;

import android.support.v4.util.ArrayMap;

import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by marcel on 13/08/15.
 * RxFlux dispatcher, contains the the registered actions, stores and the instance of the RxBus
 * responsible to send events to the stores. This class is used as a singleton.
 */

/**
 * Dispatcher 作为Flux应用中数据流的中转枢纽，采用单例模式。
 * 它的本质是注册回调接口，并且可以有序的调用它们。
 * 每一个 Store 都在 Dispatcher中进行注册。
 * 当新的数据进入到 Dispatcher中时，它（Dispatcher）会把数据发送给每一个 Stores。
 * 通过 dispatch()方法把数据发送到各个Store，并且 dispatch()只有一个传送数据的对象(data-payload-object)作为参数，
 * 而它实际上就是Action。
 */
public class Dispatcher {

  private static Dispatcher instance;
  private final RxBus bus;
  private final LoggerManager logger;

  // 在Dispatcher中有关系的是Action和Store ，dispatcher将
  private ArrayMap<String, Subscription> rxActionMap;  // <当前类名 , >
  private ArrayMap<String, Subscription> rxStoreMap;

  private Dispatcher(RxBus bus) {
    this.bus = bus;
    this.rxActionMap = new ArrayMap<>();
    this.rxStoreMap = new ArrayMap<>();
    this.logger = new LoggerManager();
  }

  public static synchronized Dispatcher getInstance(RxBus rxBus) {
    if (instance == null) instance = new Dispatcher(rxBus);
    return instance;
  }

  public <T extends RxActionDispatch> void subscribeRxStore(final T object) {
    final String tag = object.getClass().getSimpleName();
    /**
     * 所有订阅了这个Action的Store会接收到订阅的Action并消化Action，
     */
    Subscription subscription = rxActionMap.get(tag);
    if (subscription == null || subscription.isUnsubscribed()) {
      logger.logRxStoreRegister(tag);
      rxActionMap.put(tag, bus.get().filter(new Func1<Object, Boolean>() {
        @Override public Boolean call(Object o) {
          return o instanceof RxAction; // 通过 dispatch()方法把数据发送到各个Store，
          // 并且 dispatch()只有一个传送数据的对象(data-payload-object)作为参数，而它实际上就是Action。
        }
      }).subscribe(new Action1<Object>() {
        @Override public void call(Object o) {
          logger.logRxAction(tag, (RxAction) o);
          object.onRxAction((RxAction) o); // 当前类名和当前action进行关联  action 和 一个达成的订阅者
        //  object 就是一个Store
        }
      }));
    }
  }

  public <T extends RxViewDispatch> void subscribeRxError(final T object) {
    final String tag = object.getClass().getSimpleName() + "_error";
    Subscription subscription = rxActionMap.get(tag);
    if (subscription == null || subscription.isUnsubscribed()) {
      rxActionMap.put(tag, bus.get().filter(new Func1<Object, Boolean>() {
        @Override public Boolean call(Object o) {
          return o instanceof RxError;
        }
      }).subscribe(new Action1<Object>() {
        @Override public void call(Object o) {
          logger.logRxError(tag, (RxError) o);
          object.onRxError((RxError) o);
        }
      }));
    }
  }

  /**
   * 然后Store会发送UI状态改变的事件给相关的Activity（或Fragment)，
   * Activity在收到状态发生改变的事件之后，开始更新UI（更新UI的过程中会从Store获取所有需要的数据）。
   * @param object
   * @param <T>
     */
  // 进行关联 《 看到没 这里边的T 代表的就是RxViewDispatch 接口的对象 。 因为传递的是接口，所以经过了Instances的处理。》
  public <T extends RxViewDispatch> void subscribeRxView(final T object) {
    final String tag = object.getClass().getSimpleName(); //  AndroidFragment

    Subscription subscription = rxStoreMap.get(tag); // 判断是否已经建立联系
    if (subscription == null || subscription.isUnsubscribed()) { // 当为 null 或者是  Indicates whether this {@code Subscription} is currently unsubscribed.
      logger.logViewRegisterToStore(tag); // 绑定View到Store中？
      rxStoreMap.put(tag, bus.get().filter(new Func1<Object, Boolean>() {
        @Override public Boolean call(Object o) {
          return o instanceof RxStoreChange;
        }
      }).subscribe(new Action1<Object>() {
        @Override public void call(Object o) {
          logger.logRxStore(tag, (RxStoreChange) o);
          object.onRxStoreChanged((RxStoreChange) o);
        }
      }));
    }
    subscribeRxError(object);
  }

  public <T extends RxActionDispatch> void unsubscribeRxStore(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxActionMap.get(tag);
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
      rxActionMap.remove(tag);
      logger.logUnregisterRxAction(tag);
    }
  }

  public <T extends RxViewDispatch> void unsubscribeRxError(final T object) {
    String tag = object.getClass().getSimpleName() + "_error";
    Subscription subscription = rxActionMap.get(tag);
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
      rxActionMap.remove(tag);
    }
  }

  public <T extends RxViewDispatch> void unsubscribeRxView(final T object) {
    String tag = object.getClass().getSimpleName();
    Subscription subscription = rxStoreMap.get(tag);
    if (subscription != null && !subscription.isUnsubscribed()) {
      subscription.unsubscribe();
      rxStoreMap.remove(tag);
      logger.logUnregisterRxStore(tag);
    }
    unsubscribeRxError(object);
  }

  public synchronized void unsubscribeAll() {
    for (Subscription subscription : rxActionMap.values()) {
      subscription.unsubscribe();
    }

    for (Subscription subscription : rxStoreMap.values()) {
      subscription.unsubscribe();
    }

    rxActionMap.clear();
    rxStoreMap.clear();
  }

  /**
   * 并通过Dispatcher发送给Store
   * 所有订阅了这个Action的Store会接收到订阅的Action并消化Action
   * @param action
     */
  public void postRxAction(final RxAction action) {
    bus.send(action);
  }

  /**
   * 所以它会提供一些公有方法来注册监听(* subscribeRxStore)和分发消息。
   * @param storeChange
     */
  public void postRxStoreChange(final RxStoreChange storeChange) {
    bus.send(storeChange);
  }
}




















