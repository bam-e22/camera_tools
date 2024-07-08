package com.example.common

import android.content.Context
import android.graphics.Rect
import android.graphics.YuvImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

fun Image.toYuvImage(): YuvImage {
    require(format == ImageFormat.YUV_420_888) { "Only need to YUV_420_888 format" }
    val yPlane = planes[0]
    val uPlane = planes[1]
    val vPlane = planes[2]

    val width: Int = width
    val height: Int = height

    // Order of U/V channel guaranteed, read more:
    // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888

    // Full size Y channel and quarter size U+V channels.
    val numPixels = (width * height * 1.5f).toInt()
    val nv21 = ByteArray(numPixels)
    var index = 0

    // Copy Y channel.
    for (y in 0 until height) {
        for (x in 0 until width) {
            nv21[index++] = yPlane.buffer.get(y * yPlane.rowStride + x * yPlane.pixelStride)
        }
    }

    // Copy VU data; NV21 format is expected to have YYYYVU packaging.
    // The U/V planes are guaranteed to have the same row stride and pixel stride.
    val uvWidth = width / 2
    val uvHeight = height / 2
    for (y in 0 until uvHeight) {
        for (x in 0 until uvWidth) {
            val bufferIndex = y * uPlane.rowStride + x * uPlane.pixelStride
            // V channel.
            nv21[index++] = vPlane.buffer.get(bufferIndex)
            // U channel.
            nv21[index++] = uPlane.buffer.get(bufferIndex)
        }
    }
    return YuvImage(
        nv21, ImageFormat.NV21, width, height,  /* strides= */null
    )
}

fun YuvImage.toFile(context: Context, fileName: String) {
    val image = ByteArrayOutputStream().use {
        compressToJpeg(Rect(0, 0, width, height), 100, it)
        it.toByteArray()
    }

    ByteArrayInputStream(image).toFile(context, fileName)
}

fun InputStream.toFile(context: Context, fileName: String) {
    val cacheDir = File(context.cacheDir, "facePay")
    if (!cacheDir.exists()) {
        cacheDir.mkdir()
    }

    val file = File(cacheDir, fileName)

    if (!file.exists()) {
        file.createNewFile()
    }

    file.outputStream().use { fos ->
        copyTo(fos)
    }
}
