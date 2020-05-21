package com.asiantech.intern2019winterfinal.model

class Bill {
    var idBill: String = ""
    var idTables = mutableListOf<Int>()
    var particulars = mutableListOf<Particular>()
    var total: Int = 0

    constructor()

    constructor(idBill: String, idTables: MutableList<Int>, particulars: MutableList<Particular>) {
        this.idBill = idBill
        this.idTables = idTables
        this.particulars = particulars
    }

    fun getTotal() : Int? {
        for (particular in particulars) {
            total += particular.price * particular.quantity
        }
        return total
    }

    override fun toString(): String {
        return "Bill[$idBill, ${getTotal()}]"
    }
}
