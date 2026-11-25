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

package com.davils.kontext.core

import com.davils.kollect.core.meterRegistry
import com.davils.kontext.core.definition.bind
import com.davils.kontext.core.fixture.FixedInventory
import com.davils.kontext.core.fixture.Inventory
import com.davils.kontext.core.fixture.Ledger
import com.davils.kontext.core.fixture.LiveInventory
import com.davils.kontext.core.fixture.Order
import com.davils.kontext.core.observe.ContainerEvent
import com.davils.kontext.core.verify.Verification
import com.davils.kontext.testing.RecordingContainerSink
import com.davils.kontext.testing.TestProfiles
import com.davils.kontext.testing.testContainer
import com.davils.kore.core.annotation.DavilsInternal
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

private const val APPLE: String = "apple"
private const val PLUGIN_NAME: String = "plugin"
private const val STOCK: Int = 3
private val ORDER: ScopeKey = ScopeKey("order")
private val PLUGIN: ScopeKey = ScopeKey("plugin.session")

private fun storage(ledger: Ledger): Module = module("app.storage") {
    single(close = { closed: Ledger -> closed.write("ledger closed") }) { ledger }
    variant(TestProfiles.LIVE) {
        single(close = { _: LiveInventory -> ledger.write("inventory closed") }) {
            LiveInventory(mapOf(APPLE to STOCK))
        }.bind<Inventory>()
    }
    variant(TestProfiles.MOCK) { single<Inventory> { FixedInventory(amount = 0) } }
}

private fun orders(ledger: Ledger = Ledger()): Module = module("app.orders") {
    include(storage(ledger))
    scope(ORDER) {
        provided<String>()
        scoped { Order(item = get(), inventory = get(), log = get()) }
        assisted<Int, String> { amount -> "${get<String>()} x$amount" }
    }
}

class ApplicationTest : FunSpec({
    test("an application resolves its graph in each profile and closes it in reverse order") {
        val sink = RecordingContainerSink()
        val ledger = Ledger()
        val registry = meterRegistry()
        val live = container(TestProfiles.LIVE) {
            name = ContainerName("shop")
            meters = registry
            this.sink = sink
            modules(orders(ledger))
        }

        val order = live.openScope(ORDER) { provide(APPLE) }
        order.get<Order>().isAvailable shouldBe true
        order.get<(Int) -> String>()(2) shouldBe "$APPLE x2"

        val plugin = live.child {
            name = ContainerName(PLUGIN_NAME)
            modules(module(PLUGIN_NAME) { scope(PLUGIN) { scoped { get<Inventory>().count(APPLE) } } })
        }
        plugin.openScope(PLUGIN).get<Int>() shouldBe STOCK

        live.close().isClean shouldBe true

        ledger.entries shouldBe listOf("ledger closed", "inventory closed")
        sink.of<ContainerEvent.Closed>().map { event -> event.container.value } shouldBe listOf(PLUGIN_NAME, "shop")
        order.isOpen shouldBe false
    }

    test("a test container replaces a definition of the application") {
        val container = testContainer {
            modules(orders())
            replace(module("fake.inventory") { variant(TestProfiles.MOCK) { single<Inventory> { FixedInventory(5) } } })
        }

        container.openScope(ORDER) { provide("pear") }.get<Order>().isAvailable shouldBe true
        container.close()
    }

    test("verification passes the application in every profile") {
        val verification = verify(setOf(TestProfiles.LIVE, TestProfiles.MOCK)) { modules(orders()) }

        verification.shouldBeInstanceOf<Verification.Passed>()
    }
})
