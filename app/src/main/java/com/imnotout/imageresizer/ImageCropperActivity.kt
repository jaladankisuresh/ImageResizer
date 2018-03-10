package com.imnotout.imageresizer

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.theartofdev.edmodo.cropper.CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE
import kotlinx.android.synthetic.main.activity_image_cropper.*
import com.theartofdev.edmodo.cropper.CropImageView


class ImageCropperActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_cropper)

        val imageUri = intent.getParcelableExtra<Uri>("imageUri")

//        loadImage(img_selected, imageUri.path)
        img_cropped.run {
            setImageUriAsync(imageUri)
            guidelines = CropImageView.Guidelines.ON
            setAspectRatio(1, 1)
            setOnCropImageCompleteListener{ view, result ->
                val returnIntent = Intent();
                if(result.isSuccessful) {
                    returnIntent.data = result.uri
                    setResult(RESULT_OK, returnIntent)
                }
                else {
                    returnIntent.putExtra("crop_error", result.error)
                    setResult(CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE, returnIntent)
                }
                finish()
            }
        }
        img_back.setOnClickListener{
            onBackPressed()
        }
        img_rotate.setOnClickListener{
            img_cropped.rotateImage(90)
        }
        lbl_save.setOnClickListener{
            val cropppedImageUri = createImageFile(this@ImageCropperActivity)
            img_cropped.saveCroppedImageAsync(cropppedImageUri)
        }
        lbl_cancel.setOnClickListener{
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }
}
