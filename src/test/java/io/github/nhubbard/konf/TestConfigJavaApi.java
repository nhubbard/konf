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

package io.github.nhubbard.konf;

import io.github.nhubbard.konf.helpers.NetworkBuffer;
import io.github.nhubbard.konf.source.DefaultProviders;
import io.github.nhubbard.konf.source.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static kotlin.test.AssertionsKt.assertEquals;

class TestConfigJavaApi {
    private Config config;

    @BeforeEach
    void initConfig() {
        config = Configs.create();
        config.addSpec(NetworkBufferInJava.spec);
    }

    @Test
    @DisplayName("test `Configs.create`")
    void create() {
        final Config config = Configs.create();
        assertEquals(0, config.getItems().size(), null);
    }

    @Test
    @DisplayName("test `Configs.create` with init block")
    void createWithInit() {
        final Config config = Configs.create(it -> it.addSpec(NetworkBufferInJava.spec));
        assertEquals(5, config.getItems().size(), null);
    }

    @Test
    @DisplayName("test fluent API to load from map")
    void loadFromMap() {
        final HashMap<String, Integer> map = new HashMap<>();
        map.put(config.nameOf(NetworkBufferInJava.size), 1024);
        final Config newConfig = config.from().map.kv(map);
        assertEquals(1024, newConfig.get(NetworkBufferInJava.size), null);
    }

    @Test
    @DisplayName("test fluent API to load from system properties")
    void loadFromSystem() {
        System.setProperty(config.nameOf(NetworkBufferInJava.size), "1024");
        final Config newConfig = config.from().systemProperties();
        assertEquals(1024, newConfig.get(NetworkBufferInJava.size), null);
    }

    @Test
    @DisplayName("test fluent API to load from source")
    void loadFromSource() {
        final HashMap<String, Integer> map = new HashMap<>();
        map.put(config.nameOf(NetworkBufferInJava.size), 1024);
        Source.from();
        final Config newConfig = config.withSource(DefaultProviders.map.kv(map));
        assertEquals(1024, newConfig.get(NetworkBufferInJava.size), null);
    }

    @Test
    @DisplayName("test `get(Item<T>)`")
    void getWithItem() {
        final String name = config.get(NetworkBufferInJava.name);
        assertEquals("buffer", name, null);
    }

    @Test
    @DisplayName("test `get(String)`")
    void getWithName() {
        final NetworkBuffer.Type type = config.get(config.nameOf(NetworkBufferInJava.type));
        assertEquals(NetworkBuffer.Type.OFF_HEAP, type, null);
    }

    @Test
    @DisplayName("test `set(Item<T>, T)`")
    void setWithItem() {
        config.set(NetworkBufferInJava.size, 1024);
        assertEquals(1024, config.get(NetworkBufferInJava.size), null);
    }

    @Test
    @DisplayName("test `set(String, T)`")
    void setWithName() {
        config.set(config.nameOf(NetworkBufferInJava.size), 1024);
        assertEquals(1024, config.get(NetworkBufferInJava.size), null);
    }

    @Test
    @DisplayName("test `lazySet(Item<T>, Function1<ItemContainer, T>)`")
    void lazySetWithItem() {
        config.lazySet(NetworkBufferInJava.maxSize, it -> it.get(NetworkBufferInJava.size) * 4);
        config.set(NetworkBufferInJava.size, 1024);
        assertEquals(1024 * 4, config.get(NetworkBufferInJava.maxSize), null);
    }

    @Test
    @DisplayName("test `lazySet(String, Function1<ItemContainer, T>)`")
    void lazySetWithName() {
        config.lazySet(
                config.nameOf(NetworkBufferInJava.maxSize), it -> it.get(NetworkBufferInJava.size) * 4);
        config.set(NetworkBufferInJava.size, 1024);
        assertEquals(1024 * 4, config.get(NetworkBufferInJava.maxSize), null);
    }
}