package jp.co.cyberagent.android.gpuimage.sample.bean;

import android.graphics.Bitmap;
import android.media.Image;

public class ImageDataBean {
    public byte[] data;
    public int width;
    public int height;
    public Image image;
    public Bitmap bitmap;

    public ImageDataBean(Bitmap bitmap ,byte[] data, int width, int height) {
        this.bitmap = bitmap ;
        this.data = data;
        this.width = width;
        this.height = height;
    }
}
