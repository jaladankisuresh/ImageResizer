package com.imnotout.imageresizer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import com.imnotout.app.NetworkIO.BASE_WEB_API_URL
import com.imnotout.app.NetworkIO.HttpService
import kotlinx.android.synthetic.main.activity_create_gallery_demo.*

const val MEDIA_IMAGE_SIZE: Int = 800 // Max Size in Pixels for width/height which so ever is greater
class CreateGalleryDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_gallery_demo)

        btn_pick_gallery_images.setOnClickListener{
            val getImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageIntent.type = "image/*"
            getImageIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            getImageIntent.resolveActivity(getPackageManager())?.let {
                startActivityForResult(getImageIntent, 1);
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        if (resultCode == Activity.RESULT_OK) {
            val postUrl = "${BASE_WEB_API_URL}api/imageupload"

            if(requestCode == 1) {
                val imageUris = returnIntent!!.clipData
                for(index in 0..imageUris.itemCount - 1) {
                    val item = imageUris.getItemAt(index)
                    val imageUri = item.uri
                    val imageInStream = contentResolver.openInputStream(imageUri)
                    val imageBitmap = BitmapFactory.decodeStream(imageInStream)
                    val downscaledBitmap = imageBitmap.downscaleBitmap(MEDIA_IMAGE_SIZE)
                    val downscaledImageUri = createImageFile(this, downscaledBitmap)
                    val inStream = contentResolver.openInputStream(downscaledImageUri)

                    asyncWithException { HttpService.post(postUrl, inStream) }
                }
            }
        }
    }
}
