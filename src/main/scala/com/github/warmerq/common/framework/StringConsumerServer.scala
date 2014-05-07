package com.github.warmerq.common.framework

import java.util.Properties
import java.util.concurrent.Executors
import scala.collection.JavaConverters._
import scala.collection.mutable

import com.twitter.concurrent.NamedPoolThreadFactory
import com.twitter.finagle.http.HttpMuxer
import com.twitter.server.TwitterServer
import com.twitter.server.handler._
import com.twitter.util.Await
import com.twitter.util.FuturePool
import com.typesafe.config.ConfigFactory
import kafka.consumer._
import kafka.serializer.StringDecoder
import org.jboss.netty.handler.codec.http._

import com.github.warmerq.common.config.Settings


trait StringConsumerServer extends TwitterServer with Settings {

  val consumer_conf = conf.getConfig("kafka.consumer")
  val topics = conf.getStringList("processing.topics")
  val num_threads = conf.getInt("processing.num_threads")

  val consumerThreadPool = FuturePool(
    Executors.newFixedThreadPool(
      num_threads * topics.size,
      new NamedPoolThreadFactory("StringConsumerServer")
    )
  )

  val props = new Properties()

  //put the `kafka_conf` into a java property, complex type conversion.
  consumer_conf.entrySet.asScala.foreach(
    entry => props.put(
      entry.getKey,
      consumer_conf.getString(entry.getKey)
    )
  )


  val consumerConfig = new ConsumerConfig(props)
  val consumerConnector = Consumer.create(consumerConfig)
  val topicMessageStreams = consumerConnector.createMessageStreams[String](
    topics.asScala.map(_ -> num_threads ).toMap,
    new StringDecoder
  )


  class ConsumerShutdownHandler extends TwitterHandler{
    def apply(req: HttpRequest) = {
      consumerConnector.shutdown
      respond("Consumer Shutdown OK")
    }
  }

  private val processors = mutable.Buffer[String => StreamProcessor[String]]()

  protected def doProcess(f: String => StreamProcessor[String]) { processors += f }

  def main() {

    for ((topic, stream_list) <- topicMessageStreams){
      val message_counter = statsReceiver.counter("kafka/consumer/" + topic + "_handled")

      for (stream <- stream_list){
        consumerThreadPool { // run in a seperate thread

          //before each stream
          // run the StreamProcessor generator
          val streamProcessor = processors(0)(topic)

          for (message_and_meta_data <- stream){ // infinite loop
            try{
              // Do something with the message_and_meta_data.message
              streamProcessor.runProcessInStream( message_and_meta_data )

              message_counter.incr
            }catch{
              case e: Exception => {
                log.error("Unexpected Exception" + e.printStackTrace)
              }
            }
          }

          //stream drained
          streamProcessor.runProcessAfterStream

        } // consumerThreadPool ending
      }
    }

    log.info("Server started with configuration:\n"
      + "topics: " + topics + "\n"
      + "number of threads per topic: " + num_threads + "\n"
    )

    onExit {
      consumerConnector.shutdown
      log.info("Server Exit...")
    }

    HttpMuxer.addHandler("/admin/consumer_shutdown", new ConsumerShutdownHandler)
    Await.ready(httpServer)

  }
}
