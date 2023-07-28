package org.heigit.ohsome.now.stats

import org.heigit.ohsome.now.stats.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

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
