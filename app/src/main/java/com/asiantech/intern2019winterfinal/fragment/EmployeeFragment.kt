package com.asiantech.intern2019winterfinal.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.AddManagerActivity
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.adapter.EmployeeAdapter
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.fragment_employee.*

class EmployeeFragment : Fragment() {
    companion object {
        const val KEY_ID = "size"
    }

    private var spotsDialog: SpotsDialog? = null
    private var db: FirebaseFirestore? = null
    private var employeeAdapter: EmployeeAdapter? = null
    private var users = mutableListOf<User>()
    private var sharedPrefs: SharedPrefs? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_employee, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefs = SharedPrefs(activity as HomeActivity)
        if (MyUtil.getSessionUser()?.role?.equals(HomeActivity.ROLE_ADMIN) == true) {
            setHasOptionsMenu(true)     // chỉ có admin mới thêm dc nhân viên
        }

        db = FirebaseFirestore.getInstance()
        spotsDialog = SpotsDialog(activity)

        //loadEmployees()   // để trong hàm onResume() là hay nhất
        recyclerViewEmployee.setHasFixedSize(true)
        recyclerViewEmployee.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        employeeAdapter = EmployeeAdapter(activity as HomeActivity, users)
        recyclerViewEmployee.adapter = employeeAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(1, R.id.menuAddNewEmployee, 1, R.string.option_menu_add_new_employee)
            .setIcon(R.drawable.logo)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menuAddNewEmployee) {
            startActivity(Intent(activity, AddManagerActivity::class.java))
        }
        return true
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            EmployeeAdapter.ACTION_MENU_BLOCK -> {
                updateManager(item.order, false)    // update isActivated = false
            }
            EmployeeAdapter.ACTION_MENU_UN_BLOCK -> {
                updateManager(item.order, true)     // update isActivated = false
            }
        }
        return true
    }

    private fun updateManager(index: Int, isBlock: Boolean) {
        val user = users[index]
        if ((isBlock == false && user.isActivated == false) || (isBlock == true && user.isActivated == true)) {
            return
        }
        db?.collection(User.TABLE_USER)?.document(user.id.toString())
            ?.update(User.KEY_STATUS, isBlock)
            ?.addOnCompleteListener {
                loadEmployees()
                Toast.makeText(activity, if (isBlock) R.string.toast_un_blocked else R.string.toast_blocked, Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener { exception ->
                Toast.makeText(activity, R.string.toast_failed, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadEmployees()
    }

    private fun loadEmployees() {
        spotsDialog?.show()
        users.clear()
        db?.collection(User.TABLE_USER)
            ?.whereEqualTo(User.KEY_ROLE, HomeActivity.ROLE_MANAGER)
            ?.get()
            ?.addOnCompleteListener { task ->
                for (doc: DocumentSnapshot in task.result!!) {
                    users.add(
                        User(
                            doc.getString(User.KEY_ID),
                            doc.getString(User.KEY_FULLNAME),
                            doc.getString(User.KEY_EMAIL),
                            doc.getString(User.KEY_PASSWORD),
                            doc.getString(User.KEY_ROLE),
                            doc.getBoolean(User.KEY_STATUS)
                        )
                    )
                }
                employeeAdapter?.notifyDataSetChanged()
                spotsDialog?.dismiss()
            }?.addOnFailureListener { e ->
                spotsDialog?.dismiss()
                Log.d("--- error", e.message!!)
            }
    }
}
