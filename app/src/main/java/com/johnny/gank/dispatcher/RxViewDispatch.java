package com.johnny.gank.dispatcher;


import com.johnny.gank.action.RxError;
import com.johnny.gank.store.RxStoreChange;

import android.support.annotation.NonNull;


/**
 * Created by marcel on 10/09/15.
 *
 * Activities or Fragments implementing this interface will be part of the RxFlux flow. Implement the methods in
 * order to get the proper callbacks and un/register stores accordingly to Flux flow.
 */
public interface RxViewDispatch {

  /**
   * 当刷新动作出现时。Store变化
   * All the stores will call this event after they process an action and the store change it.
   * The view can react and request the needed data
   */
  void onRxStoreChanged(@NonNull RxStoreChange change);

  /**
   * 刷新失败等
   * Called when an error occur in some point of the flux flow.
   *
   * @param error {@link RxError} containing the information for that specific error
   */
  void onRxError(@NonNull RxError error);

}
