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
import com.davils.konnect.core.port.PortConverters
import com.davils.konnect.core.port.PortRange
import com.davils.konnect.internetprotocol.AddressConverters
import com.davils.konnect.internetprotocol.address.IpAddress
import com.davils.konnect.internetprotocol.network.IpNetwork
import com.davils.kontext.core.Resolver

/**
 * Reads the property [key] as a [Port] of Konnect, such as `server.port = 8443`.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the port.
 * @throws PropertyException if no source knows the key, a source fails or the text is no port; the message never
 *   shows the value.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.port(key: String): Port = property(key, PortConverters.port)

/**
 * Reads the property [key] as a [Port], or returns `null` if no source knows it.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the port or `null`.
 * @throws PropertyException if a source fails or the text is no port.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.portOrNull(key: String): Port? = propertyOrNull(key, PortConverters.port)

/**
 * Reads the property [key] as a [PortRange] of Konnect, such as `ports = 8000-8080`.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the range.
 * @throws PropertyException if no source knows the key, a source fails or the text is no port range.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.portRange(key: String): PortRange = property(key, PortConverters.portRange)

/**
 * Reads the property [key] as an IPv4 or IPv6 [IpAddress] of Konnect, such as `server.bind = 192.0.2.1`.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the address.
 * @throws PropertyException if no source knows the key, a source fails or the text is no address.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.ipAddress(key: String): IpAddress = property(key, AddressConverters.ipAddress)

/**
 * Reads the property [key] as an [IpAddress], or returns `null` if no source knows it.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the address or `null`.
 * @throws PropertyException if a source fails or the text is no address.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.ipAddressOrNull(key: String): IpAddress? = propertyOrNull(key, AddressConverters.ipAddress)

/**
 * Reads the property [key] as an [IpNetwork] of Konnect, such as `trusted.proxies = 10.0.0.0/8`; a network with host
 * bits set is refused.
 *
 * @receiver the resolver of a definition or of the container.
 * @param key the key, a dotted name in lower case.
 * @return the network.
 * @throws PropertyException if no source knows the key, a source fails or the text is no network.
 * @throws IllegalArgumentException if [key] is not a valid key.
 * @since 1.2.0
 */
public fun Resolver.ipNetwork(key: String): IpNetwork = property(key, AddressConverters.ipNetwork)
