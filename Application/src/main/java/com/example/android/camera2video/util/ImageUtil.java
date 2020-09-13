package com.example.android.camera2video.util;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.Image;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class ImageUtil {
    private static final String TAG = "ImageUtil";

    public static byte[] imageToByteArray(Image image) {
        byte[] data = null;
        if (image.getFormat() == ImageFormat.JPEG) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            data = new byte[buffer.capacity()];
            buffer.get(data);
            return data;
        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
            data = NV21toJPEG(
                    YUV_420_888toNV21(image),
                    image.getWidth(), image.getHeight());
        }
        return data;
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
//        nv21 = rotateNV21(nv21, width, height, 270);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

       // int[] rgb = yuv2rgb(yuv.getYuvData(), width, height);

        //int index = rgb.length / 2;

       // Log.d(TAG, "NV21toJPEG: " + rgb[rgb.length / 2]);

        //int red = (rgb[index] >> 16) & 0x000000FF;
        //int green = (rgb[index] >> 8) & 0x000000FF;
        //int blue = (rgb[index]) & 0x000000FF;

        //int color = Color.rgb(red, green, blue);


        //Log.d(TAG, "NV21toJPEG: volo " + color);

        byte[] imageBytes = out.toByteArray();
//        Bitmap image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
//        storeImage(image);
        return out.toByteArray();
    }

    public static int[] yuv2rgb(byte[] yuv, int width, int height) {
        int total = width * height;
        int[] rgb = new int[total];
        int Y, Cb = 0, Cr = 0, index = 0;
        int R, G, B;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Y = yuv[y * width + x];
                if (Y < 0) Y += 255;

                if ((x & 1) == 0) {
                    Cr = yuv[(y >> 1) * (width) + x + total];
                    Cb = yuv[(y >> 1) * (width) + x + total + 1];

                    if (Cb < 0) Cb += 127;
                    else Cb -= 128;
                    if (Cr < 0) Cr += 127;
                    else Cr -= 128;
                }

                R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
                G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
                B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);

                // Approximation
//				R = (int) (Y + 1.40200 * Cr);
//			    G = (int) (Y - 0.34414 * Cb - 0.71414 * Cr);
//				B = (int) (Y + 1.77200 * Cb);

                if (R < 0) R = 0;
                else if (R > 255) R = 255;
                if (G < 0) G = 0;
                else if (G > 255) G = 255;
                if (B < 0) B = 0;
                else if (B > 255) B = 255;


                rgb[index++] = 0xff000000 + (R << 16) + (G << 8) + B;
            }
        }

        return rgb;
    }

    public static int[] getRgbData(Image image) {
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            byte[] nv21 = YUV_420_888toNV21(image);
            return yuv2rgb(nv21, image.getWidth(), image.getHeight());
        }

        return null;
    }

    public static byte[] saveImage(Image image){
        if (image.getFormat() == ImageFormat.YUV_420_888) {
            byte[] nv21 = YUV_420_888toNV21(image);
            byte[] Jpeg = NV21toJPEG(nv21, image.getWidth(), image.getHeight());

            return Jpeg;
        }

        return null;
    }



}