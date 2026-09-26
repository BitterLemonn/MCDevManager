package com.lemon.mcdevmanagermp

import com.lemon.mcdevmanagermp.utils.CookiesStore
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CookiesStoreTest {
    @BeforeTest
    fun setUp() = CookiesStore.clearCookies()

    @AfterTest
    fun tearDown() = CookiesStore.clearCookies()

    @Test
    fun preservesTrailingEqualsInCookieValue() {
        CookiesStore.addCookies(listOf("session=example==; Path=/; HttpOnly"))

        assertEquals("example==", CookiesStore.getCookie("session"))
    }

    @Test
    fun preservesEmbeddedEqualsInCookieValue() {
        CookiesStore.addCookies(listOf("session=first=second=third; Secure"))

        assertEquals("first=second=third", CookiesStore.getCookie("session"))
    }

    @Test
    fun parsesOrdinaryCookiesWithoutStoringAttributes() {
        CookiesStore.addCookies(listOf("first=one; Path=/", "second=two; HttpOnly"))

        assertEquals(mapOf("first" to "one", "second" to "two"), CookiesStore.getAllCookiesMap())
    }

    @Test
    fun skipsMalformedEntriesAndContinuesWithValidCookies() {
        CookiesStore.addCookies(listOf("", "missing-separator", "=missing-name", "  =missing-name", "valid=ok"))

        assertEquals(mapOf("valid" to "ok"), CookiesStore.getAllCookiesMap())
    }

    @Test
    fun trimsWhitespaceAroundCookieNameAndValue() {
        CookiesStore.addCookies(listOf(" session = example== ; Path=/"))

        assertEquals(mapOf("session" to "example=="), CookiesStore.getAllCookiesMap())
    }

    @Test
    fun preservesExistingEmptyValueBehavior() {
        CookiesStore.addCookies(listOf("session=; Path=/"))

        assertEquals("", CookiesStore.getCookie("session"))
    }

    @Test
    fun updatesExistingCookieWithCompleteValue() {
        CookiesStore.addCookie("session", "old")
        CookiesStore.addCookies(listOf("session=new==; Path=/"))

        assertEquals("new==", CookiesStore.getCookie("session"))
    }
}
