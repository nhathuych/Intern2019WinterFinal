package com.asiantech.intern2019winterfinal.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.adapter.BillAdapter
import com.asiantech.intern2019winterfinal.adapter.TableAdapter
import com.asiantech.intern2019winterfinal.model.Bill
import com.asiantech.intern2019winterfinal.model.Table
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_bill.*

class BillFragment : Fragment() {
    private var db: FirebaseFirestore? = null
    private var sharedPrefs: SharedPrefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_bill, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        sharedPrefs = activity?.let { SharedPrefs(it) }
        val bill = Gson().fromJson(sharedPrefs?.getString(SharedPrefs.KEY_BILL), Bill::class.java)

        tvUser.text = "User: ${MyUtil.getSessionUser()?.fullName}"
        tvTotal.text = "total: ${bill?.getTotal()?.toString()} $"

        if (bill != null) {
            recyclerViewBill.setHasFixedSize(true)
            recyclerViewBill.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            recyclerViewBill.adapter = activity?.let { BillAdapter(it, bill.particulars) }
        }

        btnCancel.setOnClickListener { activity?.onBackPressed() }
        btnConfirm.setOnClickListener { payment() }
    }

    private fun payment() {
        // cập nhật lại tất cả table = false
        val str = sharedPrefs?.getString(SharedPrefs.KEY_LIST_TABLE_SELECTED)
        if (str != null || str != "null") {     // str != "null" bá đạo
            val listIdTableSelected = getListIdTableSelected(str.toString())
            if (listIdTableSelected != null && listIdTableSelected.size >= 1) {
                for (i in 0..(listIdTableSelected.size - 1)) {
                    db?.collection(Table.TABLES)?.document(listIdTableSelected[i].toString())
                        ?.update(Table.KEY_ID_USER_SELECTED, "0")
                }
            }
        }

        MenuFragment.bill = null
        MenuFragment.particulars = null
        MenuFragment.FLAG_IS_ORDERED = false

        sharedPrefs?.putInt(TableAdapter.ID_TABLE_SELECTED, -1)
        sharedPrefs?.putString(SharedPrefs.KEY_BILL, "")
        sharedPrefs?.putString(SharedPrefs.KEY_LIST_TABLE_SELECTED, "[]")

        (activity as HomeActivity).updateMenu(0)
    }

    private fun getListIdTableSelected(str: String): MutableList<Int>? {
        var list: MutableList<Int>? = null
        val type = object : TypeToken<MutableList<Int>>() {}.type
        list = Gson().fromJson(str, type)
        return list
    }
}
