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

package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.Feature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URI
import java.net.URL
import java.nio.file.FileSystems
import java.nio.file.StandardWatchEventKinds
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * Loader to load source from various input formats.
 *
 * @param config parent config
 */
class Loader(
    /**
     * Parent config for all child configs loading the source in this loader.
     */
    val config: Config,
    /**
     * Source provider to provide the source from various input format.
     */
    val provider: Provider
) {
    val optional = config.isEnabled(Feature.OPTIONAL_SOURCE_BY_DEFAULT)

    /**
     * Returns a child config containing values from specified reader.
     *
     * @param reader specified reader for reading character streams
     * @return a child config containing values from specified reader
     */
    fun reader(reader: Reader): Config =
        config.withSource(provider.reader(reader))

    /**
     * Returns a child config containing values from specified input stream.
     *
     * @param inputStream specified input stream of bytes
     * @return a child config containing values from specified input stream
     */
    fun inputStream(inputStream: InputStream): Config =
        config.withSource(provider.inputStream(inputStream))

    /**
     * Returns a child config containing values from specified file.
     *
     * @param file specified file
     * @param optional whether the source is optional
     * @return a child config containing values from specified file
     */
    fun file(file: File, optional: Boolean = this.optional): Config =
        config.withSource(provider.file(file, optional))

    /**
     * Returns a child config containing values from the specified file path.
     *
     * @param file specified file path
     * @param optional whether the source is optional
     * @return a child config containing values from the specified file path
     */
    fun file(file: String, optional: Boolean = this.optional): Config =
        config.withSource(provider.file(file, optional))

    private val File.digest: ByteArray
        get() {
            val messageDigest = MessageDigest.getInstance("MD5")
            DigestInputStream(inputStream().buffered(), messageDigest).use { it.readBytes() }
            return messageDigest.digest()
        }

    /**
     * Returns a child config containing values from specified file,
     * and reloads values when file content has been changed.
     *
     * @param file specified file
     * @param delayTime delay to observe between every check. The default value is 5.
     * @param unit time unit of delay. The default value is [TimeUnit.SECONDS].
     * @param context context of the coroutine. The default value is [Dispatchers.Default].
     * @param optional whether the source is optional
     * @param onLoad function invoked after the updated file is loaded
     * @return a child config containing values from watched file
     */
    fun watchFile(
        file: File,
        delayTime: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        context: CoroutineContext = Dispatchers.Default,
        optional: Boolean = this.optional,
        onLoad: ((config: Config, source: Source) -> Unit)? = null
    ): Config {
        val absoluteFile = file.absoluteFile
        return provider.file(absoluteFile, optional).let { source ->
            config.withLoadTrigger("watch ${source.description}") { newConfig, load ->
                newConfig.lock {
                    load(source)
                }
                onLoad?.invoke(newConfig, source)
                val path = absoluteFile.toPath().parent
                val isMac = "mac" in System.getProperty("os.name").lowercase()
                val watcher = FileSystems.getDefault().newWatchService()
                path.register(
                    watcher,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_CREATE
                )
                var digest = absoluteFile.digest
                MainScope().launch(context) {
                    while (true) {
                        delay(unit.toMillis(delayTime))
                        if (isMac) {
                            val newDigest = absoluteFile.digest
                            if (!newDigest.contentEquals(digest)) {
                                digest = newDigest
                                val newSource = provider.file(file, optional)
                                newConfig.lock {
                                    newConfig.clear()
                                    load(newSource)
                                }
                                onLoad?.invoke(newConfig, newSource)
                            }
                        } else {
                            val key = watcher.poll()
                            if (key != null) {
                                for (event in key.pollEvents()) {
                                    val kind = event.kind()
                                    val filename = event.context()
                                    if (filename.toString() == absoluteFile.name) {
                                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                                            continue
                                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY ||
                                            kind == StandardWatchEventKinds.ENTRY_CREATE
                                        ) {
                                            val newSource = provider.file(file, optional)
                                            newConfig.lock {
                                                newConfig.clear()
                                                load(newSource)
                                            }
                                            onLoad?.invoke(newConfig, newSource)
                                        }
                                    }
                                    val valid = key.reset()
                                    if (!valid) {
                                        watcher.close()
                                        throw InvalidWatchKeyException(source)
                                    }
                                }
                            }
                        }
                    }
                }
            }.withLayer()
        }
    }

    /**
     * Returns a child config containing values from the specified file path,
     * and reloads values when file content has been changed.
     *
     * @param file specified file path
     * @param delayTime delay to observe between every check. The default value is 5.
     * @param unit time unit of delay. The default value is [TimeUnit.SECONDS].
     * @param context context of the coroutine. The default value is [Dispatchers.Default].
     * @param optional whether the source is optional
     * @param onLoad function invoked after the updated file is loaded
     * @return a child config containing values from watched file
     */
    fun watchFile(
        file: String,
        delayTime: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        context: CoroutineContext = Dispatchers.Default,
        optional: Boolean = this.optional,
        onLoad: ((config: Config, source: Source) -> Unit)? = null
    ): Config =
        watchFile(File(file), delayTime, unit, context, optional, onLoad)

    /**
     * Returns a child config containing values from specified string.
     *
     * @param content specified string
     * @return a child config containing values from specified string
     */
    fun string(content: String): Config =
        config.withSource(provider.string(content))

    /**
     * Returns a child config containing values from the specified byte array.
     *
     * @param content specified byte array
     * @return a child config containing values from specified byte array
     */
    fun bytes(content: ByteArray): Config =
        config.withSource(provider.bytes(content))

    /**
     * Returns a child config containing values from the specified portion of byte array.
     *
     * @param content specified byte array
     * @param offset the start offset of the portion of the array to read
     * @param length the length of the portion of the array to read
     * @return a child config containing values from the specified portion of byte array
     */
    fun bytes(content: ByteArray, offset: Int, length: Int): Config =
        config.withSource(provider.bytes(content, offset, length))

    /**
     * Returns a child config containing values from specified url.
     *
     * @param url specified url
     * @param optional whether the source is optional
     * @return a child config containing values from specified url
     */
    fun url(url: URL, optional: Boolean = this.optional): Config =
        config.withSource(provider.url(url, optional))

    /**
     * Returns a child config containing values from specified url string.
     *
     * @param url specified url string
     * @param optional whether the source is optional
     * @return a child config containing values from specified url string
     */
    fun url(url: String, optional: Boolean = this.optional): Config =
        config.withSource(provider.url(url, optional))

    /**
     * Returns a child config containing values from specified url,
     * and reloads values periodically.
     *
     * @param url specified url
     * @param period reload period. The default value is 5.
     * @param unit time unit of the reload period. The default value is [TimeUnit.SECONDS].
     * @param context context of the coroutine. The default value is [Dispatchers.Default].
     * @param optional whether the source is optional
     * @param onLoad function invoked after the updated URL is loaded
     * @return a child config containing values from specified url
     */
    fun watchUrl(
        url: URL,
        period: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        context: CoroutineContext = Dispatchers.Default,
        optional: Boolean = this.optional,
        onLoad: ((config: Config, source: Source) -> Unit)? = null
    ): Config {
        return provider.url(url, optional).let { source ->
            config.withLoadTrigger("watch ${source.description}") { newConfig, load ->
                newConfig.lock {
                    load(source)
                }
                onLoad?.invoke(newConfig, source)
                MainScope().launch(context) {
                    while (true) {
                        delay(unit.toMillis(period))
                        val newSource = provider.url(url, optional)
                        newConfig.lock {
                            newConfig.clear()
                            load(newSource)
                        }
                        onLoad?.invoke(newConfig, newSource)
                    }
                }
            }.withLayer()
        }
    }

    /**
     * Returns a child config containing values from specified url string,
     * and reloads values periodically.
     *
     * @param url specified url string
     * @param period reload period. The default value is 5.
     * @param unit time unit of the reload period. The default value is [TimeUnit.SECONDS].
     * @param context context of the coroutine. The default value is [Dispatchers.Default].
     * @param optional whether the source is optional
     * @param onLoad function invoked after the updated URL is loaded
     * @return a child config containing values from specified url string
     */
    fun watchUrl(
        url: String,
        period: Long = 5,
        unit: TimeUnit = TimeUnit.SECONDS,
        context: CoroutineContext = Dispatchers.Default,
        optional: Boolean = this.optional,
        onLoad: ((config: Config, source: Source) -> Unit)? = null
    ): Config =
        watchUrl(URI(url).toURL(), period, unit, context, optional, onLoad)

    /**
     * Returns a child config containing values from specified resource.
     *
     * @param resource path of specified resource
     * @param optional whether the source is optional
     * @return a child config containing values from specified resource
     */
    fun resource(resource: String, optional: Boolean = this.optional): Config =
        config.withSource(provider.resource(resource, optional))
}
