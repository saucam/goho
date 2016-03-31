package com.goho.service

import com.goho.conf.TestGoHoConf

/**
 * Created by yash.datta on 31/03/16.
 */
class TestGoHoService extends GoHoService
    with TestGoHoConf {

  override val rateLimiter = new RateLimiter(refreshRate, enableRate)

}
