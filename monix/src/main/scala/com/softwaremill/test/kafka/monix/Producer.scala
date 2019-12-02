package com.softwaremill.test.kafka.monix

import java.util.UUID

import monix.execution.Scheduler
import monix.kafka._
import monix.reactive.Observable
import org.apache.kafka.clients.producer.ProducerRecord

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Producer {
  implicit val scheduler: Scheduler = monix.execution.Scheduler.global
  val producerCfg =
    KafkaProducerConfig.default.copy(bootstrapServers = List("127.0.0.1:9092"))

  val producerSink = KafkaProducerSink[String, String](producerCfg, scheduler)
  val producer = KafkaProducer[String, String](producerCfg, scheduler)

  import scala.concurrent.duration._
  // Lets pretend we have this observable of records
  val observable: Observable[ProducerRecord[String, String]] =
    Observable
      .intervalAtFixedRate(1 seconds)
      .map(
        _ =>
          new ProducerRecord[String, String](
            "foo2",
            s"key${UUID.randomUUID().toString}",
            s"recordVal${UUID.randomUUID().toString}"
          )
      )

//ACHTUNG, generuje nieskoczenie wiele wiadomosci
  def main(args: Array[String]): Unit = {
    val task = observable
      .bufferIntrospective(1024)
      .consumeWith(producerSink)
      .runToFuture

    Await.result(task, Duration.Inf)

  }
}
