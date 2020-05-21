package com.asiantech.intern2019winterfinal.adapter

import android.app.Activity
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.model.Table
import com.asiantech.intern2019winterfinal.utils.MyUtil
import kotlinx.android.synthetic.main.item_table.view.*

class TableAdapter(var activity: Activity, var tables: MutableList<Table>?) : RecyclerView.Adapter<TableAdapter.ItemViewHolder>() {
    companion object {
        const val ID_TABLE_SELECTED = "idTableSelected"
        const val ACTION_MENU_DELETE = "DELETE"
        const val ACTION_MENU_UPDATE = "UPDATE"
    }

    internal var onOrderClick: (Int, Int) -> Unit = { a, b -> }
    internal var onCancelClick: (Int, Int) -> Unit = { a, b -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_table, parent, false))
    }

    override fun getItemCount(): Int {
        return if (tables.isNullOrEmpty()) 0 else tables!!.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val table = tables?.get(position)
        holder.imgTable.setImageResource(if (table?.idUserSelected != "0") R.drawable.banantrue else R.drawable.banan)
        holder.tvTableName.text = table?.tableName
        holder.tvTableId.text = table?.id.toString()
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val imgTable = itemView.imgTable
        val tvTableName = itemView.tvTableName
        val tvTableId = itemView.tvId

        init {
            if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_USER) == false) {
                itemView.setOnCreateContextMenuListener(this)
            }

            itemView.imgOrder.setOnClickListener {
                val id = tvTableId.text.toString().toInt()
                onOrderClick(id, adapterPosition)
            }

            itemView.imgCancel.setOnClickListener {
                val id = tvTableId.text.toString().toInt()
                onCancelClick(id, adapterPosition)
            }
        }

        override fun onCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
            contextMenu?.setHeaderTitle(R.string.context_menu_select_the_action)
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_UPDATE)    // truyền position qua để sửa
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_DELETE)    // truyền position qua để xóa
        }
    }
}
