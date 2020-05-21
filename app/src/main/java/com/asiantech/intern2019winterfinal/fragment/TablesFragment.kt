package com.asiantech.intern2019winterfinal.fragment

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.asiantech.intern2019winterfinal.R
import androidx.recyclerview.widget.GridLayoutManager
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.adapter.TableAdapter
import com.asiantech.intern2019winterfinal.model.Table
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.dialog_add_new_table.view.*
import kotlinx.android.synthetic.main.fragment_tables.*
import java.lang.Exception

class TablesFragment : Fragment() {
    companion object {
        var listIdTableSelected: MutableList<Int>? = null     // cập nhật lại bên BillFragment
    }
    private var spotsDialog: SpotsDialog? = null
    private var tableAdapter: TableAdapter? = null
    private var tables = mutableListOf<Table>()
    private var db: FirebaseFirestore? = null
    private var sharedPrefs: SharedPrefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tables, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_USER) == false) {
            setHasOptionsMenu(true)
        }

        db = FirebaseFirestore.getInstance()
        spotsDialog = SpotsDialog(activity)
        sharedPrefs = SharedPrefs(activity as HomeActivity)

        //loadTables() gọi trong hàm onResume() là hay nhất
        val gridLayoutManager = GridLayoutManager(activity, 2)
        recyclerView.layoutManager = gridLayoutManager
        tableAdapter = TableAdapter(activity!!, tables)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = tableAdapter

        tableAdapter?.onOrderClick = this::handleOrderClick
        tableAdapter?.onCancelClick = this::handleCancelClick
    }

    override fun onResume() {
        super.onResume()
        loadTables()
    }

    // nếu đã chọn món thì ko thể hủy
    fun handleCancelClick(idTable: Int, position: Int) {
        // nếu đã gọi món thì ko cho hủy
        if (MenuFragment.FLAG_IS_ORDERED) {
            Toast.makeText(activity, R.string.toast_pay_to_unselect, Toast.LENGTH_SHORT).show()
            return
        }

        // nếu bàn có IdUser bằng với IdUser đang đăng nhập thì cho hủy
        if (tables.get(position).idUserSelected == MyUtil.getSessionUser()?.id) {
            FirebaseFirestore.getInstance().collection(Table.TABLES).document(idTable.toString())
                .update(Table.KEY_ID_USER_SELECTED, "0")
                .addOnCompleteListener {
                    tables.get(position).idUserSelected = "0"
                    listIdTableSelected?.remove(idTable)
                    sharedPrefs?.putString(SharedPrefs.KEY_LIST_TABLE_SELECTED, Gson().toJson(listIdTableSelected))

                    tableAdapter?.notifyItemChanged(position)
                }
        }
    }

    // cập nhật lại tình trạng bàn
    // lưu danh sách bàn xuống sharedPrefs
    fun handleOrderClick(idTable: Int, position: Int) {
        // nếu bàn trống thì cho chọn
        if (tables.get(position).idUserSelected == "0") {
            FirebaseFirestore.getInstance().collection(Table.TABLES).document(idTable.toString())
                .update(Table.KEY_ID_USER_SELECTED, MyUtil.getSessionUser()?.id)
                .addOnCompleteListener {
                    tables.get(position).idUserSelected = MyUtil.getSessionUser()?.id
                    if (listIdTableSelected == null) {
                        listIdTableSelected = mutableListOf()
                    }
                    listIdTableSelected?.add(idTable)
                    sharedPrefs?.putString(SharedPrefs.KEY_LIST_TABLE_SELECTED, Gson().toJson(listIdTableSelected))

                    tableAdapter?.notifyItemChanged(position)
                }
        }
    }

    private fun loadTables() {
        spotsDialog?.show()
        tables.clear()
        db?.collection(Table.TABLES)?.get()
            ?.addOnCompleteListener { task ->
                for (doc: DocumentSnapshot in task.result!!) {
                    tables.add(
                        Table(
                            doc.get(Table.KEY_ID).toString().toInt(),
                            doc.getString(Table.KEY_NAME),
                            doc.getString(Table.KEY_ID_USER_SELECTED)
                        )
                    )
                }
                tableAdapter?.notifyDataSetChanged()
                spotsDialog?.dismiss()
            }
            ?.addOnFailureListener { e ->
                spotsDialog?.dismiss()
                Log.d("--- error", e.message!!)
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(1, R.id.menuAddNewTable, 1, R.string.option_menu_new_table)
            .setIcon(R.drawable.thembanan)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuAddNewTable) {
            val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_add_new_table, null, false)
            AlertDialog.Builder(activity)
                .setView(view)
                .setTitle(R.string.option_menu_new_table)
                .setPositiveButton(R.string.button_submit, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        val name = view.edtTableName.text.toString().trim()
                        if (!name.isEmpty()) {
                            val table = HashMap<String, Any>()
                            val id = tables.size + 1
                            table.put(Table.KEY_ID, id)
                            table.put(Table.KEY_NAME, name)
                            table.put(Table.KEY_ID_USER_SELECTED, "0")

                            db?.collection(Table.TABLES)?.document(id.toString())?.set(table)
                                ?.addOnSuccessListener {
                                    loadTables()
                                    Toast.makeText(activity, R.string.toast_added, Toast.LENGTH_SHORT).show()
                                }
                                ?.addOnFailureListener(object : OnFailureListener {
                                    override fun onFailure(e: Exception) {
                                        Log.d("--- error", e.message!!)
                                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                                    }
                                })
                        }
                    }
                })
                .setNegativeButton(R.string.button_cancel, object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {}
                })
                .show()
        }
        return true;
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            TableAdapter.ACTION_MENU_DELETE -> {
                if (tables.get(item.order).idUserSelected != "0") {
                    Toast.makeText(activity, R.string.toast_cant_remove_this_table, Toast.LENGTH_SHORT).show()
                } else {
                    deleteTable(item.order)
                }
            }
            TableAdapter.ACTION_MENU_UPDATE -> {
                showPopupUpdateTable(item.order)
            }
        }
        return true
    }

    private fun showPopupUpdateTable(index: Int) {
        val table = tables.get(index)
        val view: View = LayoutInflater.from(activity).inflate(R.layout.dialog_add_new_table, null, false)
        AlertDialog.Builder(activity)
            .setView(view)
            .setTitle(R.string.dialog_update_table)
            .setPositiveButton(R.string.button_submit, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {
                    val name = view.edtTableName.text.toString().trim()
                    if (!name.isEmpty()) {
                        db?.collection(Table.TABLES)?.document(table.id.toString())
                            ?.update(Table.KEY_NAME, name)
                            ?.addOnCompleteListener {
                                loadTables()
                                Toast.makeText(activity, R.string.toast_updated, Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            })
            .setNegativeButton(R.string.button_cancel, object : DialogInterface.OnClickListener {
                override fun onClick(p0: DialogInterface?, p1: Int) {}
            })
            .show()
    }

    private fun deleteTable(index: Int) {
        db?.collection(Table.TABLES)
            ?.document(tables.get(index).id.toString())
            ?.delete()
            ?.addOnSuccessListener {
                loadTables()
            }
    }
}
