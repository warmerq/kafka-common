package com.github.warmerq.common.util

/**
   * Create a new shutdown hook. As such, these will be started in
   * no particular order and run concurrently.
   */
object onExit{
  def apply(f: => Unit) {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run() = f
    })
  }
}
