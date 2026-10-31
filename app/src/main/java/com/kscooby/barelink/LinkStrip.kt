package com.kscooby.barelink

/**
 * Cuts tracking junk off links.
 *
 * Hard mode (the default) deletes from the first "?" to the end of the URL.
 * Tracking mode only drops known ad/share parameters and keeps real ones
 * such as YouTube "v=" or a search "q=".
 *
 * No Android imports — unit tests run on a plain JVM.
 */
object LinkStrip {
    private val url = Regex("""https?://[^\s<>"'`]+""", RegexOption.IGNORE_CASE)
    private val trailing = charArrayOf('.', ',', ';', ':', '!', ')', ']', '}', '\'', '"')

    private val trackingKeys = setOf(
        "fbclid", "gclid", "gclsrc", "dclid", "gbraid", "wbraid", "msclkid",
        "mc_eid", "mc_cid", "mkt_tok", "_hsenc", "_hsmi", "igshid", "igsh",
        "yclid", "twclid", "ttclid", "li_fat_id", "srsltid", "gad_source",
        "si", "feature", "vero_id", "vero_conv", "scid", "sc_cid", "s_cid",
        "spm", "scm", "share_id", "share_token", "mkcid", "mkrid", "campid",
        "toolid", "customid", "mkevt", "linkcode", "linkid", "cjevent", "cjdata",
        "epik", "usqp", "ved", "ei", "oq", "gs_lcp", "sxsrf", "fb_ref",
        "fb_source", "oly_anon_id", "oly_enc_id", "_ga", "_gl", "_ke",
        "icid", "ncid", "cmpid", "s_kwcid", "rb_clickid", "wickedid",
        "affiliate_id", "aff_id", "tag", "psc", "th", "nr_email_referer",
    )

    private val trackingPrefixes = listOf("utm_", "pd_rd_", "pf_rd_", "ref_")

    fun strip(text: String, hard: Boolean = true): String {
        return url.replace(text) { match ->
            val raw = match.value
            var end = raw.length
            while (end > 0 && raw[end - 1] in trailing) end--
            val body = raw.substring(0, end)
            val suffix = raw.substring(end)
            val cleaned = if (hard) hardCut(body) else trackingCut(body)
            cleaned + suffix
        }
    }

    private fun hardCut(urlText: String): String {
        val q = urlText.indexOf('?')
        return if (q >= 0) urlText.substring(0, q) else urlText
    }

    private fun trackingCut(urlText: String): String {
        val hash = urlText.indexOf('#')
        val beforeHash = if (hash >= 0) urlText.substring(0, hash) else urlText
        val fragment = if (hash >= 0) urlText.substring(hash) else ""
        val q = beforeHash.indexOf('?')
        if (q < 0) return urlText
        val base = beforeHash.substring(0, q)
        val kept = beforeHash.substring(q + 1)
            .split('&')
            .filter { param ->
                val key = param.substringBefore('=').lowercase()
                key.isNotEmpty() && !isTracking(key)
            }
        return if (kept.isEmpty()) base + fragment else "$base?${kept.joinToString("&")}$fragment"
    }

    private fun isTracking(key: String): Boolean {
        if (key in trackingKeys) return true
        return trackingPrefixes.any { key.startsWith(it) }
    }
}
