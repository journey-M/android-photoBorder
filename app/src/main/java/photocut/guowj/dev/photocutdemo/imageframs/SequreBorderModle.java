package photocut.guowj.dev.photocutdemo.imageframs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

/**
 * Created by guowj on 2016/11/15.
 */

public class SequreBorderModle implements IBorderModle {

    private Context mContext;

    private int resLT;
    private int resLB;
    private int resRT;
    private int resRB;
    private int resTop;
    private int resRight;
    private int resBottom;
    private int resLeft;


    private Paint mPaint = new Paint();

    private Bitmap bmpL, bmpT, bmpR, bmpB;
    private Bitmap bmpCornerLT, bmpCornerLB, bmpCornerRT, bmpCornerRB;


    private int leftSpace = -1;
    private int extraImageHeight = 0;

    public SequreBorderModle(Context mContext) {
        this.mContext = mContext;
    }

    public SequreBorderModle(Context mContext, int resLT, int resTop, int resRT, int resRight, int resRB, int resBottom,
                             int resLB, int resLeft) {
        this.mContext = mContext;
        this.resLB = resLB;
        this.resLT = resLT;
        this.resRB = resRB;
        this.resRT = resRT;
        // 边框
        this.resTop = resTop;
        this.resRight = resRight;
        this.resBottom = resBottom;
        this.resLeft = resLeft;

        bmpT = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resTop));
        bmpL = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resLeft));
        bmpR = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resRight));
        bmpB = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resBottom));
        bmpCornerLT = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resLT));
        bmpCornerLB = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resLB));
        bmpCornerRT = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resRT));
        bmpCornerRB = BitmapFactory.decodeStream(mContext.getResources().openRawResource(resRB));

        Log.e("tag", "border width=" + bmpCornerLB.getWidth() + "--height=" + bmpCornerLB.getHeight() + "--bmpT height=" + bmpT.getHeight());

    }


    @Override
    public Bitmap getLeftTopConer() {
        return bmpCornerLT;
    }

    @Override
    public Bitmap getLeftBottomConer() {
        return bmpCornerLB;
    }

    @Override
    public Bitmap getRightTopConer() {
        return bmpCornerRT;
    }

    @Override
    public Bitmap getRightBottomConer() {
        return bmpCornerRB;
    }


    @Override
    public int getLeftBorderWidth() {
        if (leftSpace > -1) {
            return leftSpace;
        }
        return bmpCornerLT.getWidth() / 2;
    }

    @Override
    public int getTopBorderWidth() {
        return bmpCornerLT.getWidth() / 2;
    }

    @Override
    public int getRightBorderWidth() {
        return bmpCornerLT.getWidth() / 2;
    }

    @Override
    public int getBottomBorderWidth() {
        return bmpCornerLT.getWidth() / 2;
    }


    @Override
    public int getCornerWidth() {
        return bmpCornerLT.getWidth();
    }

    @Override
    public int getExtraHeight() {
        return extraImageHeight;
    }

    @Override
    public void setExtraheight(int extraheight) {
        this.extraImageHeight = extraheight;
    }

    public void setLeftSpace(int leftSpace) {
        this.leftSpace = leftSpace;
    }


    @Override
    public Bitmap getLeftBorder(int height) {
        if (height <= 0) {
            return null;
        }
        int number = getToDrawNumber(height, bmpL.getHeight());
        Bitmap topTmp = Bitmap.createBitmap(getCornerWidth(), number * bmpL.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(topTmp);
        int top = 0;
        for (int i = 0; i < number; i++) {
            canvas.drawBitmap(bmpL, 0, top, mPaint);
            top = top + bmpL.getHeight();
        }
        float scalRate = (float) height / (float) topTmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(1, scalRate);
        Bitmap returnBitmap = Bitmap.createBitmap(topTmp, 0, 0, topTmp.getWidth(), topTmp.getHeight(), matrix, true);
        topTmp.recycle();
        return returnBitmap;
    }

    @Override
    public Bitmap getTopBorder(int width) {
        if (width <= 0) {
            return null;
        }
        int number = getToDrawNumber(width, bmpT.getWidth());
        Bitmap topTmp = Bitmap.createBitmap(number * bmpT.getWidth(), getCornerWidth(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(topTmp);
        int left = 0;
        for (int i = 0; i < number; i++) {
            canvas.drawBitmap(bmpT, left, 0, mPaint);
            left = left + bmpT.getWidth();
        }
        float scalRate = (float) width / (float) topTmp.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scalRate, 1);
        Bitmap returnBitmap = Bitmap.createBitmap(topTmp, 0, 0, topTmp.getWidth(), topTmp.getHeight(), matrix, true);
        topTmp.recycle();
        return returnBitmap;
    }

    @Override
    public Bitmap getRightBorder(int height) {
        if (height <= 0) {
            return null;
        }
        int number = getToDrawNumber(height, bmpR.getHeight());
        Bitmap topTmp = Bitmap.createBitmap(getCornerWidth(), number * bmpR.getHeight(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(topTmp);
        int top = 0;
        for (int i = 0; i < number; i++) {
            canvas.drawBitmap(bmpR, 0, top, mPaint);
            top = top + bmpR.getHeight();
        }
        float scalRate = (float) height / (float) topTmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(1, scalRate);
        Bitmap returnBitmap = Bitmap.createBitmap(topTmp, 0, 0, topTmp.getWidth(), topTmp.getHeight(), matrix, true);
        topTmp.recycle();
        return returnBitmap;
    }

    @Override
    public Bitmap getBottomBorder(int width) {
        if (width <= 0) {
            return null;
        }
        int number = getToDrawNumber(width, bmpT.getWidth());
        Bitmap topTmp = Bitmap.createBitmap(number * bmpT.getWidth(), getCornerWidth(), Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(topTmp);
        int left = 0;
        for (int i = 0; i < number; i++) {
            canvas.drawBitmap(bmpB, left, 0, mPaint);
            left = left + bmpB.getWidth();
        }
        float scalRate = (float) width / (float) topTmp.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scalRate, 1);
        Bitmap returnBitmap = Bitmap.createBitmap(topTmp, 0, 0, topTmp.getWidth(), topTmp.getHeight(), matrix, true);
        topTmp.recycle();
        return returnBitmap;
    }

    private int getToDrawNumber(int widthSpace, int singleSpace) {
        int number = widthSpace / singleSpace;
        int subSpace = widthSpace % singleSpace;
        if (subSpace > singleSpace / 2) {
            return number + 1;
        }
        if (number == 0) {
            number = 1;
        }
        return number;
    }

}
