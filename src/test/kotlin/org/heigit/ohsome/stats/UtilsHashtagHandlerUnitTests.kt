package org.heigit.ohsome.stats

import org.junit.jupiter.api.Assertions.*
import org.heigit.ohsome.stats.utils.HashtagHandler
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

class UtilsHashtagHandlerUnitTests {

    @Test
    fun `check if normal hashtag is wildcard`() {
        val hashtagHandler = HashtagHandler("normalHashtag")
        assertEquals("normalhashtag", hashtagHandler.hashtag)
        assertFalse(hashtagHandler.isWildCard)
    }

    @Test
    fun `check if wildcard hashtag is wildcard`() {
        val hashtagHandler = HashtagHandler("wildcardHashtag*")
        assertEquals("wildcardhashtag", hashtagHandler.hashtag)
        assertTrue(hashtagHandler.isWildCard)
    }

}
