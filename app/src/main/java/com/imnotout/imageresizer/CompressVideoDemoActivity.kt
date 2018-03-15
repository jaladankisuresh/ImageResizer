package com.imnotout.imageresizer

import android.app.Activity
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.RecoverySystem
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import kotlinx.android.synthetic.main.activity_compress_video_demo.*
import android.util.Log
import com.imnotout.app.NetworkIO.BASE_WEB_API_URL
import com.imnotout.app.NetworkIO.HttpService
import com.imnotout.imageresizer.Models.MediaType
import net.ypresto.androidtranscoder.MediaTranscoder
import org.jetbrains.anko.alert
import org.jetbrains.anko.appcompat.v7.Appcompat
import org.jetbrains.anko.yesButton
import java.io.File
import java.lang.Exception
import kotlin.math.min


class CompressVideoDemoActivity : AppCompatActivity() {
    lateinit var videoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compress_video_demo)

        btn_pick_video.setOnClickListener {
            val getImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImageIntent.type = "video/*"
            getImageIntent.resolveActivity(getPackageManager())?.let {
                startActivityForResult(getImageIntent, 1);
            }
        }
        btn_capture_video.setOnClickListener {
            val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            takeVideoIntent.resolveActivity(packageManager)?.let {
                videoUri = createVideoFile(this)
                val videoContentUri = FileProvider.getUriForFile(this,"com.imnotout.imageresizer.fileprovider",
                        File(videoUri.path))
                takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoContentUri)
                takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60)
                takeVideoIntent.putExtra(MediaStore.EXTRA_FULL_SCREEN, true)
                takeVideoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                startActivityForResult(takeVideoIntent, 2)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        when(resultCode) {
            Activity.RESULT_OK -> {
                val inVideoUri = if(requestCode == 1) returnIntent!!.data else videoUri
                video_downsized.setVideoURI(inVideoUri)
                video_downsized.start()

                MediaMetadataRetriever().run {
                    val cntx = this@CompressVideoDemoActivity
                    setDataSource(cntx, inVideoUri)
                    val durationMilliSec = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toLong()
                    val videoDurationInSec = durationMilliSec / 1000
                    if(videoDurationInSec > 60) {
                        alert(Appcompat,"Upload Video cannot exceed beyond 1 Min") {
                            yesButton { }
                        }.show()

                        return
                    }
                    val inVideoWidth = extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH).toInt()
                    val inVideoBitrate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE).toInt()

                    if(inVideoWidth <= VideoSDFormatStrategy.MAX_PIXELS &&
                            inVideoBitrate <= VideoSDFormatStrategy.DEFAULT_VIDEO_BITRATE) {
//                        Video size is less than the max video upload size
                        uploadVideo(inVideoUri)
                    }
                    else {
                        val downSizedVideoUri = createVideoFile(cntx)
                        val transcodeListener = object : MediaTranscoder.Listener {
                            override fun onTranscodeCanceled() {
                                Log.i(LOG_APP_TAG, "Video Transcoding Canceled: ${System.currentTimeMillis()}")
                            }
                            override fun onTranscodeCompleted() {
                                Log.i(LOG_APP_TAG, "Video Transcoding Complete: ${System.currentTimeMillis()}")
                                uploadVideo(downSizedVideoUri)
                            }
                            override fun onTranscodeProgress(progress: Double) {
//                            Log.i(LOG_APP_TAG, "Video Transcoding with ${progress} Complete")
                            }
                            override fun onTranscodeFailed(ex: Exception) = ex.printStackTrace()
                        }
                        Log.i(LOG_APP_TAG, "Video Transcoding Start: ${System.currentTimeMillis()}")
                        downscaleVideo(inVideoUri, downSizedVideoUri, transcodeListener)
                    }
                }
            }
            Activity.RESULT_CANCELED -> Log.i(LOG_APP_TAG, "Video Intent canceled by the user")
            else -> Log.e(LOG_APP_TAG, "Error while picking video from gallery")
        }
    }

    private fun uploadVideo(videoUri: Uri) {
        val postUrl = "${BASE_WEB_API_URL}api/imageupload"
        val inStream = contentResolver.openInputStream(videoUri)
        asyncWithException { HttpService.post(postUrl, MediaType.VIDEO, inStream) }
    }

    private fun downscaleVideo(inVideoUri: Uri, outVideoUri: Uri, transcodeListener: MediaTranscoder.Listener) {
        val parcelFileDescriptor = contentResolver.openFileDescriptor(inVideoUri, "r")
        val fileDescriptor = parcelFileDescriptor.fileDescriptor
        MediaMetadataRetriever().run {
            val cntx = this@CompressVideoDemoActivity
            setDataSource(cntx, inVideoUri)
            val inVideoBitrate = extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE).toInt()
            val outVideoBitrate = min(inVideoBitrate, VideoSDFormatStrategy.DEFAULT_VIDEO_BITRATE)

//            MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, outVideoUri.path,
//                    MediaFormatStrategyPresets.createAndroid720pStrategy(), transcodeListener)
            MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, outVideoUri.path,
                    VideoSDFormatStrategy(outVideoBitrate), transcodeListener)
        }
    }
}
