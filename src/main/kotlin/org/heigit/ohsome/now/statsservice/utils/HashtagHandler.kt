package org.heigit.ohsome.now.statsservice.utils

import java.util.*

/**
 * A hashtag can be
 *  - empty string - the parameter hashtag has been omitted completely or specified without value (e.g. ?hashtag=&starttime=...)
 *  - a string not ending with '*' - representing a normal single case-insensitive hashtag
 *  - a string ending with '*' - representing subtring of a hashtags starting with the chars before '*'
 */
data class HashtagHandler(var hashtag: String) {

    private var isWildCard: Boolean = false

    var variableFilterSQL: String = ""
    var optionalFilterSQL: String = ""

    init {
        this.hashtag = this.hashtag.lowercase(Locale.getDefault())
        detectWildCard(hashtag)

        this.variableFilterSQL = if (this.isWildCard) "startsWith" else "equals"
        this.optionalFilterSQL = when {

            this.isWildCard -> """
                has_hashtags = true
                AND arrayExists(hashtag -> startsWith(hashtag, :hashtag), hashtags)
                AND """.trimIndent()

            !this.isWildCard && this.hashtag.isNotBlank() -> """
                has_hashtags = true
                AND has(hashtags, :hashtag)
                AND """.trimIndent()

            // hashtag isBlank ""
            else -> ""
        }
    }


    fun detectWildCard(hashtag: String) {
        if (hashtag.isBlank()) return

        isWildCard = hashtag.last() == '*'

        if (isWildCard) {
            this.hashtag = hashtag.dropLast(1)
        }

    }

}
