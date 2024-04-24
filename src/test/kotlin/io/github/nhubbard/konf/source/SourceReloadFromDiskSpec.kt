package io.github.nhubbard.konf.source

import io.github.nhubbard.konf.Config
import io.github.nhubbard.konf.source.helpers.ConfigForLoad
import io.github.nhubbard.konf.source.helpers.kvLoadContent
import io.github.nhubbard.konf.tempFile
import org.jetbrains.spek.subject.SubjectSpek
import org.jetbrains.spek.subject.itBehavesLike
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object SourceReloadFromDiskSpec : SubjectSpek<Config>({

    subject {
        val config = Config {
            addSpec(ConfigForLoad)
        }.from.map.kv(kvLoadContent)
        val map = config.toMap()
        val newMap = tempFile().run {
            ObjectOutputStream(outputStream()).use {
                it.writeObject(map)
            }
            ObjectInputStream(inputStream()).use {
                @Suppress("UNCHECKED_CAST")
                it.readObject() as Map<String, Any>
            }
        }
        Config {
            addSpec(ConfigForLoad)
        }.from.map.kv(newMap)
    }

    itBehavesLike(SourceLoadBaseSpec)
})