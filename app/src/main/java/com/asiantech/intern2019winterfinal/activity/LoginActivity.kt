package com.asiantech.intern2019winterfinal.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_login.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import dmax.dialog.SpotsDialog
import android.view.WindowManager
import android.os.Build

class LoginActivity : AppCompatActivity() {
    private var spotsDialog: SpotsDialog? = null
    private var db: FirebaseFirestore? = null
    private val sharedPrefs = SharedPrefs(this)

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // làm trong suốt statusBar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        val userJson = sharedPrefs.getString(SharedPrefs.KEY_USER_LOGIN)
        if (!userJson.equals("")) {
            val user = Gson().fromJson(userJson, User::class.java)
            MyUtil.setSessionUser(user)
            startActivity(Intent(this, HomeActivity::class.java))
        }

        initView()
        spotsDialog = SpotsDialog(this);
        db = FirebaseFirestore.getInstance()
    }

    private fun initView() {
        val email: String? = intent.getStringExtra(RegisterActivity.EMAIL)
        val password: String? = intent.getStringExtra(RegisterActivity.PASSWORD)
        if (email?.isEmpty() == false && password?.isEmpty() == false) {
            edtEmail.setText(email)
            edtPassword.setText(password)
        }

        btnLogin.setOnClickListener { doLogin() }
        tvCreateNewAccount.setOnClickListener { startActivity(Intent(this, RegisterActivity::class.java)) }
    }

    private fun doLogin() {
        val email: String = edtEmail.text.toString().trim()
        val password: String = edtPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.toast_there_are_empty_fields, Toast.LENGTH_SHORT).show()
        } else {
            spotsDialog?.show()
            val query = db?.collection(User.TABLE_USER)
            query?.whereEqualTo(User.KEY_EMAIL, email)
                ?.get()
                ?.addOnCompleteListener { task: Task<QuerySnapshot> ->
                    if (task.getResult()!!.isEmpty) {
                        spotsDialog?.dismiss()
                        Toast.makeText(this, R.string.toast_could_not_find_your_account, Toast.LENGTH_SHORT).show()
                    } else {
                        // nếu status == false -> ko cho login
                        for (doc: DocumentSnapshot in task.result!!) {
                            if (doc.getBoolean(User.KEY_STATUS) == false) {
                                spotsDialog?.dismiss()
                                Toast.makeText(this, R.string.toast_account_is_blocked, Toast.LENGTH_SHORT).show()
                                return@addOnCompleteListener
                            }
                        }

                        query.whereEqualTo(User.KEY_PASSWORD, password)
                            .get()
                            .addOnCompleteListener { task2: Task<QuerySnapshot> ->
                                if (task2.getResult()!!.isEmpty) {
                                    spotsDialog?.dismiss()
                                    Toast.makeText(this, R.string.toast_wrong_password, Toast.LENGTH_SHORT).show()
                                } else {
                                    spotsDialog?.dismiss()
                                    for (doc: DocumentSnapshot in task.result!!) {
                                        val user = User(
                                            doc.getString(User.KEY_ID),
                                            doc.getString(User.KEY_FULLNAME),
                                            doc.getString(User.KEY_EMAIL),
                                            doc.getString(User.KEY_PASSWORD),
                                            doc.getString(User.KEY_ROLE),
                                            doc.getBoolean(User.KEY_STATUS)
                                        )
                                        // convert đối tượng user thành json và lưu xuống SharedPrefs
                                        sharedPrefs.putString(SharedPrefs.KEY_USER_LOGIN, Gson().toJson(user))
                                        MyUtil.setSessionUser(user)
                                    }
                                    startActivity(Intent(this, HomeActivity::class.java))
                                }
                            }
                    }
                }
        }
    }
}
