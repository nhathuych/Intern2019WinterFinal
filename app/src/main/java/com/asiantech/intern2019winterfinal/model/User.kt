package com.asiantech.intern2019winterfinal.model

class User {
    companion object {
        const val TABLE_USER = "user"
        const val KEY_ID = "id"
        const val KEY_FULLNAME = "fullName"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_ROLE = "role"
        const val KEY_STATUS = "status"
    }
    var id: String? = ""
    var fullName: String? = ""
    var email: String? = ""
    var password: String? = ""
    var role: String? = ""
    var isActivated: Boolean? = false

    constructor()

    constructor(id: String?, fullName: String?, email: String?, password: String?, role: String?, isActivated: Boolean?) {
        this.id = id
        this.fullName = fullName
        this.email = email
        this.password = password
        this.role = role
        this.isActivated = isActivated
    }

    override fun toString(): String {
        return "User[$id, $email]"
    }
}
