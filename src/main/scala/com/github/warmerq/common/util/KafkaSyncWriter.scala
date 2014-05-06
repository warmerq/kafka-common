package com.github.warmerq.common.util

import com.typesafe.config.{Config, ConfigFactory}
import kafka.producer._
import java.util.concurrent.Executors
import java.util.Properties
import com.twitter.util.FuturePool
import com.twitter.concurrent.NamedPoolThreadFactory
import scala.collection.JavaConverters._
import com.twitter.server.TwitterServer
import com.twitter.finagle.stats.{ StatsReceiver, NullStatsReceiver }
import com.twitter.logging.Logger

class KafkaSyncWriter[Key_Type, Message_Type](
  kafka_conf: Config,
  statsReceiver: StatsReceiver = NullStatsReceiver,
  log: Logger = Logger()
) {

  val props = new Properties();

  //put the `kafka_conf` into a java property, complex type conversion.
  kafka_conf.entrySet.asScala.foreach(
    entry => props.put(
      entry.getKey,
      kafka_conf.getString(entry.getKey)
    )
  )

  val producerConfig = new ProducerConfig(props);
  val producer = new Producer[Key_Type, Message_Type](producerConfig);

  def write( topic:String, key:Key_Type, message: Message_Type) = {
    val log_counter = statsReceiver.counter("kafka/log_to_" + topic)
    val data = new ProducerData[Key_Type, Message_Type](topic, key, List(message));
    log.debug("ready to write a message to kafka")
    producer.send(data)
    log_counter.incr()
    log.debug("DONE written a message to kafka")
  }

}
