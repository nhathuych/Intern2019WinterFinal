package com.asiantech.intern2019winterfinal.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.asiantech.intern2019winterfinal.R
import com.asiantech.intern2019winterfinal.fragment.MenuFragment
import com.asiantech.intern2019winterfinal.model.Food
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_update_food.btnCancel
import kotlinx.android.synthetic.main.activity_update_food.btnConfirm
import kotlinx.android.synthetic.main.activity_update_food.edtNewFoodName
import kotlinx.android.synthetic.main.activity_update_food.edtNewPrice
import kotlinx.android.synthetic.main.activity_update_food.imgNewImage

class UpdateFoodActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_CODE = 1
        private const val PICK_IMAGE_TYPE = "image/*"
        private const val PARAMETER_TOKEN = "&token"
    }
    private var imageUrl: String? = ""
    private var spotsDialog: SpotsDialog? = null
    private var storageReference: StorageReference? = null
    private var db: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_food)

        spotsDialog = SpotsDialog(this)
        storageReference = FirebaseStorage.getInstance().getReference(System.currentTimeMillis().toString())
        db = FirebaseFirestore.getInstance()

        val food: Food = intent.getSerializableExtra(MenuFragment.KEY_CURRENT_FOOD) as Food
        Picasso.with(this).load(food.photo).into(imgNewImage)
        edtNewFoodName.setText(food.name)
        edtNewPrice.setText(food.price)

        imgNewImage.setOnClickListener { loadImage() }
        btnCancel.setOnClickListener { finish() }
        // nếu ko chọn hình mới thì lấy hình cũ
        btnConfirm.setOnClickListener {
            val newName = edtNewFoodName.text.toString().trim()
            val newPrice = edtNewPrice.text.toString().trim()
            if (newName.isEmpty()) {
                Toast.makeText(this, R.string.toast_field_name_is_invalid, Toast.LENGTH_SHORT).show()
            } else if (newPrice.isEmpty()) {
                Toast.makeText(this, R.string.toast_price_is_invalid, Toast.LENGTH_SHORT).show()
            } else {
                db?.collection(Food.TABLE_MENU)?.document(food.id.toString())
                    ?.update(
                        Food.KEY_IMAGE, if (imageUrl.equals("")) food.photo else imageUrl,
                        Food.KEY_NAME, newName,
                        Food.KEY_PRICE, newPrice
                    )
                    ?.addOnCompleteListener {
                        finish()
                        Toast.makeText(this, R.string.toast_updated, Toast.LENGTH_SHORT).show()
                    }
                    ?.addOnFailureListener { exception ->
                        Log.d("--- error", exception.message.toString())
                        Toast.makeText(this, R.string.toast_failed, Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadImage() {
        val intent = Intent()
        intent.type = PICK_IMAGE_TYPE
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
                    imageUrl = task.result!!.toString().substring(0, task.result!!.toString().indexOf(PARAMETER_TOKEN))
                    imgNewImage.setImageURI(data.data)
                    spotsDialog?.dismiss()
                    Log.d("--- link", imageUrl.toString())
                }
            }
        }
    }
}
