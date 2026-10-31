package com.kscooby.barelink

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkStripTest {
    @Test
    fun cutsQueryOffALink() {
        assertEquals(
            "https://www.ebay.com/itm/1234567890",
            LinkStrip.strip("https://www.ebay.com/itm/1234567890?mkcid=1&campid=9&toolid=10001"),
        )
    }

    @Test
    fun keepsWordsAroundTheLink() {
        assertEquals(
            "see https://a.co/x thanks",
            LinkStrip.strip("see https://a.co/x?utm_source=ig&fbclid=abc thanks"),
        )
    }

    @Test
    fun leavesACleanUrlAlone() {
        assertEquals("https://a.co/x", LinkStrip.strip("https://a.co/x"))
    }

    @Test
    fun keepsSentencePunctuation() {
        assertEquals(
            "https://a.co/x).",
            LinkStrip.strip("https://a.co/x?utm=1)."),
        )
    }

    @Test
    fun cutsEveryLink() {
        assertEquals(
            "https://a.com/z and https://b.com/y",
            LinkStrip.strip("https://a.com/z?x=1 and https://b.com/y?y=2"),
        )
    }

    @Test
    fun ignoresAQuestionMarkThatIsNotALink() {
        assertEquals("what time? not a link", LinkStrip.strip("what time? not a link"))
    }

    @Test
    fun keepsParenthesesThatBelongToTheUrl() {
        val wiki = "https://en.wikipedia.org/wiki/Foo_(bar)"
        assertEquals(wiki, LinkStrip.strip(wiki))
    }

    @Test
    fun trackingModeKeepsYoutubeVideoId() {
        assertEquals(
            "https://www.youtube.com/watch?v=abc",
            LinkStrip.strip("https://www.youtube.com/watch?v=abc&si=xyz&utm_source=share", hard = false),
        )
    }

    @Test
    fun hardModeCutsYoutube() {
        assertEquals(
            "https://www.youtube.com/watch",
            LinkStrip.strip("https://www.youtube.com/watch?v=abc", hard = true),
        )
    }
}
