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
  val percentControl = 10
  val percentEscape = 10

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
    Ascii.NUL, // Ctrl+@
    Ascii.SOH, // Ctrl+A
    Ascii.STX, // Ctrl+B
    Ascii.ETX, // Ctrl+C
    Ascii.EOT, // Ctrl+D
    Ascii.ENQ, // Ctrl+E
    Ascii.ACK, // Ctrl+F
    Ascii.BEL  // Ctrl+G
  )

  // Large mixed input with approximately 10,000 characters
  private def input(targetLength: Int): String = {
    val sb = new StringBuilder()
    val rand = new Random(42) // Fixed seed for reproducibility
    
    while (sb.length < targetLength) {
      val choice = rand.nextInt(100)
      
      if (choice < percentEscape)
        // Print escape sequence
        sb.append(escapeSequences(rand.nextInt(escapeSequences.length)))
      else if (choice < (percentControl + percentEscape))
        // Print control sequence
        sb.append(controlChars(rand.nextInt(controlChars.length)))
      else
        // Printable Ascii character
        sb.append((rand.nextInt(94) + 32).toChar)
    }
    
    sb.toString
  }

  // Varying sized inputs for benchmarking scaling behavior
  val smallInput: String = input(1000)
  val mediumInput: String = input(5000)
  val largeInput: String = input(10000)

  @Benchmark
  def benchmarkSmallInput(): Int = benchmark(smallInput)

  @Benchmark
  def benchmarkMediumInput(): Int = benchmark(mediumInput)

  @Benchmark
  def benchmarkLargeInput(): Int = benchmark(largeInput)
  
  private def benchmark(input: String): Int = {
    val reader = new StringBufferReader(input)
    var count = 0
    
    while (true) {
      reader.readKey() match {
        case Eof => return count
        case _: Key => count += 1
      }
    }

    count
  }
}