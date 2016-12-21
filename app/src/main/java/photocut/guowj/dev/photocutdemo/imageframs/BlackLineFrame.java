package photocut.guowj.dev.photocutdemo.imageframs;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by guowj on 2016/10/20.
 */

public class BlackLineFrame extends BorderModle {

    private int borderWidth = 120;

    Paint bpaint = new Paint();

    private Bitmap bmpTop = null;
    private Bitmap bmpLeft = null;

    public BlackLineFrame(int width, int height) {

        bpaint.setColor(Color.GREEN);
        bpaint.setStrokeWidth(15);

//        bmpLeft = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.fram_black_left);
//        bmpTop = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.fram_black_top);

    }

    @Override
    public Bitmap getFramBitmap(int bmpWidth, int bmpHeight) {

        Bitmap framBitmap = Bitmap.createBitmap(bmpWidth + borderWidth * 2,
                bmpHeight + borderWidth * 2, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(framBitmap);
        Matrix matrix = new Matrix();
        Bitmap topBitmap = getTopBorderBitmap(bmpWidth);
        canvas.drawBitmap(topBitmap, matrix, bpaint);
        matrix.setTranslate(bmpTop.getHeight(), bmpHeight + bmpTop.getHeight());
        canvas.drawBitmap(topBitmap, matrix, bpaint);

        Bitmap leftBitmap = getLeftBorderBitmap(bmpHeight);
        //绘制左侧
        matrix.setTranslate(0, bmpTop.getHeight());
        canvas.drawBitmap(leftBitmap, matrix, bpaint);
        matrix.setTranslate(bmpWidth + bmpLeft.getWidth(), 0);
        canvas.drawBitmap(leftBitmap, matrix, bpaint);

        return framBitmap;
    }


    @Override
    public int getBitmapLeftPadding(int originLeft) {
        originLeft = originLeft - bmpLeft.getWidth();
        return originLeft;
    }

    @Override
    public int getBitmapTopPadding(int originTop) {
        originTop = originTop - bmpTop.getHeight();
        return originTop;
    }


    private Bitmap getTopBorderBitmap(int bmpWidth) {
        int number = getToDrawNum(bmpWidth, bmpTop.getWidth());
        float scaleRate = getScaleRate(number, bmpWidth);
        Bitmap topTmp = Bitmap.createBitmap(bmpTop.getWidth() * number, bmpTop.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas topCanvas = new Canvas(topTmp);
        Matrix matrix = new Matrix();
        for (int i = 0; i < number; i++) {
            matrix.setTranslate(bmpTop.getWidth() * i, 0);
            topCanvas.drawBitmap(bmpTop, matrix, bpaint);
        }
        matrix.postScale(scaleRate, 1);
        Bitmap topBitmap = Bitmap.createBitmap(topTmp, 0, 0, topTmp.getWidth(),
                topTmp.getHeight(), matrix, true);
        topTmp.recycle();
        return topBitmap;
    }


    private Bitmap getLeftBorderBitmap(int bmpHeight) {
        int number = getToDrawNum(bmpHeight, bmpLeft.getHeight());
        float scaleRate = getScaleRate(number, bmpHeight);
        Bitmap leftTmp = Bitmap.createBitmap(bmpLeft.getWidth(), bmpLeft.getHeight() * number, Bitmap.Config.ARGB_4444);
        Canvas topCanvas = new Canvas(leftTmp);
        Matrix matrix = new Matrix();
        for (int i = 0; i < number; i++) {
            matrix.setTranslate(0, bmpLeft.getHeight() * i);
            topCanvas.drawBitmap(bmpLeft, matrix, bpaint);
        }
        matrix.postScale(1, scaleRate);
        Bitmap leftBitmap = Bitmap.createBitmap(leftTmp, 0, 0, leftTmp.getWidth(),
                leftTmp.getHeight(), matrix, true);
        leftTmp.recycle();
        return leftBitmap;
    }


    private int getToDrawNum(int length, int oneSpace) {
        int num = length / oneSpace;
        return num;
    }

    private float getScaleRate(int number, int bitmapWidth) {
        int topwidth = number * bmpTop.getWidth();
        float f = ((float) (bitmapWidth + bmpTop.getHeight())) / topwidth;
        return f;
    }


}
