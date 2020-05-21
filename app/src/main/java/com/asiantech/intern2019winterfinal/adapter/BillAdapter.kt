package com.asiantech.intern2019winterfinal.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.model.Particular
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_bill.view.*

class BillAdapter(var activity: Activity, var particulars: MutableList<Particular>) : RecyclerView.Adapter<BillAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_bill, parent, false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val particular = particulars.get(position)
        holder.tvFoodName.setText(particular.name)
        holder.tvPrice.setText(particular.price.toString())
        holder.tvQuantity.setText("x ${particular.quantity}")
        Picasso.with(activity).load(particular.imageUrl).into(holder.imgFood)
    }

    override fun getItemCount(): Int {
        return particulars.size
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood = itemView.imgFood
        val tvFoodName = itemView.tvFoodName
        val tvPrice = itemView.tvPrice
        val tvQuantity = itemView.tvQuantity
    }
}
