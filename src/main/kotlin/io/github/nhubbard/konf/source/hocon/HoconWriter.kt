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

package io.github.nhubbard.konf.source.hocon

import io.github.nhubbard.konf.*
import com.typesafe.config.ConfigRenderOptions
import com.typesafe.config.ConfigValueFactory
import io.github.nhubbard.konf.source.Writer
import io.github.nhubbard.konf.source.base.toHierarchicalMap
import com.typesafe.config.ConfigValue
import java.io.OutputStream

/**
 * Writer for HOCON source.
 */
class HoconWriter(val config: Config) : Writer {
    private val renderOpts = ConfigRenderOptions.defaults()
        .setOriginComments(false)
        .setComments(false)
        .setJson(false)

    override fun toWriter(writer: java.io.Writer) {
        writer.write(toText())
    }

    override fun toOutputStream(outputStream: OutputStream) {
        outputStream.writer().use {
            toWriter(it)
        }
    }

    private fun TreeNode.toConfigValue(): ConfigValue {
        val value = when (this) {
            is ValueNode -> ConfigValueFactory.fromAnyRef(value)
            is ListNode -> ConfigValueFactory.fromIterable(list.map { it.toConfigValue() })
            else -> ConfigValueFactory.fromMap(children.mapValues { (_, value) -> value.toConfigValue() })
        }
        return comments.takeIf { it.isNotEmpty() }?.let {
            value.withOrigin(value.origin().withComments(it.split("\n")))
        } ?: value
    }

    override fun toText(): String {
        val output = if (config.isEnabled(Feature.WRITE_DESCRIPTIONS_AS_COMMENTS)) {
            config.toTree().toConfigValue().render(renderOpts.setComments(true))
        } else {
            ConfigValueFactory.fromMap(config.toHierarchicalMap()).render(renderOpts)
        }
        return output.replace("\n", System.lineSeparator())
    }
}

/**
 * Returns writer for HOCON source.
 */
val Config.toHocon: Writer get() = HoconWriter(this)
