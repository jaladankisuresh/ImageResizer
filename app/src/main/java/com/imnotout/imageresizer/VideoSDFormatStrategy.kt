package com.imnotout.imageresizer

import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import net.ypresto.androidtranscoder.format.MediaFormatExtraConstants
import net.ypresto.androidtranscoder.format.MediaFormatStrategy
import net.ypresto.androidtranscoder.format.OutputFormatUnavailableException
import kotlin.math.max

//@JvmOverloads constructor(private val mVideoBitrate: Int = VideoSDFormatStrategy.DEFAULT_VIDEO_BITRATE, private val mAudioBitrate: Int = VideoSDFormatStrategy.AUDIO_BITRATE_AS_IS, private val mAudioChannels: Int = VideoSDFormatStrategy.AUDIO_CHANNELS_AS_IS)

class VideoSDFormatStrategy(val videoBitrate: Int = DEFAULT_VIDEO_BITRATE) : MediaFormatStrategy {
    companion object {
//        const val AUDIO_BITRATE_AS_IS = -1
//        const val AUDIO_CHANNELS_AS_IS = -1
        const val MAX_PIXELS = 640
        const val DEFAULT_VIDEO_BITRATE = 1000 * 1000
    }
    override fun createVideoOutputFormat(inputFormat: MediaFormat): MediaFormat {
        val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH)
        val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT)

        if(width < MAX_PIXELS) return createVideoFormat(width, height)

        val aspectRatio: Float = width.toFloat() / height
        val outWidth: Int = MAX_PIXELS
        val outHeight: Int = (MAX_PIXELS / aspectRatio).toInt()
        return createVideoFormat(outWidth, outHeight)
    }

//    override fun createAudioOutputFormat(inputFormat: MediaFormat): MediaFormat? {
//        if (mAudioBitrate == AUDIO_BITRATE_AS_IS || mAudioChannels == AUDIO_CHANNELS_AS_IS) return null
//
//        // Use original sample rate, as resampling is not supported yet.
//        val format = MediaFormat.createAudioFormat(MediaFormatExtraConstants.MIMETYPE_AUDIO_AAC,
//                inputFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE), mAudioChannels)
//        format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
//        format.setInteger(MediaFormat.KEY_BIT_RATE, mAudioBitrate)
//        return format
//    }
    override fun createAudioOutputFormat(inputFormat: MediaFormat): MediaFormat? = null

    private fun createVideoFormat(width: Int, height: Int) =
            MediaFormat.createVideoFormat("video/avc", width, height).apply {
                setInteger(MediaFormat.KEY_BIT_RATE, videoBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            }
}
