package com.asiantech.intern2019winterfinal.adapter

import android.app.Activity
import android.graphics.Color
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.model.Food
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_food.view.*

class MenuAdapter(val activity: Activity? = null, val foods: MutableList<Food>) : RecyclerView.Adapter<MenuAdapter.MyViewHolder>() {
    companion object {
        const val ACTION_MENU_DELETE = "DELETE"
        const val ACTION_MENU_UPDATE = "UPDATE"
        private val COLOR_1 = Color.parseColor("#B3DEFFDE")
        private val COLOR_2 = Color.parseColor("#B3F3F8BF")
    }
    internal var onSelectFood: (Int, String, String, Int, Int) -> Unit = { a, b, c, d, e -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_food, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.setBackgroundColor(if (position % 2 == 0) COLOR_1 else COLOR_2)
        val currFood = foods[position]
        holder.tvFoodName.text = currFood.name
        holder.tvPrice.text = currFood.price
        holder.tvId.text = currFood.id.toString()
        holder.tvImageUrl.text = currFood.photo
        Picasso.with(activity).load(currFood.photo).into(holder.imgFood)
    }

    override fun getItemCount(): Int {
        return foods.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnCreateContextMenuListener {
        val imgFood = itemView.imgFood
        val tvFoodName = itemView.tvFoodName
        val tvPrice = itemView.tvPrice
        val tvId = itemView.tvId
        val tvImageUrl = itemView.tvImageUrl
        val btnSelect = itemView.btnSelect

        init {
            if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_USER) == false) {
                itemView.setOnCreateContextMenuListener(this)
            }

            btnSelect.setOnClickListener {
                onSelectFood(
                    tvId.text.toString().toInt(),
                    tvFoodName.text.toString(),
                    tvImageUrl.text.toString(),
                    tvPrice.text.toString().toInt(),
                    adapterPosition
                )
            }
        }

        override fun onCreateContextMenu(contextMenu: ContextMenu?, view: View?, contextMenuInfo: ContextMenu.ContextMenuInfo?) {
            contextMenu?.setHeaderTitle(R.string.context_menu_select_the_action)
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_UPDATE)    // truyền id qua để sửa
            contextMenu?.add(0, 0, adapterPosition, ACTION_MENU_DELETE)    // truyền id qua để xóa
        }
    }
}
