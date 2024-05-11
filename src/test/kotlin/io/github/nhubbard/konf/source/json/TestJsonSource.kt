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

package io.github.nhubbard.konf.source.json

import com.fasterxml.jackson.databind.node.*
import io.github.nhubbard.konf.singleArgumentsOf
import io.github.nhubbard.konf.source.WrongTypeException
import io.github.nhubbard.konf.source.asValue
import io.github.nhubbard.konf.toPath
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.math.BigInteger
import java.util.stream.Stream
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class TestJsonSource {
    @Test
    fun testJsonSource_getOp_onGetUnderlyingJsonNode_itShouldReturnCorrespondingNode() {
        val intSource = JsonSource(IntNode.valueOf(1))
        val node = intSource.node
        assertTrue(node.isInt)
        assertEquals(1, node.intValue())
    }

    @Test
    fun testJsonSource_getOp_onGetKey_itShouldContainTheKey() {
        //language=Json
        val source = JsonProvider.string("""{ "key": 1 }""")
        assertTrue("key".toPath() in source)
    }

    @Test
    fun testJsonSource_getOp_onGetKey_itShouldContainTheCorrespondingValue() {
        //language=Json
        val source = JsonProvider.string("""{ "key": 1 }""")
        assertEquals(1, source["key".toPath()].asValue<Int>())
    }

    @Test
    fun testJsonSource_getOp_onGetInvalidKey_itShouldNotContainTheKey() {
        //language=Json
        val source = JsonProvider.string("""{ "key": 1 }""")
        assertTrue("invalid".toPath() !in source)
    }

    @Test
    fun testJsonSource_getOp_onGetInvalidKey_itShouldNotContainTheCorrespondingValue() {
        //language=Json
        val source = JsonProvider.string("""{ "key": 1 }""")
        assertNull(source.getOrNull("invalid".toPath()))
    }

    @ParameterizedTest
    @MethodSource("wrongTypeSource")
    fun testJsonSource_castOp_onGetInvalidValue_itShouldThrowWrongTypeException(source: () -> Unit) {
        assertFailsWith<WrongTypeException> { source() }
    }

    @Test
    fun testJsonSource_castOp_onGetValidValue_itShouldSucceed() {
        assertEquals(1L, JsonSource(LongNode.valueOf(1L)).asValue<Long>())
        assertEquals(1L, JsonSource(IntNode.valueOf(1)).asValue<Long>())
        assertEquals(1.toShort(), JsonSource(ShortNode.valueOf(1)).asValue<Short>())
        assertEquals(1.toShort(), JsonSource(IntNode.valueOf(1)).asValue<Short>())
        assertEquals(1.0F, JsonSource(FloatNode.valueOf(1.0F)).asValue<Float>())
        assertEquals(1.0F, JsonSource(DoubleNode.valueOf(1.0)).asValue<Float>())
        assertEquals(BigInteger.ONE, JsonSource(BigIntegerNode.valueOf(BigInteger.valueOf(1L))).asValue<BigInteger>())
        assertEquals(BigInteger.ONE, JsonSource(LongNode.valueOf(1L)).asValue<BigInteger>())
        assertEquals(BigDecimal.valueOf(1.0), JsonSource(DecimalNode.valueOf(BigDecimal.valueOf(1.0))).asValue<BigDecimal>())
        assertEquals(BigDecimal.valueOf(1.0), JsonSource(DoubleNode.valueOf(1.0)).asValue<BigDecimal>())
    }

    companion object {
        @JvmStatic
        fun wrongTypeSource(): Stream<Arguments> = singleArgumentsOf(
            { JsonSource(IntNode.valueOf(1)).asValue<String>() },
            { JsonSource(IntNode.valueOf(1)).asValue<Boolean>() },
            { JsonSource(BooleanNode.valueOf(true)).asValue<Double>() },
            { JsonSource(DoubleNode.valueOf(1.0)).asValue<Int>() }
        )
    }
}