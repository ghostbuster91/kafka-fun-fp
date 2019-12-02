package com.softwaremill.test.kafka.monix

import monix.eval.Task
import monix.execution.{CancelableFuture, Scheduler}
import monix.kafka.{KafkaConsumerConfig, KafkaConsumerObservable}
import monix.reactive.Observable
import org.apache.kafka.clients.consumer.ConsumerRecord

import scala.concurrent.Await
import scala.util.Random

object Consumer {
  implicit val scheduler: Scheduler = monix.execution.Scheduler.global
  val consumerCfg = KafkaConsumerConfig.default
    .copy(bootstrapServers = List("127.0.0.1:9092"), groupId = "kafka-tests")

  val random = Random

  import scala.concurrent.duration._
  def main(args: Array[String]): Unit = {
    val value: Observable[ConsumerRecord[String, String]] =
      KafkaConsumerObservable[String, String](consumerCfg, List("foo2"))
    val observable: CancelableFuture[Unit] =
      value
        .groupBy(_.partition())
        .mergeMap { group =>
          group.flatMap { f =>
            println(f)
            Observable.now(())
          }
        }
        .foreach(_ => ())

    Await.result(observable, Duration.Inf)
  }
}
