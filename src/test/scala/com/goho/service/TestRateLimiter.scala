package com.goho.service

/**
 * Created by yash.datta on 31/03/16.
 */

import com.goho.service.AuthorizedKeys._




object TestRateLimiter {

  def setRateMap(key: String, rates: Int): Unit = {
    RateLimiter.rateMap.put(keyMap(key), rates)
  }
}
