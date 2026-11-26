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

@file:OptIn(DavilsInternal::class)

package com.davils.kontext.kaml

import com.davils.kaml.core.Yaml
import com.davils.kaml.core.YamlParseResult
import com.davils.kaml.core.model.YamlTag
import com.davils.kaml.core.node.YamlNode
import com.davils.kontext.core.property.PropertyKey
import com.davils.kontext.core.property.PropertySource
import com.davils.kore.core.annotation.DavilsInternal
import com.davils.kore.core.collection.OrderedMap
import com.davils.kore.core.text.SafeText

private const val SEPARATOR: Char = '.'

/**
 * Properties read from one YAML document, such as `db:\n  port: 5432` for the key `db.port`.
 *
 * A mapping nests its keys with dots, a scalar is the value of its key exactly as the document writes it, and a null
 * is no property at all. Every key, after nesting, must be a [PropertyKey], a dotted name in lower case; a sequence, a
 * key that is no scalar, a key that is no such name, a document that is no mapping, more than one document or a
 * document kaml refuses is refused
 * when the source is read, never when a property is read later. kaml reads the text within the limits of [yaml], which
 * bound its size, depth, nodes and aliases, so a hostile document stops at its budget. Kontext still reads no file:
 * the application hands the text over.
 *
 * The values may be secrets; a source shows only how many properties it holds. A source is immutable and safe to share
 * between threads.
 *
 * @since 1.1.0
 */
public class YamlPropertySource private constructor(private val values: OrderedMap<String, String>) : PropertySource {
    /**
     * The number of properties.
     *
     * @since 1.1.0
     */
    public val size: Int get() = values.size

    override fun valueOf(key: PropertyKey): String? = values[key.value]

    /**
     * Shows the number of properties, never a key or a value.
     *
     * @return the source as text.
     * @since 1.1.0
     */
    override fun toString(): String = "YamlPropertySource($size properties)"

    /**
     * Reads YAML documents into property sources.
     *
     * @since 1.1.0
     */
    public companion object {
        /**
         * Reads the properties of the single document of [text].
         *
         * @param text the YAML text the application read, such as the content of `application.yaml`.
         * @param yaml the YAML configuration, [Yaml.Default] with its limits for untrusted text by default.
         * @return the source.
         * @throws IllegalArgumentException if kaml refuses the text, it holds more or less than one document, or the
         *   document holds a sequence, a key that is no scalar or a key that is no property key. The message names a
         *   refused key escaped, never a value.
         * @since 1.1.0
         */
        public fun parse(text: String, yaml: Yaml = Yaml.Default): YamlPropertySource {
            val parsed = yaml.parse(text)
            require(parsed is YamlParseResult.Parsed) { "The YAML text is not valid within the limits of kaml" }
            val document = parsed.stream.single
            requireNotNull(document) { "The YAML text must hold exactly one document" }
            val root = document.root
            require(root is YamlNode.Mapping) { "The YAML document must be a mapping of properties" }
            val values = OrderedMap<String, String>()
            root.entries.forEach { entry -> flatten(node = entry.value, key = nameOf(entry.key), into = values) }
            return YamlPropertySource(values)
        }

        private fun flatten(node: YamlNode, key: String, into: OrderedMap<String, String>) {
            when (node) {
                is YamlNode.Scalar -> scalar(node = node, key = key, into = into)

                is YamlNode.Mapping -> node.entries.forEach { entry ->
                    flatten(node = entry.value, key = "$key$SEPARATOR${nameOf(entry.key)}", into = into)
                }

                is YamlNode.Sequence -> throw IllegalArgumentException(
                    "A sequence is no property, but ${SafeText.quoted(key)} holds one",
                )
            }
        }

        private fun scalar(node: YamlNode.Scalar, key: String, into: OrderedMap<String, String>) {
            if (node.tag == YamlTag.NULL) return
            PropertyKey(key)
            into[key] = node.value
        }

        private fun nameOf(key: YamlNode): String {
            require(key is YamlNode.Scalar && key.tag != YamlTag.NULL) { "A key of a property must be a text" }
            return key.value
        }
    }
}
