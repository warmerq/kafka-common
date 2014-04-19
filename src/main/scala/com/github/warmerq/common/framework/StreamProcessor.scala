package com.github.warmerq.common.framework

import scala.collection.mutable
import kafka.message.MessageAndMetadata

class StreamProcessor[T] {

  val inStreams = mutable.Buffer[() => MessageAndMetadata[T] => Unit]()
  val afterStreams = mutable.Buffer[() => Unit]()

  def processInStream(f: MessageAndMetadata[T] => Unit) = {
    inStreams += (() => f)
    this
  }

  def runProcessInStream(messageAndMetadata: MessageAndMetadata[T]) = {
    for (f <- inStreams) f()(messageAndMetadata)
  }

  def processAfterStream(f: => Unit) = {
    afterStreams += (() => f)
    this
  }

  def runProcessAfterStream() = {
    for (f <- afterStreams) f()
  }


}

object StreamProcessor {
  def apply[T]() = new StreamProcessor[T]()
}
