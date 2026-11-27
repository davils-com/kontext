/*
 * Copyright 2026 Davils
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

package com.davils.kontext.core.property

import com.davils.konnect.core.port.Port
import com.davils.konnect.core.port.PortRange
import com.davils.konnect.internetprotocol.address.IpAddress
import com.davils.konnect.internetprotocol.network.IpNetwork
import com.davils.kontext.core.fixture.containerOf
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotContain

private const val HTTPS: Int = 443
private const val SECRET: String = "not-a-port-s3cr3t"
private const val SERVER_PORT: String = "server.port"
private const val MISSING: String = "server.missing"
private const val PORTS: String = "8000-8080"
private const val ADDRESS: String = "2001:db8::1"
private const val NETWORK: String = "10.0.0.0/8"

class NetworkPropertiesTest : FunSpec({
    test("reads ports, ranges, addresses and networks typed") {
        val container = containerOf {
            properties {
                values(
                    SERVER_PORT to "443",
                    "server.ports" to PORTS,
                    "server.bind" to ADDRESS,
                    "trusted.proxies" to NETWORK,
                )
            }
        }

        container.port(SERVER_PORT) shouldBe Port(HTTPS)
        container.portRange("server.ports") shouldBe PortRange.parse(PORTS)
        container.ipAddress("server.bind") shouldBe IpAddress.parse(ADDRESS)
        container.ipNetwork("trusted.proxies") shouldBe IpNetwork.parse(NETWORK)
        container.portOrNull(MISSING).shouldBeNull()
        container.ipAddressOrNull(MISSING).shouldBeNull()
    }

    test("refuses a value that is no port without showing it") {
        val container = containerOf { properties { values(SERVER_PORT to SECRET) } }

        val failure = shouldThrow<PropertyException> { container.port(SERVER_PORT) }

        failure.message.orEmpty() shouldNotContain SECRET
    }
})
