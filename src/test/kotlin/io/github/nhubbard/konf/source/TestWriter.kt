package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.ConfigSpec
import io.github.nhubbard.konf.source.properties.toProperties
import io.github.nhubbard.konf.tempFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.io.ByteArrayOutputStream
import java.io.StringWriter
import java.nio.charset.Charset

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TestWriter {
    companion object {
        @JvmStatic
        fun provider(): Writer {
            val spec = object : ConfigSpec() {
                @Suppress("unused")
                val key by optional("value")
            }
            val config = Config { addSpec(spec) }
            return config.toProperties
        }

        @JvmStatic
        val expectedString = "key=value" + System.lineSeparator()
    }

    @Test
    fun testWriter_onSaveToString_itShouldReturnAStringWhichContainsContentFromConfig() {
        val subject = provider()
        val string = subject.toText()
        assertEquals(string, expectedString)
    }

    @Test
    fun testWriter_onSaveToByteArray_itShouldReturnAByteArrayWhichContainsContentFromConfig() {
        val subject = provider()
        val byteArray = subject.toBytes()
        assertEquals(byteArray.toString(Charset.defaultCharset()), expectedString)
    }

    @Test
    fun testWriter_onSaveToWriter_itShouldReturnAWriterWhichContainsContentFromConfig() {
        val subject = provider()
        val writer = StringWriter()
        subject.toWriter(writer)
        assertEquals(writer.toString(), expectedString)
    }

    @Test
    fun testWriter_onSaveToOutputStream_itShouldReturnAnOutputStreamWhichContainsContentFromConfig() {
        val subject = provider()
        val outputStream = ByteArrayOutputStream()
        subject.toOutputStream(outputStream)
        assertEquals(outputStream.toString(), expectedString)
    }

    @Test
    fun testWriter_onSaveToFile_itShouldReturnAFileWhichContainsContentFromConfig_andShouldNotLockTheFile() {
        val subject = provider()
        val file = tempFile()
        subject.toFile(file)
        assertEquals(file.readText(), expectedString)
        assertTrue(file.delete())
    }

    @Test
    fun testWriter_onSaveToFileByPath_itShouldReturnAFileWhichContainsContentFromConfig() {
        val subject = provider()
        val file = tempFile()
        val path = file.toString()
        subject.toFile(path)
        assertEquals(file.readText(), expectedString)
    }
}