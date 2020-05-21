package com.asiantech.intern2019winterfinal.model

class Particular {
    var id: Int = 0
    var idFood: Int = 0
    var name: String? = ""
    var imageUrl: String? = ""
    var price: Int = 0
    var quantity: Int = 0

    constructor()

    constructor(id: Int, idFood: Int, name: String?, imageUrl: String?, price: Int, quantity: Int) {
        this.id = id
        this.idFood = idFood
        this.name = name
        this.imageUrl = imageUrl
        this.price = price
        this.quantity = quantity
    }

    override fun toString(): String {
        return "Particular[$id, $price, $quantity]"
    }
}
