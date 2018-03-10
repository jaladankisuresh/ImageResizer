package com.imnotout.imageresizer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.support.v4.content.FileProvider
import kotlinx.coroutines.experimental.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.max

@Throws(IOException::class)
fun createImageFile(cntx: Context): Uri {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "JPEG_${timeStamp}_"
    val imageFile = File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            cntx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)      /* directory */
    )
    return Uri.fromFile(imageFile)
//    return FileProvider.getUriForFile(cntx, "com.imnotout.imageresizer.fileprovider", imageFile)
}

@Throws(IOException::class)
fun createImageFile(cntx: Context, image: Bitmap): Uri {
    val fileUri = createImageFile(cntx)
    image.compress(Bitmap.CompressFormat.JPEG, 70, FileOutputStream(fileUri.path, false))
    return fileUri
}

inline fun <T> asyncWithException (
    context: CoroutineContext = DefaultDispatcher,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    parent: Job? = null,
    noinline block: suspend CoroutineScope.() -> T) {

    async(context, start, parent, block)
        .invokeOnCompletion{
            it?.run {
                printStackTrace()
            }
        }
}

fun Bitmap.downscaleBitmap(maxPixels: Int): Bitmap {
    val dimension = max(width, height)
    if(dimension < maxPixels) return this

    val aspectRatio: Float = width.toFloat() / height
    val outWidth: Int = if(aspectRatio > 0) maxPixels else (maxPixels * aspectRatio).toInt()
    val outHeight: Int = if(aspectRatio > 0) (maxPixels / aspectRatio).toInt() else maxPixels
    return Bitmap.createScaledBitmap(this, outWidth, outHeight, false)
}

