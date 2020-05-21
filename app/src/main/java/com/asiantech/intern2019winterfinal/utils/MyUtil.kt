package com.asiantech.intern2019winterfinal.utils

import com.asiantech.intern2019winterfinal.model.User

object MyUtil {
    private var currentUser: User? = null

    fun getSessionUser() : User? {
        return currentUser
    }

    fun setSessionUser(user: User?) {
        if (user == null) {
            this.currentUser = User()
        }
        this.currentUser = user
    }
}
