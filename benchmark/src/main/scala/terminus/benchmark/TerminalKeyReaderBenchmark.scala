/*
 * Copyright 2024 Creative Scala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package terminus.benchmark

import org.openjdk.jmh.annotations._
import terminus._
import terminus.effect._
import java.util.concurrent.TimeUnit
import scala.util.Random
import scala.concurrent.duration._
import cats.syntax.show._

/**
 * Benchmark for TerminalKeyReader that measures the performance of processing
 * large amounts of input data containing both regular characters and escape sequences.
 *
 * To run:
 * sbt "benchmark/Jmh/run -i 10 -wi 5 -f 2 -t 1 terminus.benchmark.TerminalKeyReaderBenchmark"
 *
 * Parameters:
 * -i: Number of iterations
 * -wi: Number of warmup iterations
 * -f: Number of forks
 * -t: Number of threads
 */
@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
class TerminalKeyReaderBenchmark {

  // Common escape sequences
  val escapeSequences = Array(
    s"${Ascii.ESC}[A",     // Up arrow
    s"${Ascii.ESC}[B",     // Down arrow
    s"${Ascii.ESC}[C",     // Right arrow
    s"${Ascii.ESC}[D",     // Left arrow
    s"${Ascii.ESC}OP",     // F1
    s"${Ascii.ESC}OQ",     // F2
    s"${Ascii.ESC}OR",     // F3
    s"${Ascii.ESC}OS",     // F4
    s"${Ascii.ESC}[15~",   // F5
    s"${Ascii.ESC}[17~",   // F6
    s"${Ascii.ESC}[1;2A",  // Shift+Up
    s"${Ascii.ESC}[1;5A",  // Ctrl+Up
    s"${Ascii.ESC}[1;6A",  // Ctrl+Shift+Up
    s"${Ascii.ESC}[3~",    // Delete
    s"${Ascii.ESC}[3;5~",  // Ctrl+Delete
    s"${Ascii.ESC}[3;6~"   // Ctrl+Shift+Delete
  )

  // Control characters
  val controlChars = Array(
    Ascii.NUL.toChar, // Ctrl+@
    Ascii.SOH.toChar, // Ctrl+A
    Ascii.STX.toChar, // Ctrl+B
    Ascii.ETX.toChar, // Ctrl+C
    Ascii.EOT.toChar, // Ctrl+D
    Ascii.ENQ.toChar, // Ctrl+E
    Ascii.ACK.toChar, // Ctrl+F
    Ascii.BEL.toChar  // Ctrl+G
  )

  // Large mixed input with approximately 10,000 characters
  val largeInput: String = {
    val sb = new StringBuilder()
    val rand = new Random(42) // Fixed seed for reproducibility
    
    // Generate approximately 10,000 characters
    val targetLength = 10000
    while (sb.length < targetLength) {
      val choice = rand.nextInt(100)
      
      if (choice < 80) {
        // 80% chance of regular ASCII char
        sb.append((rand.nextInt(94) + 32).toChar) // Printable ASCII
      } else if (choice < 90) {
        // 10% chance of control char
        sb.append(controlChars(rand.nextInt(controlChars.length)))
      } else {
        // 10% chance of escape sequence
        sb.append(escapeSequences(rand.nextInt(escapeSequences.length)))
      }
    }
    
    sb.toString
  }

  // Varying sized inputs for benchmarking scaling behavior
  val smallInput: String = largeInput.substring(0, 1000)
  val mediumInput: String = largeInput.substring(0, 5000)

  @Benchmark
  def benchmarkSmallInput(): Int = {
    val reader = new StringBufferReader(smallInput)
    var count = 0
    
    while (true) {
      reader.readKey() match {
        case Eof => return count
        case key: Key => 
          count += 1
      }
    }
    
    count
  }

  @Benchmark
  def benchmarkMediumInput(): Int = {
    val reader = new StringBufferReader(mediumInput)
    var count = 0
    
    while (true) {
      reader.readKey() match {
        case Eof => return count
        case key: Key => 
          count += 1
      }
    }
    
    count
  }

  @Benchmark
  def benchmarkLargeInput(): Int = {
    val reader = new StringBufferReader(largeInput)
    var count = 0
    
    while (true) {
      reader.readKey() match {
        case Eof => return count
        case key: Key => 
          count += 1
      }
    }
    
    count
  }
  
  // Benchmark that also prints each key (tests Show[Key] performance)
  @Benchmark
  def benchmarkLargeInputWithShow(): Int = {
    val reader = new StringBufferReader(largeInput)
    var count = 0
    val sb = new StringBuilder()
    
    while (true) {
      reader.readKey() match {
        case Eof => return count
        case key: Key => 
          count += 1
          sb.append(key.show)
      }
    }
    
    count
  }
}