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

package com.davils.kontext.core

import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection

private const val HASH_FACTOR: Int = 31
private const val NULLABLE_HASH: Int = 1

@PublishedApi
internal fun sameType(first: KType, second: KType): Boolean {
    val isSameShape = first.classifier == second.classifier && first.isMarkedNullable == second.isMarkedNullable
    return isSameShape && sameArguments(first = first.arguments, second = second.arguments)
}

internal fun typeHash(type: KType): Int {
    val nullability = NULLABLE_HASH.takeIf { type.isMarkedNullable } ?: 0
    val arguments = type.arguments.fold(0) { hash, argument -> HASH_FACTOR * hash + projectionHash(argument) }
    return HASH_FACTOR * (HASH_FACTOR * type.classifier.hashCode() + arguments) + nullability
}

internal fun typeName(type: KType): String = type.toString()

private fun sameArguments(first: List<KTypeProjection>, second: List<KTypeProjection>): Boolean =
    first.zip(second).all { (one, other) -> sameProjection(first = one, second = other) }

private fun sameProjection(first: KTypeProjection, second: KTypeProjection): Boolean {
    val firstType = first.type
    val secondType = second.type
    if (firstType == null || secondType == null) return firstType == secondType && first.variance == second.variance
    return first.variance == second.variance && sameType(first = firstType, second = secondType)
}

private fun projectionHash(projection: KTypeProjection): Int {
    val type = projection.type ?: return 0
    return HASH_FACTOR * projection.variance.hashCode() + typeHash(type)
}
