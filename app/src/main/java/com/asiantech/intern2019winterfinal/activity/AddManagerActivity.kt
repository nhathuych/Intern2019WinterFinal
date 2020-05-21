package com.asiantech.intern2019winterfinal.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.ValidationUtil
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_add_manager.*
import kotlinx.android.synthetic.main.activity_add_manager.btnRegister
import kotlinx.android.synthetic.main.activity_add_manager.edtEmail
import kotlinx.android.synthetic.main.activity_add_manager.edtFullName
import kotlinx.android.synthetic.main.activity_add_manager.edtPassword
import java.lang.Exception
import java.util.*
import kotlin.collections.HashMap

class AddManagerActivity : AppCompatActivity() {
    private var spotsDialog: SpotsDialog? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_manager)

        spotsDialog = SpotsDialog(this)
        db = FirebaseFirestore.getInstance()

        imgBack.setOnClickListener { finish() }
        btnRegister.setOnClickListener { doRegister() }
    }

    private fun doRegister() {
        val email: String = edtEmail.text.toString().trim().toLowerCase()
        val password: String = edtPassword.text.toString().trim()
        val fullName: String = edtFullName.text.toString().trim()

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.toast_there_are_empty_fields, Toast.LENGTH_SHORT).show()
        } else if (!ValidationUtil.validateEmail(email)) {
            Toast.makeText(this, R.string.toast_email_is_invalid, Toast.LENGTH_SHORT).show()
        } else if (!ValidationUtil.validatePassword(password)) {
            Toast.makeText(this, R.string.toast_password_is_invalid, Toast.LENGTH_SHORT).show()
        } else if (password.length < 6) {
            Toast.makeText(this, R.string.toast_password_less_than_6_characters, Toast.LENGTH_SHORT).show()
        } else {
            spotsDialog?.show()

            // nếu trùng email thì báo lỗi
            db?.collection(User.TABLE_USER)?.whereEqualTo(User.KEY_EMAIL, email)?.get()
                ?.addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.getResult()!!.isEmpty) {  // nếu là rỗng thì ko bị trùng email
                        addManager(fullName, email, password, HomeActivity.ROLE_MANAGER, true)
                    } else {    // trùng email
                        spotsDialog?.dismiss()
                        Snackbar.make(btnRegister, R.string.toast_email_is_duplicate, Snackbar.LENGTH_INDEFINITE).setAction(R.string.button_yes, {}).show()
                    }
                }
        }
    }

    private fun addManager(fullName: String, email: String, password: String, role: String, isActivated: Boolean) {
        val user = HashMap<String, Any>()
        val id = UUID.randomUUID().toString()
        user.put(User.KEY_ID, id)
        user.put(User.KEY_FULLNAME, fullName)
        user.put(User.KEY_EMAIL, email)
        user.put(User.KEY_PASSWORD, password)
        user.put(User.KEY_ROLE, role)
        user.put(User.KEY_STATUS, isActivated)

        db?.collection(User.TABLE_USER)?.document(id)?.set(user)
            ?.addOnSuccessListener {
                spotsDialog?.dismiss()
                finish()
            }?.addOnFailureListener { exception: Exception ->
                Snackbar.make(btnRegister, exception.message.toString(), Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.button_yes, {}).show()
            }
    }
}
