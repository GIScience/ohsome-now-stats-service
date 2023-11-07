package org.heigit.ohsome.now.stats.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class HashtagHandlerUnitTests {


    @Test
    fun `check if normal hashtag is wildcard`() {

        val hashtagHandler = HashtagHandler("normalHashtag")

        assertEquals("normalhashtag", hashtagHandler.hashtag)
        assertEquals("equals", hashtagHandler.variableFilterSQL)
    }


    @Test
    fun `check if wildcard hashtag is wildcard`() {

        val hashtagHandler = HashtagHandler("wildcardHashtag*")

        assertEquals("wildcardhashtag", hashtagHandler.hashtag)
        assertEquals("startsWith", hashtagHandler.variableFilterSQL)
    }


}
