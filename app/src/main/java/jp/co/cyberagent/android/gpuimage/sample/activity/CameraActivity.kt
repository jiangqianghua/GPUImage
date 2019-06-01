/*
 * Copyright (C) 2018 CyberAgent, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.co.cyberagent.android.gpuimage.sample.activity

import android.content.res.Resources
import android.media.FaceDetector
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.co.cyberagent.android.gpuimage.GPUImageView
import jp.co.cyberagent.android.gpuimage.filter.GPUImageAlphaBlendFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.sample.GPUImageFilterTools
import jp.co.cyberagent.android.gpuimage.sample.GPUImageFilterTools.FilterAdjuster
import jp.co.cyberagent.android.gpuimage.sample.R
import jp.co.cyberagent.android.gpuimage.sample.bean.ImageDataBean
import jp.co.cyberagent.android.gpuimage.sample.utils.*
import jp.co.cyberagent.android.gpuimage.util.Rotation
import kotlinx.android.synthetic.main.activity_home_main.view.*
import android.R.attr.y
import android.R.attr.x
import android.graphics.*


class CameraActivity : AppCompatActivity() {

    private val gpuImageAlphaBlendFilter = GPUImageAlphaBlendFilter() ;
    private val gpuImageView: GPUImageView by lazy { findViewById<GPUImageView>(R.id.surfaceView) }
    private val seekBar: SeekBar by lazy { findViewById<SeekBar>(R.id.seekBar) }
    private var faceDetectionAsyncTask: FaceDetectionAsyncTask ?= null
    private val bgImage: RelativeLayout by lazy {
        findViewById<RelativeLayout>(R.id.bgimage)
    }
    private val resouce1:Resources by lazy {
        this.resources
    }
    private val cameraLoader: CameraLoader by lazy {
        if (Build.VERSION.SDK_INT < 21) {
            Camera1Loader(this)
        } else {
            Camera2Loader(this)
        }
    }
    private var filterAdjuster: FilterAdjuster? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                filterAdjuster?.adjust(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        findViewById<View>(R.id.button_choose_filter).setOnClickListener {
            GPUImageFilterTools.showDialog(this) { filter -> switchFilterTo(filter) }
        }
        findViewById<View>(R.id.button_capture).setOnClickListener {
            saveSnapshot()
        }
        findViewById<View>(R.id.img_switch_camera).run {
            if (!cameraLoader.hasMultipleCamera()) {
                visibility = View.GONE
            }
            setOnClickListener {
//                gpuImageAlphaBlendFilter.bitmap = BitmapFactory.decodeResource(resouce1,R.mipmap.a)
//                gpuImageView.filter = gpuImageAlphaBlendFilter
                cameraLoader.switchCamera()
                gpuImageView.setRotation(getRotation(cameraLoader.getCameraOrientation()))
            }
        }
        val bitmap1 = BitmapFactory.decodeResource(resouce1,R.mipmap.d)
        cameraLoader.setOnPreviewFrameListener { bitmap, data, width, height ->
            gpuImageView.updatePreviewFrame(data, width, height)
            faceDetectionAsyncTask = FaceDetectionAsyncTask()
            faceDetectionAsyncTask?.execute(ImageDataBean(bitmap,data,width,height))
            faceDetectionAsyncTask?.setOnFaceDetectionAsyncTask(object: FaceDetectionAsyncTask.OnFaceDetectionAsyncTask{
                override fun onFaceDetected(faces: Array<out FaceDetector.Face>?) {
                    Log.d("TAG","---" + faces?.size)
                    val size = faces?.size
                    if(size == 0)
                        return
                    for (index in 0 until  1){
                        val face = faces!![index]
                        val r = RectF()
                        var pf = PointF()
                        if(face == null)
                            return
                        face.getMidPoint(pf)
//                        r.left = pf.x - face.eyesDistance() / 2
//                        r.right = pf.x + face.eyesDistance() / 2
//                        r.top = pf.y - face.eyesDistance() / 2
//                        r.bottom = pf.y + face.eyesDistance() / 2
                        gpuImageAlphaBlendFilter.bitmap = BitMapUtils.createNewBtiMap(this@CameraActivity,bitmap1,pf.x.toInt(),pf.y.toInt())
                        gpuImageAlphaBlendFilter.onOutputSizeChanged(1,1)
                        gpuImageAlphaBlendFilter.setMix(1.0f)
                        gpuImageView.filter = gpuImageAlphaBlendFilter
                    }
                    // gpuImageView.filter = null


                }
            })
        }
        //val newBitmap = BitMapUtils.scaleBitmap(BitmapFactory.decodeResource(resouce,R.mipmap.d), 20, 20)
//        val bitmap = BitmapFactory.decodeResource(resouce1,R.mipmap.d)
        // gpuImageAlphaBlendFilter.bitmap = BitMapUtils.createBitmap(bgImage)

//        gpuImageAlphaBlendFilter.bitmap = BitMapUtils.createNewBtiMap(this,bitmap,40,400)
//        gpuImageAlphaBlendFilter.onOutputSizeChanged(1,1)
//        gpuImageAlphaBlendFilter.setMix(1.0f)
//        gpuImageView.filter = gpuImageAlphaBlendFilter
        gpuImageView.setRotation(getRotation(cameraLoader.getCameraOrientation()))
        gpuImageView.setRenderMode(GPUImageView.RENDERMODE_CONTINUOUSLY)

        // gpuImageView.setImage(BitmapFactory.decodeResource(resouce1,R.mipmap.b))


        // faceDetectionAsyncTask.execute(bitmap)

    }

    override fun onResume() {
        super.onResume()
        gpuImageView.doOnLayout {
            cameraLoader.onResume(it.width, it.height)
        }
    }

    override fun onPause() {
        cameraLoader.onPause()
        super.onPause()
    }

    private fun saveSnapshot() {
        val folderName = "GPUImage"
        val fileName = System.currentTimeMillis().toString() + ".jpg"
        gpuImageView.saveToPictures(folderName, fileName) {
            Toast.makeText(this, "$folderName/$fileName saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRotation(orientation: Int): Rotation {
        return when (orientation) {
            90 -> Rotation.ROTATION_90
            180 -> Rotation.ROTATION_180
            270 -> Rotation.ROTATION_270
            else -> Rotation.NORMAL
        }
    }

    private fun switchFilterTo(filter: GPUImageFilter) {
        if (gpuImageView.filter == null || gpuImageView.filter!!.javaClass != filter.javaClass) {
            gpuImageView.filter = filter
            filterAdjuster = FilterAdjuster(filter)
            filterAdjuster?.adjust(seekBar.progress)
        }
    }
}
