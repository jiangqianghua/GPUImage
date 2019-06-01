package jp.co.cyberagent.android.gpuimage.sample.utils

import android.graphics.Bitmap
import android.media.Image


abstract class CameraLoader {

    protected var onPreviewFrame: ((bitmap:Bitmap, data: ByteArray, width: Int, height: Int) -> Unit)? = null

    abstract fun onResume(width: Int, height: Int)

    abstract fun onPause()

    abstract fun switchCamera()

    abstract fun getCameraOrientation(): Int

    abstract fun hasMultipleCamera(): Boolean

    fun setOnPreviewFrameListener(onPreviewFrame: (bitmap:Bitmap, data: ByteArray, width: Int, height: Int) -> Unit) {
        this.onPreviewFrame = onPreviewFrame
    }
}