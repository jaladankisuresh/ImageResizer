package com.imnotout.imageresizer.NetworkIO

import okio.Okio
import okio.BufferedSink
import android.support.annotation.Nullable
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.IOException
import java.io.InputStream


// https://github.com/square/okhttp/issues/3585
class StreamRequestBody(private val contentType: MediaType?,
                             private val inputStream: InputStream): RequestBody() {
    @Nullable
    override fun contentType(): MediaType? =  contentType

    @Throws(IOException::class)
    override fun contentLength() = if (inputStream.available() == 0) -1 else inputStream.available().toLong()

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        Okio.source(inputStream).use {
            sink.writeAll(it)
        }
    }
}