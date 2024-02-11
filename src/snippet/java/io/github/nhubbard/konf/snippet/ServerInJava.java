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

package io.github.nhubbard.konf.snippet;

import io.github.nhubbard.konf.Config;

@SuppressWarnings("unused")
public class ServerInJava {
    private final String host;
    private final Integer tcpPort;

    public ServerInJava(String host, Integer tcpPort) {
        this.host = host;
        this.tcpPort = tcpPort;
    }

    public ServerInJava(Config config) {
        this(config.get(ServerSpecInJava.host), config.get(ServerSpecInJava.tcpPort));
    }

    public String getHost() {
        return host;
    }

    public Integer getTcpPort() {
        return tcpPort;
    }
}
