package com.asiantech.intern2019winterfinal.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_add_new_food.*
import android.content.Intent
import android.util.Log
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.fragment.MenuFragment
import com.asiantech.intern2019winterfinal.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dmax.dialog.SpotsDialog

class AddNewFoodActivity : AppCompatActivity() {
    companion object {
        private val PICK_IMAGE_CODE: Int = 1
    }
    private var imageUrl: String? = null
    private var spotsDialog: SpotsDialog? = null
    private var storageReference: StorageReference? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_food)

        spotsDialog = SpotsDialog(this)
        storageReference = FirebaseStorage.getInstance().getReference(System.currentTimeMillis().toString())
        db = FirebaseFirestore.getInstance()

        imgNewImage.setOnClickListener { loadImage() }
        btnConfirm.setOnClickListener { addNewFood() }
        btnCancel.setOnClickListener { finish() }
    }

    private fun addNewFood() {
        val name = edtNewFoodName.text.toString().trim()
        val price = edtNewPrice.text.toString().trim()
        if (imageUrl == null) {
            Toast.makeText(this, R.string.toast_there_is_no_image, Toast.LENGTH_SHORT).show()
        } else if (name.isEmpty()) {
            Toast.makeText(this, R.string.toast_field_name_is_invalid, Toast.LENGTH_SHORT).show()
        } else if (price.isEmpty()) {
            Toast.makeText(this, R.string.toast_price_is_invalid, Toast.LENGTH_SHORT).show()
        } else {
            val id: Int = intent.getIntExtra(MenuFragment.KEY_SIZE, -1) + 1
            if (id != -1) {
                val food = HashMap<String, Any>()
                food.put(Food.KEY_ID, id)
                food.put(Food.KEY_NAME, name)
                food.put(Food.KEY_PRICE, price)
                food.put(Food.KEY_IMAGE, imageUrl.toString())

                db?.collection(Food.TABLE_MENU)?.document(id.toString())?.set(food)
                    ?.addOnSuccessListener {
                        Toast.makeText(this, R.string.toast_added, Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
        }
    }

    private fun loadImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.dialog_select_picture)), PICK_IMAGE_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_CODE && data != null) {
            spotsDialog?.show()
            val uploadTask = data.data?.let { storageReference?.putFile(it) }
            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    spotsDialog?.dismiss()
                    Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show()
                    Log.d("--- error", task.exception?.message.toString())
                }
                storageReference?.getDownloadUrl()
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    imageUrl = task.result!!.toString().substring(0, task.result!!.toString().indexOf("&token"))    // xóa chuỗi token
                    imgNewImage.setImageURI(data.data)
                    spotsDialog?.dismiss()
                    Log.d("--- link", imageUrl.toString())
                }
            }
        }
    }
}
