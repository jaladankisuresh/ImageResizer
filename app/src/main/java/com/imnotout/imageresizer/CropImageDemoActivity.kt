package com.imnotout.imageresizer

import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import com.theartofdev.edmodo.cropper.CropImage
import android.net.Uri
import android.support.v4.content.FileProvider
import android.util.Log
import com.imnotout.app.NetworkIO.BASE_WEB_API_URL
import com.imnotout.app.NetworkIO.HttpService
import com.imnotout.imageresizer.Models.MediaType
import com.imnotout.imageresizer.NetworkIO.loadImage
import kotlinx.android.synthetic.main.activity_crop_image_demo.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.startActivityForResult
import java.io.File


// Launch Camera App
// https://developer.android.com/training/camera/photobasics.html
// Pick from gallery
// https://stackoverflow.com/questions/2507898/how-to-pick-an-image-from-gallery-sd-card-for-my-app
// https://stackoverflow.com/questions/5309190/android-pick-images-from-gallery
const val PROFILE_IMAGE_SIZE: Int = 400 // Size in Pixels
class CropImageDemoActivity : AppCompatActivity() {
    lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop_image_demo)
//        To allow the user to select both images/videos from gallery, check the below link
//        https//stackoverflow.com/questions/4922037/android-let-user-pick-image-or-video-from-gallery
        btn_pick_image.setOnClickListener {
//            val getIntent = Intent(Intent.ACTION_GET_CONTENT)
//            getIntent.type = "image/*"
//            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            pickIntent.type = "image/*"
//            val chooserIntent = Intent.createChooser(getIntent, "Select Image")
//            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

            val getImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageIntent.type = "image/*"
            getImageIntent.resolveActivity(getPackageManager())?.let {
                startActivityForResult(getImageIntent, 1);
            }
        }
        btn_capture_image.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.resolveActivity(packageManager)?.let {
                imageUri = createImageFile(this)
                val imageContentUri = FileProvider.getUriForFile(this,"com.imnotout.imageresizer.fileprovider",
                        File(imageUri.path))
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageContentUri)
                startActivityForResult(takePictureIntent, 2)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                1 -> {
                    val imageUri = returnIntent!!.data
                    startActivityForResult<ImageCropperActivity>(3, "imageUri" to imageUri)
                }
                2 -> startActivityForResult<ImageCropperActivity>(3, "imageUri" to imageUri)
                3 -> {
                    val croppedImageUri = returnIntent!!.data
                    val croppedBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, croppedImageUri)
//                    we are cropping the image with aspect ratio 1:1, so width = height
                    val outSize = if(croppedBitmap.height > PROFILE_IMAGE_SIZE) PROFILE_IMAGE_SIZE else croppedBitmap.height
                    val downscaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, outSize, outSize, false)
                    val downscaledImageUri = createImageFile(this, downscaledBitmap)

                    val postUrl = "${BASE_WEB_API_URL}api/imageupload"
                    loadImage(img_result, downscaledImageUri)
                    val inStream = contentResolver.openInputStream(downscaledImageUri)
                    asyncWithException { HttpService.post(postUrl, MediaType.IMAGE, inStream) }
                }
            }
        }
        else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            val error = returnIntent?.getSerializableExtra("crop_error")
        }
    }
}
