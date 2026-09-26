package com.autoskip.app

import java.text.Normalizer
import java.util.Locale

/**
 * Pure text/ID matching rules for YouTube's skip-ad button. Kept free of Android
 * types so it can be unit tested on the JVM.
 */
object SkipAdMatcher {

    const val YOUTUBE_PACKAGE = "com.google.android.youtube"

    /** Resource IDs YouTube has used for the skip button across versions. */
    val SKIP_VIEW_IDS = listOf(
        "skip_ad_button",
        "skip_ad_button_container",
        "modern_skip_ad_text",
        "skip_ad_button_text",
    )

    /**
     * Full "skip ad" labels only. Bare words like "Skip" or "Saltar" are deliberately
     * excluded: they only count when the view ID already identifies the skip button.
     */
    private val RAW_PHRASES = listOf(
        // English
        "Skip ad", "Skip ads",
        // Español / Català / Galego / Euskara
        "Saltar anuncio", "Saltar anuncios", "Omitir anuncio", "Omitir anuncios",
        "Omet l'anunci", "Omet els anuncis", "Salta l'anunci", "Saltatu iragarkia",
        // Português
        "Pular anúncio", "Pular anúncios", "Ignorar anúncio", "Ignorar anúncios", "Saltar anúncio",
        // Français
        "Passer l'annonce", "Passer les annonces", "Ignorer l'annonce", "Ignorer les annonces",
        // Deutsch
        "Werbung überspringen", "Anzeige überspringen", "Anzeigen überspringen",
        // Italiano
        "Salta annuncio", "Salta annunci", "Ignora annuncio", "Ignora annunci",
        // Nederlands
        "Advertentie overslaan", "Advertenties overslaan",
        // Nordic
        "Hoppa över annons", "Hoppa över annonser", "Spring annonce over", "Spring annoncer over",
        "Hopp over annonsen", "Hopp over annonser", "Ohita mainos", "Ohita mainokset",
        // Central / Eastern Europe
        "Pomiń reklamę", "Pomiń reklamy", "Přeskočit reklamu", "Preskočiť reklamu",
        "Reklám átugrása", "Omite anunțul", "Omite anunțurile",
        "Пропустить рекламу", "Пропустити рекламу", "Пропусни рекламата",
        "Παράλειψη διαφήμισης", "Παράβλεψη διαφήμισης",
        // Türkçe
        "Reklamı atla", "Reklamları atla",
        // Middle East / South Asia
        "تخطي الإعلان", "تخطي الإعلانات", "דלג על המודעה", "דילוג על המודעה",
        "विज्ञापन छोड़ें",
        // East / South-East Asia
        "広告をスキップ", "跳过广告", "略過廣告", "跳過廣告", "광고 건너뛰기",
        "Lewati iklan", "Langkau iklan", "Bỏ qua quảng cáo", "ข้ามโฆษณา", "Laktawan ang ad",
    )

    private const val MAX_LABEL_LENGTH = 40

    private val COMBINING_MARKS = Regex("\\p{Mn}+")
    private val NON_ALPHANUMERIC = Regex("[^\\p{L}\\p{N}]+")

    private val PHRASES: Set<String> = RAW_PHRASES.map(::normalize).toSet()

    /** Lowercases, strips accents and collapses punctuation so "Passer l’annonce ▸" == "passer l annonce". */
    fun normalize(text: CharSequence): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase(Locale.ROOT)
            .replace(NON_ALPHANUMERIC, " ")
            .trim()

    /** True only when the whole label is a known "skip ad" phrase. */
    fun isSkipText(text: CharSequence?): Boolean {
        if (text.isNullOrBlank() || text.length > MAX_LABEL_LENGTH) return false
        return normalize(text) in PHRASES
    }
}
