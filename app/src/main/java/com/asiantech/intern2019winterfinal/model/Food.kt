package com.asiantech.intern2019winterfinal.model

import java.io.Serializable

class Food : Serializable {
    companion object {
        const val TABLE_MENU: String = "menu"
        const val KEY_ID: String = "id"
        const val KEY_NAME: String = "name"
        const val KEY_PRICE: String = "price"
        const val KEY_IMAGE: String = "photo"
    }

    var id: Int = 0
    var name: String? = ""
    var price: String? = ""
    var photo: String? = ""

    constructor() {
    }

    constructor(id: Int, name: String?, price: String?, photo: String?) {
        this.id = id
        this.name = name
        this.price = price
        this.photo = photo
    }

    override fun toString(): String {
        return "Food[$id, $name, $price, $photo]"
    }
}
