package com.goho.service

/**
 * Created by yash.datta on 31/03/16.
 */
trait AuthService {

  // Authenticate by key
  def accept(key: String): Boolean
}
