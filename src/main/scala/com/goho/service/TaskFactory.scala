package com.goho.service

/**
 * Created by yash.datta on 30/03/16.
 */
import java.util.concurrent.{Executors, ExecutorService, ThreadFactory}

import com.goho.conf.GoHoConf._
import scalaz.concurrent.Task

/**
 * Basic Building Blocks for creating tasks for executing sql queries
 */
object TaskFactory {

  /**
   * Returns the task by wrapping the given closure computation in a task
   * Adds careExecutorService as the pool to be used for executing this task
   *
   * @param a - Computation to be performed by the task
   * @param execService - Executor Thread Pool to be used for executing this task
   * @tparam A - Desired Ouput Type
   * @return - Scalaz's Task
   */
  def getTask[A](a: => A)(implicit execService: ExecutorService = gohoExecutorService): Task[A] = Task(a)(execService)

}