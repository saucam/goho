package com.goho.conf

import java.util.concurrent.{Executors, ExecutorService, ThreadFactory}
import scala.concurrent.{ExecutionContext}
import com.google.common.util.concurrent.ThreadFactoryBuilder

/**
 * Created by yash.datta on 30/03/16.
 */

trait GoHoConf {
  // Specify overridable properties here
  val refreshRate = 10
  val enableRate = 300
}

object GoHoConf {

  // Initialize thread pool for parallel query execution
  // We use a cached pool for the expected
  // numerous short-lived (sub-second) queries
  val gohoThreadFactory: ThreadFactory = new ThreadFactoryBuilder()
    .setNameFormat("goho-query-pool-thread-%d").build()

  val javaExecutorService: ExecutorService = {
    Executors.newCachedThreadPool(gohoThreadFactory)
  }

  val gohoExecutorService = ExecutionContext.fromExecutorService(javaExecutorService)

}
