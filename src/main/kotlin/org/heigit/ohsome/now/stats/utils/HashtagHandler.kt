package org.heigit.ohsome.now.stats.utils

import java.util.*


data class HashtagHandler(var hashtag: String) {

    private var isWildCard: Boolean = false

    var variableFilterSQL: String = ""


    init {
        this.hashtag = this.hashtag.lowercase(Locale.getDefault())
        detectWildCard(hashtag)

        this.variableFilterSQL = if (this.isWildCard) "startsWith" else "equals"

    }


    fun detectWildCard(hashtag: String) {
        isWildCard = hashtag.last() == '*'

        if (isWildCard) {
            this.hashtag = hashtag.dropLast(1)
        }

    }

}
