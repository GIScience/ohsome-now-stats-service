package org.heigit.ohsome.now.statsservice.utils

data class UserHandler(val userId: String) {
    var optionalFilterSQL = ""
    var userTableIdentifier = ""

    init {
        if (userId != "") {
            this.optionalFilterSQL = "AND user_id=$userId"
            this.userTableIdentifier = "_user"
        }
    }

}