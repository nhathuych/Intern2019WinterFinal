package com.asiantech.intern2019winterfinal.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.ValidationUtil
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_register.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import dmax.dialog.SpotsDialog
import java.lang.Exception
import java.util.*

class RegisterActivity : AppCompatActivity() {
    companion object {
        const val EMAIL = "email"
        const val PASSWORD = "password"
    }

    private var spotsDialog: SpotsDialog? = null
    private var db: FirebaseFirestore? = null

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // làm trong suốt statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        spotsDialog = SpotsDialog(this)
        db = FirebaseFirestore.getInstance()

        btnRegister.setOnClickListener { doRegister() }
        tvLogin.setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
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

            // nếu người đầu tiên đăng ký tài khoản thì người đó có quyền Admin
            db?.collection(User.TABLE_USER)?.get()
                ?.addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.getResult()!!.isEmpty) {   // nếu là rỗng thì là người đầu tiên
                        addUser(fullName, email, password, HomeActivity.ROLE_ADMIN, true)
                    } else {    // nếu ko rỗng thì là user bình thường
                        // nếu trùng email thì báo lỗi
                        db?.collection(User.TABLE_USER)?.whereEqualTo(User.KEY_EMAIL, email)?.get()
                            ?.addOnCompleteListener { task2: Task<QuerySnapshot> ->
                                if (task2.getResult()!!.isEmpty) {  // nếu là rỗng thì ko bị trùng email
                                    addUser(fullName, email, password, HomeActivity.ROLE_USER, true)
                                } else {    // trùng email
                                    spotsDialog?.dismiss()
                                    Snackbar.make(btnRegister, R.string.toast_email_is_duplicate, Snackbar.LENGTH_INDEFINITE).setAction(R.string.button_yes, {}).show()
                                }
                        }
                    }
                }
        }
    }

    private fun addUser(fullName: String, email: String, password: String, role: String, isActivated: Boolean) {
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
                Snackbar.make(btnRegister, R.string.toast_back_to_login, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.button_yes) {
                        val intentLogin = Intent(this@RegisterActivity, LoginActivity::class.java)
                        intentLogin.putExtra(EMAIL, email)
                        intentLogin.putExtra(PASSWORD, password)
                        startActivity(intentLogin)
                    }
                    .show()
            }
            ?.addOnFailureListener {exception: Exception ->
                Snackbar.make(btnRegister, exception.message.toString(), Snackbar.LENGTH_INDEFINITE).setAction(R.string.button_yes, {}).show()
            }
    }
}
