package org.heigit.ohsome.stats.utils

class HashtagHandler(var hashtag: String, var isWildCard: Boolean = false) {
    init {
        detectWildCard(hashtag)
    }

    fun detectWildCard(hashtag: String) {
        isWildCard = hashtag.last() == '*'
        if (isWildCard) {
            this.hashtag.dropLast(1)
        }
    }
}
