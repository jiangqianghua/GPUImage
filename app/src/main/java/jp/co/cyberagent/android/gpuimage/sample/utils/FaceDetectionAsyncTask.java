package jp.co.cyberagent.android.gpuimage.sample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.FaceDetector;
import android.os.AsyncTask;
import jp.co.cyberagent.android.gpuimage.sample.bean.ImageDataBean;
//import android.hardware.Camera2;

public class FaceDetectionAsyncTask extends AsyncTask<ImageDataBean,Void,FaceDetector.Face[]> {

    public interface OnFaceDetectionAsyncTask {
        void onFaceDetected(FaceDetector.Face[] faces);
    }

    private OnFaceDetectionAsyncTask onFaceDetectionAsyncTask ;

    public void setOnFaceDetectionAsyncTask(OnFaceDetectionAsyncTask onFaceDetectionAsyncTask) {
        this.onFaceDetectionAsyncTask = onFaceDetectionAsyncTask;
    }

    @Override
    protected FaceDetector.Face[] doInBackground(ImageDataBean... imageDataBeans) {
        Bitmap bitmap = BitMapUtils.getOriginalBitmap(imageDataBeans[0].bitmap, imageDataBeans[0].data,imageDataBeans[0].width,imageDataBeans[0].height);
        if(bitmap == null)
            return null;
        FaceDetector faceDet = new FaceDetector(bitmap.getWidth(), bitmap.getHeight(), 1);
        FaceDetector.Face[] faceList = new FaceDetector.Face[1];
        faceDet.findFaces(bitmap, faceList);
        bitmap.recycle();
        return faceList;
    }

    @Override
    protected void onPostExecute(FaceDetector.Face[] faces) {
        // super.onPostExecute(faces);
        if(onFaceDetectionAsyncTask != null){
            onFaceDetectionAsyncTask.onFaceDetected(faces);
        }
    }
}
