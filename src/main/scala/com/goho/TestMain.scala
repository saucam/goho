package com.goho

import org.http4s._, org.http4s.dsl._
import org.http4s.server.blaze._
/**
 * Created by yash.datta on 30/03/16.
 */
object TestMain {

  def main(args: Array[String]): Unit = {

    val service = HttpService {
      case GET -> Root / "hello" / name =>
        Ok(s"Hello, $name.")
    }

    val builder = BlazeBuilder.mountService(service)

    val server = builder.run
  }

}
