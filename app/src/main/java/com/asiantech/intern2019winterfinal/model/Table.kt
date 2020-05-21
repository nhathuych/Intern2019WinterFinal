package com.asiantech.intern2019winterfinal.model

class Table {
    companion object {
        const val TABLES: String = "tables"
        const val KEY_ID: String = "id"
        const val KEY_NAME: String = "name"
        const val KEY_ID_USER_SELECTED: String = "idUserSelected"
    }

    var id: Int = 0
    var tableName: String? = null
    var idUserSelected: String? = "0"   // nếu bàn ăn dc chọn thì sẽ lưu idUser

    constructor() {
    }

    constructor(id: Int, tableName: String?, isSelected: String?) {
        this.id = id
        this.tableName = tableName
        this.idUserSelected = isSelected
    }

    override fun toString(): String {
        return "table[$id, $tableName, $idUserSelected]"
    }
}
