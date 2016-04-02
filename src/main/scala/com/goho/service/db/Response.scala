package com.goho.service.db

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

/**
 * Created by yash.datta on 02/04/16.
 */

abstract class Response[T](results: Array[T])(implicit m: ClassTag[T]) extends Serializable