package org.heigit.ohsome.now.statsservice.utils

data class UserHandler(val userIds: List<Int>) {
    var optionalFilterSQL = ""
    var userTableIdentifier = ""

    init {
        if (userIds.isNotEmpty()) {
            this.optionalFilterSQL = "AND user_id in (${userIds.joinToString(",")})"
            this.userTableIdentifier = "_user"
        }
    }

}