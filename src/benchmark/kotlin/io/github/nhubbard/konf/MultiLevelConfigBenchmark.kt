/*
 * Copyright (c) 2017-2024 Uchuhimo
 * Copyright (c) 2024-present Nicholas Hubbard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for specific language governing permissions and
 * limitations under the License.
 */

package io.github.nhubbard.konf

import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 10, batchSize = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Timeout(time = 1, timeUnit = TimeUnit.SECONDS)
@Threads(8)
@Fork(1)
class MultiLevelConfigBenchmark {
    @State(Scope.Thread)
    class ConfigState {
        val config = Config { addSpec(Buffer) }.withLayer().withLayer().withLayer().withLayer()
        val path = Buffer.qualify(Buffer.name)
    }

    @State(Scope.Benchmark)
    class MultiThreadConfigState {
        val config = Config { addSpec(Buffer) }.withLayer().withLayer().withLayer().withLayer()
        val path = Buffer.qualify(Buffer.name)
    }

    @Benchmark
    fun getWithItem(state: ConfigState) = state.config[Buffer.name]

    @Benchmark
    fun getWithItemFromMultiThread(state: MultiThreadConfigState) = state.config[Buffer.name]

    @Benchmark
    fun setWithItem(state: ConfigState) {
        state.config[Buffer.name] = "newName"
    }

    @Benchmark
    fun setWithItemFromMultiThread(state: MultiThreadConfigState) {
        state.config[Buffer.name] = "newName"
    }

    @Benchmark
    fun getWithPath(state: ConfigState) = state.config<String>(state.path)

    @Benchmark
    fun getWithPathFromMultiThread(state: MultiThreadConfigState) = state.config<String>(state.path)

    @Benchmark
    fun setWithPath(state: ConfigState) {
        state.config[state.path] = "newName"
    }

    @Benchmark
    fun setWithPathFromMultiThread(state: MultiThreadConfigState) {
        state.config[state.path] = "newName"
    }
}