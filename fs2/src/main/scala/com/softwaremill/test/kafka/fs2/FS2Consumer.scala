package com.softwaremill.test.kafka.fs2

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.functor._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, ProducerRecord, ProducerRecords, consumerStream, _}

object FS2Consumer extends IOApp {

  private val consumerSettings =
    ConsumerSettings[IO, String, String]
      .withAutoOffsetReset(AutoOffsetReset.Earliest)
      .withBootstrapServers("172.17.0.1:9092")
      .withGroupId("group")
  private val partitionedConsumer =
    consumerStream[IO]
      .using(consumerSettings)
      .evalTap(_.subscribeTo("foo2"))
      .flatMap(k => k.partitionedStream)
  private val joinedProcessor = partitionedConsumer.map { s =>
    s.mapAsync(1) { committable =>
      processRecord(committable.record)
    }
  }.parJoinUnbounded

  override def run(args: List[String]): IO[ExitCode] = {
    val stream = joinedProcessor
    stream.compile.drain.as(ExitCode.Success)
  }

  def processRecord(
      record: ConsumerRecord[String, String]
  ): IO[(String, String)] = {
    println(record)
    IO.pure(record.key -> record.value)
  }
}
