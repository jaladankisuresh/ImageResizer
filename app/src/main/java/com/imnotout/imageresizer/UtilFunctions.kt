package com.imnotout.imageresizer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.imnotout.imageresizer.Models.MediaType
import kotlinx.coroutines.experimental.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.experimental.CoroutineContext
import kotlin.math.max

@Throws(IOException::class)
fun createImageFile(cntx: Context, image: Bitmap): Uri {
    val fileUri = createImageFile(cntx)
    image.compress(Bitmap.CompressFormat.JPEG, 70, FileOutputStream(fileUri.path, false))
    return fileUri
}
fun createImageFile(cntx: Context) = createMediaFile(cntx, MediaType.IMAGE)
fun createVideoFile(cntx: Context) = createMediaFile(cntx, MediaType.VIDEO)

@Throws(IOException::class)
fun createMediaFile(cntx: Context, type: MediaType): Uri {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
    val imageFileName = "${type.name}_${timeStamp}"
    val imageFile = when(type) {
        MediaType.VIDEO -> File.createTempFile(
                imageFileName, /* prefix */
                ".mp4", /* suffix */
                cntx.getExternalFilesDir(Environment.DIRECTORY_MOVIES)      /* directory */
        )
        else -> File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                cntx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)      /* directory */
        )
    }
    return Uri.fromFile(imageFile)
//    return FileProvider.getUriForFile(cntx, "com.imnotout.imageresizer.fileprovider", imageFile)
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
    val outWidth: Int
    val outHeight: Int
    if(aspectRatio > 0) {
        outWidth = maxPixels
        outHeight = (maxPixels / aspectRatio).toInt()
    }
    else {
        outWidth = (maxPixels * aspectRatio).toInt()
        outHeight = maxPixels
    }
    return Bitmap.createScaledBitmap(this, outWidth, outHeight, false)
}

