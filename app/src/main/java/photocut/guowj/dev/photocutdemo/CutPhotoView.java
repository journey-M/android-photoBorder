package photocut.guowj.dev.photocutdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposePathEffect;
import android.graphics.CornerPathEffect;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by guowj on 2016/10/17.
 */

public class CutPhotoView extends FrameLayout implements View.OnTouchListener {

    /**
     * 获取擦除照片时候放大的回调
     */
    public interface IOnImageEraseCallBack {
        void onImageErase(Bitmap bitmap, int touchX, int touchY);

        void onPathSet(Path cropPath);
    }

    private final int MODECROP = 0x11;
    private final int MODEERASE = 0x12;
    private int erasePaintSize = 10;
    private int curentMode = MODECROP;
    private String pathPaintColor = "#fcb200";
    private int pathPaintStrokWidth = 12;

    private Uri photoOriginUri = null;
    private ImageView photoView, coverView;
    private float originDownx, originDowny, downx, downy, upx, upy;
    private int cropdLeft, cropdTop, cropdRight, cropdButtom;

    private Path clipPath = null;
    //临时的画笔路径
    private Path clearPath = null;
    //画笔的历史记录
    private List<Path> eraseHistoryPaths = new ArrayList<>();
    private Bitmap resizeBitmap = null;
    private Bitmap coverBitmap = null;
    private Bitmap showBitmap = null;
    private Bitmap cropdBitmap = null;
    private Canvas canvasCover = null;
    private Canvas canvasBitmap = null;
    private Canvas canvasShowBack = null;

    private Paint linePaintUp = new Paint();
    private Paint erasePaint = new Paint();
    private Paint clearCanvasPaint = new Paint();
    private ComposePathEffect composEffectLine = new ComposePathEffect(new DashPathEffect(new float[]{20, 20}, 0), new CornerPathEffect(25.0f));
    private Bitmap bmpEraseCircle = null;
    private boolean isPNGPhoto = true;
    private boolean isGifPhoto = false;
    //连续擦除的次数
    private int undoEraseSerialNum = 0;
    private int undoMinIndex = -1;
    //照片回调缩略图的半径
    private int mEraseEnlargeRadios = 200;
    //裁切图片的回调
    private IOnImageEraseCallBack onImageEraseCallback;

    private Handler reSetImageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                setBitmap2Views(resizeBitmap);
            }
        }
    };


    public CutPhotoView(Context context) {
        this(context, null);
    }

    public CutPhotoView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CutPhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        //初始化设置子View
        this.setOnTouchListener(this);
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        photoView = new ImageView(getContext());
        coverView = new ImageView(getContext());
        photoView.setLayoutParams(layoutParams);
        coverView.setLayoutParams(layoutParams);
        this.addView(photoView);
        this.addView(coverView);

        //重置划线的画笔
        resetDrawlinPaint();

        //设置清空画布的画笔
        clearCanvasPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        clearCanvasPaint.setAntiAlias(true);

        //设置橡皮擦画笔
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setAntiAlias(true);
        erasePaint.setColor(Color.BLACK);
        erasePaint.setAlpha(0xff);
        erasePaint.setStrokeWidth(erasePaintSize);
        erasePaint.setAntiAlias(true);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        //设置画笔特效
        Path p = new Path();
        p.addCircle(0, 0, erasePaintSize, Path.Direction.CCW);
        PathDashPathEffect paDashEffect = new PathDashPathEffect(p, 2.0f, 3.0f, PathDashPathEffect.Style.ROTATE);
        ComposePathEffect composEffect = new ComposePathEffect(paDashEffect, new CornerPathEffect(25.0f));
        erasePaint.setPathEffect(composEffect);

    }

    private void resetDrawlinPaint() {
        linePaintUp.setAntiAlias(true);
        linePaintUp.setColor(Color.parseColor(pathPaintColor));
        linePaintUp.setStrokeWidth(pathPaintStrokWidth);
        linePaintUp.setAntiAlias(true);
        linePaintUp.setStyle(Paint.Style.STROKE);
        linePaintUp.setPathEffect(null);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (resizeBitmap == null || coverBitmap == null) {
            return false;
        }
        switch (curentMode) {
            case MODECROP:
                drawPhotoCutPath(event);
                break;
            case MODEERASE:
                drawPhotoErase(event);
                break;
        }
        return true;
    }


    /**
     * 设置橡皮擦的大小
     *
     * @param paintSize
     */
    public void setErasePaintSize(int paintSize) {
        erasePaintSize = paintSize;
        erasePaint.setStrokeWidth(paintSize);
        Path p = new Path();
        p.addCircle(0, 0, erasePaintSize, Path.Direction.CCW);
        PathDashPathEffect paDashEffect = new PathDashPathEffect(p, 2.0f, 3.0f, PathDashPathEffect.Style.ROTATE);
        ComposePathEffect composEffect = new ComposePathEffect(paDashEffect, new CornerPathEffect(25.0f));
        erasePaint.setPathEffect(composEffect);
        InputStream instream = null;
        switch (paintSize) {
            case 15:
                instream = getContext().getResources().openRawResource(R.raw.icon_dig1);
                break;
            case 25:
                instream = getContext().getResources().openRawResource(R.raw.icon_dig2);
                break;
            case 38:
                instream = getContext().getResources().openRawResource(R.raw.icon_dig3);
                break;
        }
        if (instream != null) {
            bmpEraseCircle = BitmapFactory.decodeStream(instream);
        }

    }

    /**
     * 设置擦除的时候图片返回的大小
     *
     * @param mEraseEnlargeRadios
     */
    public void setmEraseEnlargeRadios(int mEraseEnlargeRadios) {
        this.mEraseEnlargeRadios = mEraseEnlargeRadios;
    }

    public void setOnImageEraseCallback(IOnImageEraseCallBack onImageEraseCallback) {
        this.onImageEraseCallback = onImageEraseCallback;
    }


    private void onGetEnlargeImage(MotionEvent event, int x, int y, int rados, boolean isUp) {
        if (onImageEraseCallback != null) {
            int startx = 0, starty = 0;
            int circlex = 0, circley = 0;
            if (x - rados < 0) {
                startx = 0;
                circlex = x;
            } else if (x + rados >= showBitmap.getWidth()) {
                startx = showBitmap.getWidth() - 2 * rados;
                circlex = 2 * rados - (showBitmap.getWidth() - x);
            } else {
                startx = x - rados;
                circlex = rados;
            }

            if (y - rados < 0) {
                starty = 0;
                circley = y;
            } else if (y + rados >= showBitmap.getHeight()) {
                starty = showBitmap.getHeight() - 2 * rados;
                circley = 2 * rados - (showBitmap.getHeight() - y);
            } else {
                starty = y - rados;
                circley = rados;
            }
            Matrix matrix = new Matrix();
            matrix.setTranslate(circlex - bmpEraseCircle.getWidth() / 2, circley - bmpEraseCircle.getHeight() / 2);
            if (isUp) {
                onImageEraseCallback.onImageErase(null, x, y);
            } else {
                Bitmap bitmapTemp = Bitmap.createBitmap(showBitmap, startx, starty, rados * 2, rados * 2);
                canvasShowBack = new Canvas(bitmapTemp);
                canvasShowBack.drawBitmap(bmpEraseCircle, matrix, linePaintUp);
                onImageEraseCallback.onImageErase(bitmapTemp, (int) event.getRawX(), (int) event.getRawY());
            }

        }
    }


    private void switchMode(int mode) {
        if (mode != MODECROP && mode != MODEERASE) {
            throw new IllegalArgumentException("该状态不存在");
        }
        curentMode = mode;
        if (mode == MODECROP) {
            coverView.setVisibility(View.VISIBLE);
            if (clearPath != null) {
                Paint clearPaint = new Paint();
                clearPaint.setStrokeWidth(15);
                clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                canvasCover.drawPath(clipPath, clearPaint);
                coverView.invalidate();
            }
        }
        if (mode == MODEERASE) {
            coverView.setVisibility(View.INVISIBLE);
        }
    }


    public boolean cropBitmapByPath() {
        if (clipPath != null) {
            clipPath.setFillType(Path.FillType.INVERSE_WINDING);
            Paint xferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            xferPaint.setColor(Color.BLACK);
            xferPaint.setAlpha(0xff);
            xferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvasBitmap.drawPath(clipPath, xferPaint);
        } else {
            return false;
        }
        cropdBitmap = Bitmap.createBitmap(showBitmap);
        photoView.invalidate();
        coverView.setVisibility(View.INVISIBLE);
        switchMode(MODEERASE);
        return true;
    }


    /**
     * 绘制剪切的路径
     *
     * @param event
     */
    private void drawPhotoCutPath(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //重新绘制 canvas上路径的图像
                resetDrawlinPaint();
                canvasCover.drawPaint(clearCanvasPaint);
                originDownx = downx = getTouchX(event.getX());
                originDowny = downy = getTouchY(event.getY());
                clipPath = new Path();
                clipPath.moveTo(downx, downy);
                break;
            case MotionEvent.ACTION_MOVE:
                upx = getTouchX(event.getX());
                upy = getTouchY(event.getY());
                setCropRectSize((int) upx, (int) upy);
                canvasCover.drawLine(downx, downy, upx, upy, linePaintUp);
                clipPath.lineTo(upx, upy);
                photoView.invalidate();
                coverView.invalidate();
                downx = upx;
                downy = upy;
                break;
            case MotionEvent.ACTION_UP:
                upx = getTouchX(event.getX());
                upy = getTouchY(event.getY());
                clipPath.lineTo(upx, upy);
                canvasCover.drawLine(downx, downy, upx, upy, linePaintUp);
                clipPath.lineTo(originDownx, originDowny);
                canvasCover.drawLine(upx, upy, originDownx, originDowny, linePaintUp);
                canvasCover.drawPaint(clearCanvasPaint);
                clipPath.close();
                linePaintUp.setPathEffect(composEffectLine);
                canvasCover.drawPath(clipPath, linePaintUp);
                photoView.invalidate();
                coverView.invalidate();
                if (onImageEraseCallback != null) {
                    onImageEraseCallback.onPathSet(clipPath);
                }
                break;
        }

    }

    Path tmpPath = new Path();
    //绘制临时的点
    private BlockingQueue<Point> blck = new LinkedBlockingQueue();

    private synchronized void addNewPoint(Point np) {
        if (blck.size() > 10) {
            blck.poll();
        }
        blck.add(np);
    }


    private void drawPhotoErase(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //清除临时记录
                blck.clear();
                clearPath = new Path();
                clearPath.reset();
                downx = event.getX();
                downy = event.getY();
                clearPath.moveTo(downx, downy);
                break;
            case MotionEvent.ACTION_MOVE:
                tmpPath.reset();
                boolean hasSet = false;
                while (blck.size() > 0) {
                    Point ptmp = blck.poll();
                    if (!hasSet) {
                        tmpPath.moveTo(ptmp.x, ptmp.y);
                    } else {
                        tmpPath.lineTo(ptmp.x, ptmp.y);
                    }
                    hasSet = true;
                }
                if (blck.size() == 0) {
                    tmpPath.moveTo(downx, downy);
                }
                final int historySize = event.getHistorySize();
                for (int h = 0; h < historySize; h++) {
                    upx = event.getHistoricalX(h);
                    upy = event.getHistoricalY(h);
                    clearPath.lineTo(upx, upy);
                    addNewPoint(new Point((int) upx, (int) upy));
                    tmpPath.lineTo(upx, upy);
                    downx = upx;
                    downy = upy;
                }
                canvasBitmap.drawPath(tmpPath, erasePaint);
                //line是否过长，创建新的line
                interruputPathAddNew();
                photoView.invalidate();
                onGetEnlargeImage(event, (int) downx, (int) downy, mEraseEnlargeRadios, false);
                break;
            case MotionEvent.ACTION_UP:
                upx = event.getX();
                upy = event.getY();
                //回执临时path
                tmpPath.reset();
                tmpPath.moveTo(downx, downy);
                tmpPath.lineTo(upx, upy);
                canvasBitmap.drawPath(tmpPath, erasePaint);
                clearPath.lineTo(upx, upy);
//                canvasBitmap.drawPath(clearPath, erasePaint);

                photoView.invalidate();
                // 添加记录到本地
                eraseHistoryPaths.add(clearPath);
                onGetEnlargeImage(event, (int) downx, (int) downy, mEraseEnlargeRadios, true);
                undoEraseSerialNum = 0;
                break;
        }
    }


    private void interruputPathAddNew() {
        PathMeasure pathMeasure = new PathMeasure(clearPath, false);
        float length = pathMeasure.getLength();
        if (length > 12000) {
            eraseHistoryPaths.add(clearPath);
            clearPath = new Path();
            clearPath.reset();
            clearPath.moveTo(downx, downy);
        }

    }


    private float getTouchX(float tempX) {
        if (getWidth() <= resizeBitmap.getWidth()) {
            return tempX;
        }
        int spaceLeft = (getWidth() - resizeBitmap.getWidth()) / 2;
        if (tempX < spaceLeft) {
            tempX = spaceLeft;
        } else if (tempX > resizeBitmap.getWidth() + spaceLeft) {
            tempX = resizeBitmap.getWidth() + spaceLeft;
        }
        return tempX;
    }

    private float getTouchY(float tempY) {
        if (getHeight() <= resizeBitmap.getHeight()) {
            return tempY;
        }
        int spaceTop = (getHeight() - resizeBitmap.getHeight()) / 2;
        if (tempY < spaceTop) {
            tempY = spaceTop;
        } else if (tempY >= resizeBitmap.getHeight() + spaceTop) {
            tempY = resizeBitmap.getHeight() + spaceTop;
        }
        return tempY;
    }


    private void setCropRectSize(int tempX, int tempY) {
        if (cropdLeft > tempX) {
            cropdLeft = tempX;
        }
        if (cropdRight < tempX) {
            cropdRight = tempX;
        }
        if (cropdTop > tempY) {
            cropdTop = tempY;
        }
        if (cropdButtom < tempY) {
            cropdButtom = tempY;
        }
    }

    private int resizeBitmapDrawLeft;
    private int resizeBitmapDrawTop;

    private void setBitmap2Views(Bitmap bitmap) {
        switchMode(MODECROP);
        Bitmap.Config bmpConfig = bitmap.getConfig();
        if (bmpConfig == null) {
            bmpConfig = Bitmap.Config.ARGB_4444;
        }
        showBitmap = Bitmap.createBitmap(getWidth(), getHeight(), bmpConfig);
        coverBitmap = Bitmap.createBitmap(getWidth(), getHeight(), bmpConfig);
        canvasCover = new Canvas(coverBitmap);
        canvasBitmap = new Canvas(showBitmap);
        Matrix matrix = new Matrix();
        resizeBitmapDrawLeft = 0;
        resizeBitmapDrawTop = 0;
        if (getWidth() > bitmap.getWidth()) {
            resizeBitmapDrawLeft = getWidth() - bitmap.getWidth();
            resizeBitmapDrawLeft = resizeBitmapDrawLeft / 2;
        }
        if (getHeight() > bitmap.getHeight()) {
            resizeBitmapDrawTop = getHeight() - bitmap.getHeight();
            resizeBitmapDrawTop = resizeBitmapDrawTop / 2;
        }
        //设置背景要画的图片
        matrix.setTranslate(resizeBitmapDrawLeft, resizeBitmapDrawTop);
        canvasBitmap.drawBitmap(bitmap, matrix, linePaintUp);
        photoView.setImageBitmap(showBitmap);
        //设置顶层的布局画出的路径
        coverView.setImageBitmap(coverBitmap);
        //初始化设置裁剪的位置
        cropdLeft = cropdRight = showBitmap.getWidth() / 2;
        cropdTop = cropdButtom = showBitmap.getHeight() / 2;
    }

    /**
     * 设置图片Uri加载图片
     *
     * @param photoUri
     */
    public void setImagePhotoUri(Uri photoUri) {
        photoOriginUri = photoUri;
        //获取加载后的图片
        resizeBitmap = getBitmapFromLoacal(photoUri);
        if (resizeBitmap == null) {
            return;
        }
        Log.e("tag", "---mzbs--width=" + resizeBitmap.getWidth() + "----height=" + resizeBitmap.getHeight());
        setBitmap2Views(resizeBitmap);
    }

    /**
     * 设置图片的Path
     *
     * @param photoPath
     */
    public void setImagePhotoPath(String photoPath) {
        if (photoPath.endsWith(".png")) {
            isPNGPhoto = true;
        } else if (photoPath.endsWith(".jpg") || photoPath.endsWith(".jpeg")) {
            isPNGPhoto = false;
        } else if (photoPath.endsWith(".gif")) {
            isGifPhoto = true;
        }
        File imgFile = new File(photoPath);
        Uri uri = Uri.fromFile(imgFile);
        setImagePhotoUri(uri);
    }

    /**
     * 根据uri从sd卡获取经过裁剪的bitmap， 初步适配View的大小
     *
     * @param photoUri
     * @return
     */
    private Bitmap getBitmapFromLoacal(Uri photoUri) {
        Bitmap localbitmap = null;
        try {
            BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
            bmpFactoryOptions.inJustDecodeBounds = true;
            localbitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(photoUri),
                    null, bmpFactoryOptions);
            //获取原始图片的高度
            int originHeight = bmpFactoryOptions.outHeight;
            int originWidth = bmpFactoryOptions.outWidth;
            //获取View的高度
            int viewHeight = this.getHeight();
            int viewWidth = this.getWidth();
            bmpFactoryOptions.inJustDecodeBounds = false;
            bmpFactoryOptions.inSampleSize = getInSameSize(originWidth, originHeight, viewWidth, viewHeight);
            localbitmap = BitmapFactory.decodeStream(getContext().getContentResolver().openInputStream(photoUri),
                    null, bmpFactoryOptions);

            Log.e("tag", "origin  width=" + originWidth + "--height=" + originHeight + "--inSampleSize=" +
                    bmpFactoryOptions.inSampleSize);
            Log.e("tag", "local  width=" + localbitmap.getWidth() + "--height=" + localbitmap.getHeight());

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (isGifPhoto) {
            ImageDecodeOptionsBuilder decodeOptionsBuilder = new ImageDecodeOptionsBuilder();
            decodeOptionsBuilder.setForceStaticImage(true);
            decodeOptionsBuilder.setDecodePreviewFrame(true);
            ImageDecodeOptions imageDecodeOptions = new ImageDecodeOptions(decodeOptionsBuilder);
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(photoUri)
                    .setImageDecodeOptions(imageDecodeOptions)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();

            ImagePipeline imagePipeLine = Fresco.getImagePipeline();
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeLine.fetchDecodedImage(imageRequest, getContext());

            dataSource.subscribe(new BaseBitmapDataSubscriber() {
                @Override
                protected void onNewResultImpl(Bitmap bitmap) {
                    if (bitmap != null) {
                        resizeBitmap = Bitmap.createBitmap(bitmap);
                        Message msg = reSetImageHandler.obtainMessage();
                        msg.what = 1;
                        reSetImageHandler.sendMessage(msg);
                    }
                }

                @Override
                protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

                }
            }, CallerThreadExecutor.getInstance());
        }

        if (localbitmap != null) {
            Bitmap bmpTmp = zoomPhoto2ImageView(localbitmap, getWidth(), getHeight());
//            if (!isPNGPhoto) {
//                localbitmap.recycle();
//            }
            return bmpTmp;
        }
        return null;
    }

    private Bitmap zoomPhoto2ImageView(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        if (width < newWidth && height < newHeight) {
            return bgimage;
        }
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        //计算要放大的倍数
        float toScal = Math.min(scaleHeight, scaleWidth);
        Log.e("tag", "toScal ==" + toScal);
        // 缩放图片动作
        matrix.postScale(toScal, toScal);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }


    /**
     * 计算图片要加载的大小比例
     *
     * @param originWidth  原始宽度
     * @param originHeight 原始高度
     * @param viewWidth    控件宽度
     * @param viewHeight   控件高度
     * @return
     */
    private int getInSameSize(int originWidth, int originHeight, int viewWidth, int viewHeight) {
        int inSampleSize = 1;
        if (originWidth > viewWidth || originHeight > viewHeight) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) originHeight / (float) viewHeight);
            final int widthRatio = Math.round((float) originWidth / (float) viewWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    public PhotoCropResult getPhotoCropResult() {

        if (cropdLeft == cropdRight) {
            cropdLeft = resizeBitmapDrawLeft;
            cropdRight = resizeBitmapDrawLeft + resizeBitmap.getWidth();
        }
        if (cropdTop == cropdButtom) {
            cropdTop = resizeBitmapDrawTop;
            cropdButtom = resizeBitmapDrawTop + resizeBitmap.getHeight();
        }
        int cropHeight = cropdButtom - cropdTop;
        int cropWdith = cropdRight - cropdLeft;
        if (cropdTop < 0) {
            cropdTop = 0;
            if (cropHeight > resizeBitmap.getHeight()) {
                cropHeight = resizeBitmap.getHeight();
            }
        }
        if (cropdLeft < 0) {
            cropdLeft = 0;
            if (cropWdith > resizeBitmap.getWidth()) {
                cropWdith = resizeBitmap.getWidth();
            }
        }


        Bitmap resultBitmap = Bitmap.createBitmap(showBitmap, cropdLeft, cropdTop, cropWdith, cropHeight);
        String path = saveBitmap(resultBitmap, generateFileName());
        PhotoCropResult result = new PhotoCropResult();
        result.bitmapResult = resultBitmap;
        result.bitmapSavedPath = path;
        return result;
    }

    private String generateFileName() {
        return "1224575" + System.currentTimeMillis();
    }


    public boolean undoErase() {
        if (eraseHistoryPaths.size() < 1) {
            return false;
        }
        showBitmap.recycle();
        showBitmap = Bitmap.createBitmap(cropdBitmap);
        canvasBitmap = new Canvas(showBitmap);
        photoView.setImageBitmap(showBitmap);
        photoView.invalidate();
        if (undoEraseSerialNum < 8 && eraseHistoryPaths.size() > undoMinIndex) {
            eraseHistoryPaths.remove(eraseHistoryPaths.size() - 1);
            undoEraseSerialNum++;
        }
        for (int i = 0; i < eraseHistoryPaths.size(); i++) {
            canvasBitmap.drawPath(eraseHistoryPaths.get(i), erasePaint);
        }
        if (undoEraseSerialNum < 8 && eraseHistoryPaths.size() > undoMinIndex) {
            return true;
        } else {
            undoMinIndex = eraseHistoryPaths.size();
            return false;
        }
    }


    /**
     * 重新加载原始图片
     */
    public void reLoadOriginImage() {
        downx = downy = upx = upy = 0;
        clipPath = null;
        clearPath = null;
        eraseHistoryPaths.clear();
        resizeBitmap = null;
        coverBitmap = null;
        showBitmap = null;
        cropdBitmap = null;
        canvasCover = null;
        canvasBitmap = null;
        undoEraseSerialNum = 0;
        undoMinIndex = -1;
        setImagePhotoUri(photoOriginUri);
    }


    /**
     * 保存到本地的操作（可优化到静态方法中）
     *
     * @param bitmap
     * @param fname
     * @return
     */
    public static String saveBitmap(Bitmap bitmap, String fname) {
        String imgPath = "";
        try {
            File saveFile = getSaveImagepath(fname);
            saveFile.createNewFile();
            FileOutputStream foutStream = new FileOutputStream(saveFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, foutStream);
            foutStream.flush();
            foutStream.close();
            imgPath = saveFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imgPath;
    }

    private static File getSaveImagepath(String fname) {
        File sdCardFile = Environment.getExternalStorageDirectory();
        boolean canWrite = sdCardFile.canWrite();
        Log.e("tag", "can write = " + canWrite);
        File dirFile = new File(sdCardFile + File.separator + "ShiZhuangPhoto" + File.separator);
        if (!dirFile.exists()) {
            boolean b = dirFile.mkdirs();
            Log.e("tag", "创建成功  =" + b);
        }
        File saveFile = new File(dirFile.getAbsolutePath() + File.separator + fname + ".jpg");
        if (saveFile.exists()) {
            saveFile.delete();
        }
        return saveFile;
    }

}
