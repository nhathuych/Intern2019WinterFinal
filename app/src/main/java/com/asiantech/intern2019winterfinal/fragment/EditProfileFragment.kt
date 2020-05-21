package com.asiantech.intern2019winterfinal.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.activity.HomeActivity
import com.asiantech.intern2019winterfinal.model.User
import com.asiantech.intern2019winterfinal.utils.MyUtil
import com.asiantech.intern2019winterfinal.utils.SharedPrefs
import com.asiantech.intern2019winterfinal.utils.ValidationUtil
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_edit_profile.*

class EditProfileFragment : Fragment() {
    private var db: FirebaseFirestore? = null
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        user = MyUtil.getSessionUser()
        edtEmail.setText(user?.email)
        edtFullName.setText(user?.fullName)
        edtPassword.setText(user?.password)

        btnConfirm.setOnClickListener {
            val fullName = edtFullName.text.toString()
            val password = edtPassword.text.toString()

            if (fullName.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity, R.string.toast_there_are_empty_fields, Toast.LENGTH_SHORT).show()
            } else if (!ValidationUtil.validatePassword(password)) {
                Toast.makeText(activity, R.string.toast_password_is_invalid, Toast.LENGTH_SHORT).show()
            } else if (password.length < 6) {
                Toast.makeText(activity, R.string.toast_password_less_than_6_characters, Toast.LENGTH_SHORT).show()
            } else {
                db?.collection(User.TABLE_USER)
                    ?.document(user?.id.toString())
                    ?.update(User.KEY_FULLNAME, fullName, User.KEY_PASSWORD, password)
                    ?.addOnCompleteListener {
                        db?.collection(User.TABLE_USER)?.document(user?.id.toString())
                            ?.get()
                            ?.addOnCompleteListener {task: Task<DocumentSnapshot> ->
                                val currentUser = User(user?.id, fullName, user?.email, password, user?.role, user?.isActivated)
                                SharedPrefs(activity as HomeActivity).putString(SharedPrefs.KEY_USER_LOGIN, Gson().toJson(currentUser))
                                MyUtil.setSessionUser(currentUser)
                            }
                        Toast.makeText(activity, R.string.toast_updated, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}
