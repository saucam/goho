package com.goho

/**
 * Created by yash.datta on 30/03/16.
 */

import com.typesafe.scalalogging.slf4j.LazyLogging
import org.scalatest.{Outcome, Matchers, BeforeAndAfterAll, FunSuite}

/**
 * Base abstract class for all unit tests in Formula
 */
abstract class GoHoFunSuite
  extends FunSuite
  with BeforeAndAfterAll
  with Matchers
  with LazyLogging {

  protected override def afterAll(): Unit = {
    try {
      // common init code
    } finally {
      super.afterAll()
    }
  }

  /**
   * Log the suite name and the test name before and after each test.
   *
   * Subclasses should never override this method. If they wish to run
   * custom code before and after each test, they should mix in the
   * {{org.scalatest.BeforeAndAfter}} trait instead.
   */
  final protected override def withFixture(test: NoArgTest): Outcome = {
    val testName = test.text
    val suiteName = this.getClass.getName
    val shortSuiteName = suiteName.replaceAll("com.goho", "c.g")
    try {
      logger.info(s"\n\n===== TEST OUTPUT FOR $shortSuiteName: '$testName' =====\n")
      test()
    } finally {
      logger.info(s"\n\n===== FINISHED $shortSuiteName: '$testName' =====\n")
    }
  }

}

