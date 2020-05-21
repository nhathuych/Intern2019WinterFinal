package com.asiantech.intern2019winterfinal.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.MyUtil
import kotlinx.android.synthetic.main.item_employee.view.*

class EmployeeAdapter(var activity: Activity, var users: MutableList<User>?) : RecyclerView.Adapter<EmployeeAdapter.ItemViewHolder>() {
    companion object {
        const val ACTION_MENU_BLOCK = "BLOCK"
        const val ACTION_MENU_UN_BLOCK = "UN BLOCK"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_employee, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val user = users?.get(position)
        holder.tvEmail.setText(user?.email)
        holder.tvStatus.setText(if (user?.isActivated == true) R.string.textview_Activated else R.string.textview_Deactivated)
    }

    override fun getItemCount(): Int {
        return users!!.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val tvEmail = itemView.tvName
        val tvStatus = itemView.tvStatus

        init {
            if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_ADMIN) == true) {
                itemView.setOnCreateContextMenuListener(this)
            }
        }

        override fun onCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
            contextMenu?.setHeaderTitle(R.string.context_menu_select_the_action)
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_BLOCK)    // truyền id qua để sửa
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_UN_BLOCK)    // truyền id qua để xóa
        }
    }
}
