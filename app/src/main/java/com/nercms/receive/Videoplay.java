package com.nercms.receive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.nio.ByteBuffer;

public class Videoplay extends View {
    public static int width = 352;
    public static int height = 288;
    public static byte[] mPixel = new byte[width * height * 3];
    public ByteBuffer buffer = ByteBuffer.wrap(mPixel);
    public Bitmap VideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);
    private Matrix matrix = null;
    public Bitmap VideoBit2 = Bitmap.createBitmap(width, height, Config.RGB_565);

    private RectF rectF;

    private int cameraPosition = 1;

    private boolean isPhotoing = false;

    private Paint photoPaint;
    private Bitmap photoBitMap;

    private int view_width;
    private int view_height;

//    private GPUImage gpuImage;
//    private static MagicFilterType filterType = MagicFilterType.NONE;

    public Videoplay(Context context, AttributeSet attrs) {
        super(context, attrs);
        matrix = new Matrix();
        //   DisplayMetrics dm = getResources().getDisplayMetrics();
        //   int W = dm.widthPixels;
        //  int H = dm.heightPixels;


        photoPaint = new Paint();
        photoPaint.setColor(Color.WHITE);
        photoPaint.setStyle(Paint.Style.STROKE);   //空心
        photoPaint.setStrokeWidth(35);

//        gpuImage = new GPUImage(MyApplication.getInstance());
    }

   /* @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制截图白色边框
        if (isPhotoing) {
            Log.d("xxxxx", view_width + " " + view_height + " " + photoPaint.getStrokeWidth());

            if (VideoBit2 != null)
                canvas.drawBitmap(VideoBit2, null, rectF, null);

            canvas.drawRect(0, 0, view_width, view_height, photoPaint);
            return;
        }

        buffer.rewind();

        VideoBit.copyPixelsFromBuffer(buffer);
        gpuImage = new GPUImage(MyApplication.getInstance());
        gpuImage.setImage(VideoBit);
        gpuImage.setFilter(ImageFilterFactory.getInstance().getFilter(filterType));
        VideoBit = gpuImage.getBitmapWithFilterApplied();
        setAngle();
        gpuImage.deleteImage();
        VideoBit = Bitmap.createBitmap(width, height, Config.RGB_565);

        synchronized (VideoBit2) {
            canvas.drawBitmap(VideoBit2, null, rectF, null);
        }


    }*/


    public byte[] jpegData;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制截图白色边框
        if (isPhotoing) {
            Log.d("xxxxx", view_width + " " + view_height + " " + photoPaint.getStrokeWidth());

            if (VideoBit2 != null)
                canvas.drawBitmap(VideoBit2, null, rectF, null);

            canvas.drawRect(0, 0, view_width, view_height, photoPaint);
            return;
        }
        buffer.rewind();

        if (jpegData != null) {

            VideoBit = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
            //Log.e("yyyy","count:"+VideoBit.getByteCount());
            setAngle();
            canvas.drawBitmap(VideoBit2, null, rectF, null);


        }

/*       VideoBit.copyPixelsFromBuffer(buffer);


        //canvas.drawBitmap(adjustPhotoRotation(VideoBit,90), 0, 0, null);
        //
        //Bitmap b = BitmapFactory.decodeByteArray(mPixel, 0, mPixel.length);
        synchronized (VideoBit2) {
            canvas.drawBitmap(VideoBit2, null, rectF, null);
            //canvas.drawText("sdfasfasdfaasdf",0,0,photoPaint);
        }*/


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("VideoChatActivity", w + "  " + h);
        rectF = new RectF(0, 0, w, h);
        view_width = w;
        view_height = h;
    }

    //  设置旋转比例
    private void setAngle() {
        matrix.reset();
        if (cameraPosition == 1)
            matrix.setRotate(-90);
        else
            matrix.setRotate(90);

        synchronized (VideoBit2) {
            VideoBit2 = Bitmap.createBitmap(VideoBit, 0, 0, VideoBit.getWidth(), VideoBit.getHeight(), matrix, true);
        }
    }

    private Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {

        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        float targetX, targetY;
        if (orientationDegree == 90) {
            targetX = bm.getHeight();
            targetY = 0;
        } else {
            targetX = bm.getHeight();
            targetY = bm.getWidth();
        }

        final float[] values = new float[9];
        m.getValues(values);

        float x1 = values[Matrix.MTRANS_X];
        float y1 = values[Matrix.MTRANS_Y];

        m.postTranslate(targetX - x1, targetY - y1);

        Bitmap bm1 = Bitmap.createBitmap(bm.getHeight(), bm.getWidth(), Config.ARGB_8888);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(bm1);
        canvas.drawBitmap(bm, m, paint);

        return bm1;
    }

    public void setCameraPosition(int position) {
        this.cameraPosition = position;
    }

    public void startCamera() {
        isPhotoing = true;
    }

    public void stopCamera() {
        isPhotoing = false;
    }

//    public static void setFilterType(MagicFilterType type) {
//        filterType = type;
//    }

    public static void close() {
//        filterType = MagicFilterType.NONE;
        mPixel = new byte[width * height * 2];
    }
}
