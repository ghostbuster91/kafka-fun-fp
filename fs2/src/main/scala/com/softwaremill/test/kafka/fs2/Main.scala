package com.softwaremill.test.kafka.fs2

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka._
import scala.concurrent.duration._
import fs2.Stream

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    testing(Person(1))
    old
  }

  private def old = {
    def processRecord(
        record: ConsumerRecord[String, String]
    ): IO[(String, String)] = {
      println(record.partition)
      IO.pure(record.key -> record.value)
    }

    val consumerSettings =
      ConsumerSettings[IO, String, String]
        .withAutoOffsetReset(AutoOffsetReset.Earliest)
        .withBootstrapServers("172.17.0.1:9092")
        .withGroupId("group")

    val producerSettings =
      ProducerSettings[IO, String, String]
        .withBootstrapServers("172.17.0.1:9092")

    val partitionedConsumer =
      consumerStream[IO]
        .using(consumerSettings)
        .evalTap(_.subscribeTo("foo"))
        .flatMap(k => k.partitionedStream)
    val joinedProcessor = partitionedConsumer.map { s =>
      s.mapAsync(1) { committable =>
        processRecord(committable.record)
          .map {
            case (key, value) =>
              println(s"$key $value")
              val record = ProducerRecord("topic", key, value)
              ProducerRecords.one(record, committable.offset)
          }
      }
    }.parJoinUnbounded
    val stream =
      joinedProcessor
        .through(produce(producerSettings))
        .map(_.passthrough)
        .through(commitBatchWithin(500, 15.seconds))

    stream.compile.drain.as(ExitCode.Success)
  }

  def testing(e: Entity) = {
    e match {
      case Org(name) => println(name)
    }
  }
}

sealed trait Entity
case class Person(age: Int) extends Entity
case class Org(name: String) extends Entity
