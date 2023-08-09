package org.heigit.ohsome.now.stats.utils

import java.util.*

class HashtagHandler(var hashtag: String, var isWildCard: Boolean = false) {
    lateinit var originalHashtag: String

    init {
        this.hashtag = this.hashtag.lowercase(Locale.getDefault())
        this.originalHashtag = this.hashtag
        detectWildCard(hashtag)
    }

    fun detectWildCard(hashtag: String) {
        isWildCard = hashtag.last() == '*'
        if (isWildCard) {
            this.hashtag = hashtag.dropLast(1)
        }
    }
}
