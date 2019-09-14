package ma.c2m.scannerc2m.image;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;

public abstract class ImageProcessing {
    public static final int A = 0;
    public static final int R = 1;
    public static final int G = 2;
    public static final int B = 3;
    public static final int H = 0;
    public static final int S = 1;
    public static final int L = 2;

    private ImageProcessing() {
    }

    public static float[] getARGB(int pixel) {
        int a = (pixel >> 24) & 0xff;
        int r = (pixel >> 16) & 0xff;
        int g = (pixel >> 8) & 0xff;
        int b = (pixel) & 0xff;
        return (new float[]{a, r, g, b});
    }

    public static int[] convertToHSL(int r, int g, int b) {
        float red = r / 255;
        float green = g / 255;
        float blue = b / 255;
        float minComponent = Math.min(red, Math.min(green, blue));
        float maxComponent = Math.max(red, Math.max(green, blue));
        float range = maxComponent - minComponent;
        float h = 0, s = 0, l = 0;
        l = (maxComponent + minComponent) / 2;
        if (range == 0) {
            h = s = 0;
        } else {
            s = (l > 0.5) ? range / (2 - range) : range / (maxComponent + minComponent);
            if (red == maxComponent) {
                h = (blue - green) / range;
            } else if (green == maxComponent) {
                h = 2 + (blue - red) / range;
            } else if (blue == maxComponent) {
                h = 4 + (red - green) / range;
            }
        }
        h *= 60;
        if (h < 0) h += 360;
        s *= 100;
        l *= 100;
        return (new int[]{(int) h, (int) s, (int) l});
    }

    public static int[] decodeYUV420SPtoLuma(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) throw new NullPointerException();
        final int frameSize = width * height;
        int[] hsl = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & (yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                hsl[yp] = y;
            }
        }
        return hsl;
    }

    public static int[] decodeYUV420SPtoRGB(byte[] yuv420sp, int width, int height) {
        if (yuv420sp == null) throw new NullPointerException();
        final int frameSize = width * height;
        int[] rgb = new int[frameSize];
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & (yuv420sp[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);
                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    public static Bitmap rgbToBitmap(int[] rgb, int width, int height) {
        if (rgb == null) throw new NullPointerException();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(rgb, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static Bitmap lumaToGreyscale(int[] lum, int width, int height) {
        if (lum == null) throw new NullPointerException();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int y = 0, xy = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++, xy++) {
                int luma = lum[xy];
                bitmap.setPixel(x, y, Color.argb(1, luma, luma, luma));
            }
        }
        return bitmap;
    }

    public static Bitmap rotate(Bitmap bmp, int degrees) {
        if (bmp == null) throw new NullPointerException();
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap rotatedBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return rotatedBmp;
    }

    public static byte[] rotate(byte[] data, int degrees) {
        if (data == null) throw new NullPointerException();
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Bitmap rotatedBmp = rotate(bmp, degrees);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        rotatedBmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}