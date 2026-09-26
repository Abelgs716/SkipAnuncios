package com.autoskip.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipAdMatcherTest {

    @Test
    fun `matches skip labels in several languages`() {
        listOf(
            "Skip ad", "Skip Ads", "Saltar anuncio", "Omitir anuncios", "Passer l’annonce",
            "Werbung überspringen", "Salta annuncio", "Pular anúncio", "Пропустить рекламу",
            "Reklamı atla", "広告をスキップ", "광고 건너뛰기",
        ).forEach { assertTrue(it, SkipAdMatcher.isSkipText(it)) }
    }

    @Test
    fun `tolerates case, accents, arrows and extra spaces`() {
        assertTrue(SkipAdMatcher.isSkipText("  SKIP AD ▸ "))
        assertTrue(SkipAdMatcher.isSkipText("saltar anuncio"))
        assertTrue(SkipAdMatcher.isSkipText("Pular anuncio"))
    }

    @Test
    fun `ignores other YouTube controls`() {
        listOf(
            null, "", "Skip", "Saltar", "Suscribirse", "Subscribe", "Me gusta", "Like",
            "Comentarios", "Buscar", "Skip ad in 5", "Ad · 0:15", "Visit advertiser",
            "Saltar anuncio " + "x".repeat(40),
        ).forEach { assertFalse(it.toString(), SkipAdMatcher.isSkipText(it)) }
    }
}
