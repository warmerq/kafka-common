package com.umeng.ads.common.framework

import org.specs2._
import kafka.message.MessageAndMetadata

class StreamProcessorTest extends mutable.Specification {

  
  "StreamProcessor" should {
    "contain the correct number of functions in processInStream" in {
      val p = StreamProcessor[String]()
      p processInStream { s: MessageAndMetadata[String] =>
        s.message
      } processInStream { s: MessageAndMetadata[String] =>
        s
      }
      p.inStreams.size must_== 2
    }

    "contain the correct number of functions in processAfterStream" in {
      val p = StreamProcessor[String]()
      p processInStream { s: MessageAndMetadata[String] =>
        s
      } processAfterStream {

      }
      p.afterStreams.size must_== 1
    }

    "could do function in processInStream" in {
      var count = 0;
      val p = StreamProcessor[String]()
      val message_and_meta_data = new MessageAndMetadata[String]("abc")
      p processInStream { s: MessageAndMetadata[String] => count += s.message.size }

      p runProcessInStream message_and_meta_data

      count must_== "abc".size
    }

    "could do function in processAfterStream" in {
      var count = 0;
      val p = StreamProcessor[String]()

      p processAfterStream { count += 5 }

      p runProcessAfterStream

      count must_== 5
    }

  }

}
