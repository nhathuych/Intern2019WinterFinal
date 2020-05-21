package com.asiantech.intern2019winterfinal.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.AddNewFoodActivity
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.activity.UpdateFoodActivity
import com.asiantech.intern2019winterfinal.adapter.MenuAdapter
import com.asiantech.intern2019winterfinal.model.Bill
import com.asiantech.intern2019winterfinal.model.Food
import com.asiantech.intern2019winterfinal.model.Particular
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.DocumentSnapshot
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_menu.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.dialog_select_quantity.view.*

class MenuFragment : Fragment() {
    companion object {
        const val KEY_CURRENT_FOOD = "currentFood"
        const val KEY_SIZE = "size"
        var bill: Bill? = null      // replace fragment ko bị mất
        var particulars: MutableList<Particular>? = null    // replace fragment ko bị mất
        var FLAG_IS_ORDERED = false     // cập nhật lại bên BillFragment
    }
    private var spotsDialog: SpotsDialog? = null
    private var foods: MutableList<Food>? = mutableListOf()
    private var menuAdapter: MenuAdapter? = null
    private var db: FirebaseFirestore? = null
    private var sharedPrefs: SharedPrefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_USER) == false) {
            setHasOptionsMenu(true)
        }

        db = FirebaseFirestore.getInstance()
        spotsDialog = SpotsDialog(activity)
        sharedPrefs = SharedPrefs(activity as HomeActivity)

        //loadMenu()    // gọi ở onResume() là hay nhất
        recyclerViewMenu.setHasFixedSize(true)
        recyclerViewMenu.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        menuAdapter = MenuAdapter(activity, foods!!)
        recyclerViewMenu.adapter = menuAdapter

        menuAdapter?.onSelectFood = this::handleSelectFood
    }

    private fun handleSelectFood(idFood: Int, foodName: String, imageUrl: String, price: Int, position: Int) {
        val str = sharedPrefs?.getString(SharedPrefs.KEY_LIST_TABLE_SELECTED)
        if (str != null || str != "null") {     // str != "null" bá đạo
            val listIdTableSelected = getListIdTableSelected(str.toString())
            if (listIdTableSelected?.isEmpty() == false) {
                // show Dialog chọn số lượng
                val view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_quantity, null, false)
                val spnQuantity = view.spnQuantity

                val adapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(activity as HomeActivity, R.array.food_quantity, R.layout.item_spinner_quantity)
                spnQuantity.adapter = adapter

                AlertDialog.Builder(activity)
                    .setView(view)
                    .setTitle(R.string.dialog_quantity)
                    .setPositiveButton(R.string.button_confirm, object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            if (bill == null) {
                                particulars = mutableListOf()
                                bill = Bill(System.currentTimeMillis().toString(), listIdTableSelected, particulars!!)
                                FLAG_IS_ORDERED = true
                            }

                            val index = duplicatePossition(idFood)
                            // nếu mã món đã tồn tại trong hóa đơn thì thêm số tượng
                            if (index != -1) {
                                particulars!!.get(index).quantity += spnQuantity.selectedItemPosition + 1
                            } else { // ngược lại thêm vào hóa đơn
                                particulars?.add(Particular(particulars!!.size + 1, idFood, foodName, imageUrl, price, spnQuantity.selectedItemPosition + 1))
                            }

                            bill?.particulars = particulars!!
                            // convert đối tượng bill thành json và lưu xuống SharePreferances
                            sharedPrefs?.putString(SharedPrefs.KEY_BILL, Gson().toJson(bill))
                        }
                    }).setNegativeButton(R.string.button_cancel, object : DialogInterface.OnClickListener {
                        override fun onClick(p0: DialogInterface?, p1: Int) {}
                    })
                    .show()
            } else {
                Snackbar.make((activity as HomeActivity).recyclerViewMenu, R.string.toast_no_table, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.button_yes) {
                        activity?.navigationView?.menu?.getItem(0)?.setChecked(true)
                        (activity as HomeActivity).onNavigationItemSelected(activity!!.navigationView.menu.getItem(0))
                    }.show()
            }
        }
    }

    private fun getListIdTableSelected(str: String): MutableList<Int>? {
        var list: MutableList<Int>? = null
        val type = object : TypeToken<MutableList<Int>>() {}.type
        list = Gson().fromJson(str, type)
        return list
    }

    // trả về vị trí bị trùng, nếu ko có trả về -1
    private fun duplicatePossition(id: Int): Int {
        val n = particulars?.size?.minus(1) as Int     // size - 1
        if (n == -1) {  // nếu size == 0 thì trả về vị trí là -1
            return -1
        }
        for (idx in 0..n) {
            if (id == particulars!!.get(idx).idFood) {    // nếu idFood đã tồn tại trong danh sách thì trả về true
                return idx
            }
        }
        return -1
    }

    override fun onResume() {
        super.onResume()
        loadMenu()
    }

    private fun loadMenu() {
        spotsDialog?.show()
        foods?.clear()
        db?.collection(Food.TABLE_MENU)?.get()
            ?.addOnCompleteListener { task ->
                for (doc: DocumentSnapshot in task.result!!) {
                    foods?.add(
                        Food(
                            doc.get(Food.KEY_ID).toString().toInt(),
                            doc.getString(Food.KEY_NAME),
                            doc.getString(Food.KEY_PRICE),
                            doc.getString(Food.KEY_IMAGE)
                        )
                    )
                }
                menuAdapter?.notifyDataSetChanged()
                spotsDialog?.dismiss()
            }
            ?.addOnFailureListener { e ->
                spotsDialog?.dismiss()
                Log.d("--- error", e.message!!)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(1, R.id.menuAddNewFood, 1, R.string.option_menu_new_food)
            .setIcon(R.drawable.logo)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuAddNewFood) {
            val intent = Intent(activity, AddNewFoodActivity::class.java)
            intent.putExtra(KEY_SIZE, foods?.size)    // lấy size làm id cho food
            startActivity(intent)
        }
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            MenuAdapter.ACTION_MENU_DELETE -> {
                deleteFood(item.order)
            }
            MenuAdapter.ACTION_MENU_UPDATE -> {
                val intent = Intent(activity, UpdateFoodActivity::class.java)
                intent.putExtra(KEY_CURRENT_FOOD, foods?.get(item.order))
                startActivity(intent)
            }
        }
        return true
    }

    private fun deleteFood(index: Int) {
        db?.collection(Food.TABLE_MENU)
            ?.document(foods?.get(index)?.id.toString())
            ?.delete()
            ?.addOnSuccessListener {
                loadMenu()
            }
    }
}
