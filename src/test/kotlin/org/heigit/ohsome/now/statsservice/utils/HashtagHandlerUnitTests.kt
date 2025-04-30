package org.heigit.ohsome.now.statsservice.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class HashtagHandlerUnitTests {


    @Test
    fun `check if normal hashtag is wildcard`() {

        val hashtagHandler = HashtagHandler("normalHashtag")
        val expectedFilterSQL = """
                has_hashtags = true
                AND has(hashtags, :hashtag)
                AND """.trimIndent()

        assertEquals("normalhashtag", hashtagHandler.hashtag)
        assertEquals(expectedFilterSQL, hashtagHandler.optionalFilterSQL)
    }


    @Test
    fun `check if wildcard hashtag is wildcard`() {

        val hashtagHandler = HashtagHandler("wildcardHashtag*")
        val expectedFilterSQL = """
                has_hashtags = true
                AND arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
                AND """.trimIndent()

        assertEquals("wildcardhashtag", hashtagHandler.hashtag)
        assertEquals(expectedFilterSQL, hashtagHandler.optionalFilterSQL)
    }

    @Test
    fun `can handle empty hashtag`() {
        val hashtagHandler = HashtagHandler("")
        val expectedFilterSQL = ""

        assertEquals("", hashtagHandler.hashtag)
        assertEquals(expectedFilterSQL, hashtagHandler.optionalFilterSQL)
    }


}
