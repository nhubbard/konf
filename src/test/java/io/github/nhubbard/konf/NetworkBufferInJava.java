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

public class NetworkBufferInJava {
    public static final ConfigSpec spec = new ConfigSpec("network.buffer");

    public static final RequiredItem<Integer> size =
            new RequiredItem<>(spec, "size", "size of buffer in KB") {
            };

    public static final LazyItem<Integer> maxSize =
            new LazyItem<>(
                    spec, "maxSize", config -> config.get(size) * 2, "max size of buffer in KB") {
            };

    public static final OptionalItem<String> name =
            new OptionalItem<>(spec, "name", "buffer", "name of buffer") {
            };

    public static final OptionalItem<NetworkBuffer.Type> type =
            new OptionalItem<>(
                    spec,
                    "type",
                    NetworkBuffer.Type.OFF_HEAP,
                    """
                            type of network buffer.
                            two type:
                            - on-heap
                            - off-heap
                            buffer is off-heap by default.""") {
            };

    public static final OptionalItem<Integer> offset =
            new OptionalItem<>(spec, "offset", null, "initial offset of buffer", null, true) {
            };
}
