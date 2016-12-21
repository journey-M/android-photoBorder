package photocut.guowj.dev.photocutdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import photocut.guowj.dev.photocutdemo.imageframs.BorderModle;
import photocut.guowj.dev.photocutdemo.imageframs.BorderModleTest;
import photocut.guowj.dev.photocutdemo.imageframs.ColorLineFrame;
import photocut.guowj.dev.photocutdemo.imageframs.IBorderModle;
import photocut.guowj.dev.photocutdemo.imageframs.SequreBorderModle;
import photocut.guowj.dev.photocutdemo.utile.DensityUtils;

/**
 * Created by guowj on 2016/10/17.
 */

public class PhotoFramView extends RelativeLayout {

    private Uri photoOriginUri = null;

    private Bitmap resizeBitmap = null;
    private Bitmap showBitmap = null;
    private Canvas canvasBitmap = null;
    private Paint linePaint = new Paint();


    private RecyclerView recylerPreView = null;
    private ImageView borderPhotoView = null;
    private BorderAdapter borderAda = null;
    private List<BorderModle> systemBorders = new ArrayList<>();
    private boolean isGifPhoto = false;
    private boolean isPNGPhoto = true;
    private Handler reSetImageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                setBitmap2Views(resizeBitmap);
            }
        }
    };

    public PhotoFramView(Context context) {
        this(context, null);
    }

    public PhotoFramView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public PhotoFramView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
        initData();
    }

    private void initViews() {
        recylerPreView = new RecyclerView(getContext());
        recylerPreView.setId(R.id.photoBorder_recyler);
        LinearLayoutManager lineManager = new LinearLayoutManager(getContext());
        lineManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recylerPreView.setLayoutManager(lineManager);
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(ALIGN_PARENT_BOTTOM, TRUE);
        recylerPreView.setLayoutParams(params);
        recylerPreView.setPadding(0, 0, 0, DensityUtils.dip2px(getContext(), 16f));

        borderPhotoView = new ImageView(getContext());
        LayoutParams photoParms = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        photoParms.addRule(ABOVE, R.id.photoBorder_recyler);
        borderPhotoView.setLayoutParams(photoParms);

        this.addView(recylerPreView);
        this.addView(borderPhotoView);
        initBorders();
    }

    private void initData() {
        linePaint.setColor(Color.GREEN);
        linePaint.setStrokeWidth(5);
    }


    private void initBorders() {
        systemBorders.clear();
        //无边框
        BorderModle bModle = new BorderModleTest(R.raw.img_frame0);
        systemBorders.add(bModle);
        //边框1

        BorderModle bModle1 = new BorderModleTest(R.raw.img_frame1);
        systemBorders.add(bModle1);

        BorderModle bModle2 = new BorderModleTest(R.raw.img_frame2);
        systemBorders.add(bModle2);

        BorderModle bModle3 = new BorderModleTest(R.raw.img_frame3);
        systemBorders.add(bModle3);

        BorderModle bModle4 = new BorderModleTest(R.raw.img_frame4);
        systemBorders.add(bModle4);

        BorderModle bModle5 = new BorderModleTest(R.raw.img_frame5);
        systemBorders.add(bModle5);

        BorderModle bModle6 = new BorderModleTest(R.raw.img_frame6_border);
        systemBorders.add(bModle6);

        BorderModle bModle7 = new BorderModleTest(R.raw.img_frame7);
        systemBorders.add(bModle7);

        BorderModle bModle8 = new BorderModleTest(R.raw.img_frame8);
        systemBorders.add(bModle8);

        borderAda = new BorderAdapter();
        recylerPreView.setAdapter(borderAda);
    }


    SequreBorderModle nCommentBorder = null;

    private void newDrawBitmapFram(IBorderModle nCommentBorder, int spaceDeviation) {
        Matrix matrix = new Matrix();
        Log.e("tag", "leftborderWidth  = " + nCommentBorder.getLeftBorderWidth());

        int leftStart = (getWidth() - resizeBitmap.getWidth()) / 2;
        int topStart = (getHeight() - resizeBitmap.getHeight()) / 2 - spaceDeviation;
        int rightStart = (getWidth() - resizeBitmap.getWidth()) / 2 + resizeBitmap.getWidth();
        int bottomStart = (getHeight() - resizeBitmap.getHeight()) / 2 + resizeBitmap.getHeight() - spaceDeviation;

        //绘制四个角 左上，右上，左下， 右下
        matrix.setTranslate(leftStart - nCommentBorder.getLeftBorderWidth(), topStart - nCommentBorder.getLeftBorderWidth());
        canvasBitmap.drawBitmap(nCommentBorder.getLeftTopConer(), matrix, linePaint);
        matrix.setTranslate(rightStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()),
                topStart - nCommentBorder.getLeftBorderWidth());
        canvasBitmap.drawBitmap(nCommentBorder.getRightTopConer(), matrix, linePaint);
        matrix.setTranslate(leftStart - nCommentBorder.getLeftBorderWidth(),
                bottomStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()) + nCommentBorder.getExtraHeight());
        canvasBitmap.drawBitmap(nCommentBorder.getRightBottomConer(), matrix, linePaint);
        matrix.setTranslate(rightStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()),
                bottomStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()) + nCommentBorder.getExtraHeight());
        canvasBitmap.drawBitmap(nCommentBorder.getLeftBottomConer(), matrix, linePaint);

        //绘制上边框
        int topWidth = resizeBitmap.getWidth() - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()) * 2;
        matrix.setTranslate(leftStart + (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()),
                topStart - nCommentBorder.getLeftBorderWidth());

        if (topWidth > 0) {
            canvasBitmap.drawBitmap(nCommentBorder.getTopBorder(topWidth), matrix, linePaint);
        }

        //绘制左侧边框
        int height = resizeBitmap.getHeight() - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()) * 2
                + nCommentBorder.getExtraHeight();
        matrix.setTranslate(leftStart - nCommentBorder.getLeftBorderWidth(),
                topStart + (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()));
        if (height > 0) {
            canvasBitmap.drawBitmap(nCommentBorder.getLeftBorder(height), matrix, linePaint);
        }
        //绘制右侧边框
        matrix.setTranslate(rightStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()),
                topStart + (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()));
        if (height > 0) {
            canvasBitmap.drawBitmap(nCommentBorder.getRightBorder(height), matrix, linePaint);
        }
        //绘制底部图形
        matrix.setTranslate(leftStart + (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()),
                bottomStart - (nCommentBorder.getCornerWidth() - nCommentBorder.getLeftBorderWidth()) + nCommentBorder.getExtraHeight());
        if (topWidth > 0) {
            canvasBitmap.drawBitmap(nCommentBorder.getBottomBorder(topWidth), matrix, linePaint);
        }
    }

    private void drawingFram3ExtraFont(int resId, IBorderModle nCommentBorder, int spaceDeviation) {
        int leftStart = (getWidth() - resizeBitmap.getWidth()) / 2;
        int topStart = (getHeight() - resizeBitmap.getHeight()) / 2 - spaceDeviation;
        Bitmap bmpTemp = BitmapFactory.decodeStream(getContext().getResources().openRawResource(resId));

        nCommentBorder.setExtraheight(nCommentBorder.getCornerWidth());
        int startx = leftStart + (resizeBitmap.getWidth() - bmpTemp.getWidth()) / 2;
        int starty = resizeBitmap.getHeight() - nCommentBorder.getCornerWidth() + (nCommentBorder.getCornerWidth() - bmpTemp.getHeight()) / 2
                + nCommentBorder.getExtraHeight();
        Matrix matrixtmp = new Matrix();
        matrixtmp.setTranslate(startx, starty + topStart);
        canvasBitmap.drawBitmap(bmpTemp, matrixtmp, linePaint);
    }

    private void drawingFram8ExtraImage(int resId, IBorderModle nCommentBorder, int spaceDeviation) {
        int leftStart = (getWidth() - resizeBitmap.getWidth()) / 2;
        int topStart = (getHeight() - resizeBitmap.getHeight()) / 2 - spaceDeviation;
        Bitmap bmpTemp = BitmapFactory.decodeStream(getContext().getResources().openRawResource(resId));

        float scalRate = (float) resizeBitmap.getWidth() / (float) bmpTemp.getWidth();
        Matrix matrix = new Matrix();
        matrix.postScale(scalRate, scalRate);
        Bitmap bmpToDraw = Bitmap.createBitmap(bmpTemp, 0, 0, bmpTemp.getWidth(), bmpTemp.getHeight(), matrix, true);
        bmpTemp.recycle();

        nCommentBorder.setExtraheight((int) (bmpTemp.getHeight() * scalRate));

        int startx = leftStart;
        int starty = topStart + (resizeBitmap.getHeight() - bmpToDraw.getHeight()) + nCommentBorder.getExtraHeight();
        Matrix matrixtmp = new Matrix();
        matrixtmp.setTranslate(startx, starty);
        canvasBitmap.drawBitmap(bmpToDraw, matrixtmp, linePaint);
    }

    private void setPrePhotoImageSize(int resId, IBorderModle nCommentBorder) {
        Bitmap bmpTemp = BitmapFactory.decodeStream(getContext().getResources().openRawResource(resId));
        float scalRate = (float) resizeBitmap.getWidth() / (float) bmpTemp.getWidth();
        bmpTemp.recycle();
        nCommentBorder.setExtraheight((int) (bmpTemp.getHeight() * scalRate));
    }


    private void drawingFram6() {
        Paint clearpaint = new Paint();
        clearpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvasBitmap.drawPaint(clearpaint);

        //先绘制背景，后绘制 图片
        ColorLineFrame colorFram = new ColorLineFrame(0, 0);
        Bitmap bmpTmp = colorFram.getFramBitmap(resizeBitmap.getWidth() + 36 * 2, resizeBitmap.getHeight() + 36 * 2);
        Matrix matrixLeft = new Matrix();
        matrixLeft.setTranslate(showBitmapLeft - 36, showBitmapTop - 36);
        canvasBitmap.drawBitmap(bmpTmp, matrixLeft, linePaint);

        Matrix matrix = new Matrix();
        matrix.setTranslate(showBitmapLeft, showBitmapTop);
        canvasBitmap.drawBitmap(resizeBitmap, matrix, linePaint);
        borderPhotoView.setImageBitmap(showBitmap);
        borderPhotoView.invalidate();
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
        setBitmap2Views(resizeBitmap);
        if (borderAda != null) {
            borderAda.notifyDataSetChanged();
        }

    }


    /**
     * 设置照片的路径
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


    private void setBitmap2Views(Bitmap bitmap) {
        Bitmap.Config bmpConfig = bitmap.getConfig();
        if (bmpConfig == null) {
            bmpConfig = Bitmap.Config.ARGB_4444;
        }
        showBitmap = Bitmap.createBitmap(getWidth(), getHeight(), bmpConfig);
        canvasBitmap = new Canvas(showBitmap);
        Matrix matrix = new Matrix();
        int tempLeft = 0;
        int tempTop = 0;
        if (getWidth() > bitmap.getWidth()) {
            tempLeft = getWidth() - bitmap.getWidth();
            tempLeft = tempLeft / 2;
        }
        if (getHeight() > bitmap.getHeight()) {
            tempTop = getHeight() - bitmap.getHeight();
            tempTop = tempTop / 2;
        }
        //设置背景要画的图片
        matrix.setTranslate(tempLeft, tempTop);
        canvasBitmap.drawBitmap(bitmap, matrix, linePaint);
        borderPhotoView.setImageBitmap(showBitmap);

        showBitmapLeft = tempLeft;
        showBitmapTop = tempTop;
    }


    private int showBitmapLeft = 0;
    private int showBitmapTop = 0;

    private void clearAndRedraw() {
        clearAndReDraw(0);
    }


    private void clearAndReDraw(int extraSpace) {
        Paint clearpaint = new Paint();
        clearpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        canvasBitmap.drawPaint(clearpaint);

        Matrix matrix = new Matrix();
        matrix.setTranslate(showBitmapLeft, showBitmapTop - extraSpace);
        canvasBitmap.drawBitmap(resizeBitmap, matrix, linePaint);
        borderPhotoView.setImageBitmap(showBitmap);
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
            DataSource<CloseableReference<CloseableImage>> dataSource = imagePipeLine.fetchDecodedImage(imageRequest, App.getInstanse());

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
            Log.e("tag", "localBitmap: width =" + localbitmap.getWidth() + "---height=" + localbitmap.getHeight());
            Bitmap bmpTmp = zoomPhoto2ImageView(localbitmap, getWidth() * 3 / 4, getHeight() * 3 / 4);
            Log.e("tag", "resizeBitmap: width =" + bmpTmp.getWidth() + "---height=" + bmpTmp.getHeight());
            Log.e("tag", "view: width =" + getWidth() + "---height=" + getHeight());
//            localbitmap.recycle();
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

        float toScal = Math.min(scaleHeight, scaleWidth);
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
        Log.e("tag", "inSampleSize = " + inSampleSize);
        return inSampleSize;
    }


    public PhotoCropResult getBorderPhotoResult() {
        PhotoCropResult result = new PhotoCropResult();
        int cropWidth = resizeBitmap.getWidth() + 72;
        int extraHeight = nCommentBorder == null ? 0 : nCommentBorder.getExtraHeight();
        int cropHeight = resizeBitmap.getHeight() + 72 + extraHeight;
        if (cropWidth > getWidth() || cropHeight > getHeight()) {
            return null;
        }
        Bitmap resultBitmap = Bitmap.createBitmap(showBitmap, showBitmapLeft - 36, showBitmapTop - 36 - extraHeight / 2,
                cropWidth, cropHeight);
        result.bitmapResult = resultBitmap;
        result.bitmapSavedPath = saveBitmap(resultBitmap, generateFileName());
        return result;
    }

    private String generateFileName() {
        return "1425547" + System.currentTimeMillis();
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

    class BorderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private int currentSelectIndex = 0, lastSelectIndex = 0;
        private Map<Integer, BorderViewHolder> mapHolders = new HashMap<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(getContext()).inflate(R.layout.photofram_preview_item, parent, false);
            return new BorderViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            BorderModleTest modle = (BorderModleTest) systemBorders.get(position);
            BorderViewHolder bholder = (BorderViewHolder) holder;
//            bholder.bitmapImage.setImageURI(photoOriginUri);
            DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                    .setUri(photoOriginUri)
                    .setAutoPlayAnimations(true) // 设置加载图片完成后是否直接进行播放
                    .build();
            bholder.bitmapImage.setController(draweeController);
            bholder.itemView.setOnClickListener(new OnItemClick(position));
            FrameLayout.LayoutParams fLayout = (FrameLayout.LayoutParams) bholder.itemFram.getLayoutParams();
            //设置覆盖的图片的
            bholder.coverImage.setImageResource(modle.borderSrcid);

            if (position == currentSelectIndex) {
                fLayout.topMargin = 0;
            } else {
                fLayout.topMargin = DensityUtils.dip2px(bholder.itemView.getContext(), 8);
            }

            bholder.itemFram.setLayoutParams(fLayout);
            Bitmap bmp = BitmapFactory.decodeStream(getContext().getResources().openRawResource(modle.resBgimg));
            bholder.coverImage.setImageBitmap(bmp);

            mapHolders.put(position, bholder);
        }

        @Override
        public int getItemCount() {
            return systemBorders.size();
        }

        class OnItemClick implements OnClickListener {
            private int position = -1;

            OnItemClick(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                lastSelectIndex = currentSelectIndex;
                currentSelectIndex = position;
//                BorderAdapter.this.notifyDataSetChanged();

                BorderViewHolder bholderLast = mapHolders.get(lastSelectIndex);
                FrameLayout.LayoutParams fLayoutLast = (FrameLayout.LayoutParams) bholderLast.itemFram.getLayoutParams();
                fLayoutLast.topMargin = DensityUtils.dip2px(bholderLast.itemView.getContext(), 8);
                bholderLast.itemFram.setLayoutParams(fLayoutLast);

                BorderViewHolder bholderCurrent = mapHolders.get(currentSelectIndex);
                FrameLayout.LayoutParams fLayout = (FrameLayout.LayoutParams) bholderCurrent.itemFram.getLayoutParams();
                fLayout.topMargin = 0;
                bholderCurrent.itemFram.setLayoutParams(fLayout);


                clearAndRedraw();
                if (position == 0) {
                    //无边框
                }
                if (position == 1) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame1_1,
                            R.raw.img_frame1_2, R.raw.img_frame1_3, R.raw.img_frame1_4, R.raw.img_frame1_5,
                            R.raw.img_frame1_6, R.raw.img_frame1_7, R.raw.img_frame1_8);
                    nCommentBorder.setLeftSpace(36);
                    nCommentBorder.setExtraheight(nCommentBorder.getCornerWidth() - 36);
                    int spaceDeviation = nCommentBorder.getExtraHeight() / 2;
                    clearAndReDraw(spaceDeviation);
                    newDrawBitmapFram(nCommentBorder, spaceDeviation);
                } else if (position == 2) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame2_1,
                            R.raw.img_frame2_2, R.raw.img_frame2_3, R.raw.img_frame2_4, R.raw.img_frame2_7,
                            R.raw.img_frame2_6, R.raw.img_frame2_5, R.raw.img_frame2_8);
                    newDrawBitmapFram(nCommentBorder, 0);
                } else if (position == 3) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame3_1,
                            R.raw.img_frame3_2, R.raw.img_frame3_3, R.raw.img_frame3_4, R.raw.img_frame3_5,
                            R.raw.img_frame3_6, R.raw.img_frame3_7, R.raw.img_frame3_8);

                    nCommentBorder.setLeftSpace(0);
                    nCommentBorder.setExtraheight(nCommentBorder.getCornerWidth());
                    int spaceDeviation = nCommentBorder.getExtraHeight() / 2;
                    clearAndReDraw(spaceDeviation);
                    newDrawBitmapFram(nCommentBorder, spaceDeviation);
                    drawingFram3ExtraFont(R.raw.img_frame3_word, nCommentBorder, spaceDeviation);
                } else if (position == 4) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame4_1,
                            R.raw.img_frame4_2, R.raw.img_frame4_3, R.raw.img_frame4_4, R.raw.img_frame4_7,
                            R.raw.img_frame4_6, R.raw.img_frame4_5, R.raw.img_frame4_8);
                    nCommentBorder.setLeftSpace(36);
                    newDrawBitmapFram(nCommentBorder, 0);
                } else if (position == 5) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame5_1,
                            R.raw.img_frame5_2, R.raw.img_frame5_3, R.raw.img_frame5_4, R.raw.img_frame5_7,
                            R.raw.img_frame5_6, R.raw.img_frame5_5, R.raw.img_frame5_8);
                    nCommentBorder.setLeftSpace(36);
                    newDrawBitmapFram(nCommentBorder, 0);
                } else if (position == 6) {
                    drawingFram6();
                } else if (position == 7) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame7_1,
                            R.raw.img_frame7_2, R.raw.img_frame7_3, R.raw.img_frame7_4, R.raw.img_frame7_7,
                            R.raw.img_frame7_6, R.raw.img_frame7_5, R.raw.img_frame7_8);
                    newDrawBitmapFram(nCommentBorder, 0);
                } else if (position == 8) {
                    nCommentBorder = new SequreBorderModle(getContext(), R.raw.img_frame8_1,
                            R.raw.img_frame8_2, R.raw.img_frame8_3, R.raw.img_frame8_4, R.raw.img_frame8_7,
                            R.raw.img_frame8_6, R.raw.img_frame8_5, R.raw.img_frame8_8);
                    setPrePhotoImageSize(R.raw.img_frame8_bg, nCommentBorder);
                    //重新绘制
                    int spaceDeviation = nCommentBorder.getExtraHeight() / 2;
                    clearAndReDraw(spaceDeviation);
                    newDrawBitmapFram(nCommentBorder, spaceDeviation);
                    drawingFram8ExtraImage(R.raw.img_frame8_bg, nCommentBorder, spaceDeviation);

                }
            }
        }


        class BorderViewHolder extends RecyclerView.ViewHolder {

            public ImageView coverImage;
            public SimpleDraweeView bitmapImage;
            public FrameLayout itemFram;

            public BorderViewHolder(View itemView) {
                super(itemView);
                itemFram = (FrameLayout) itemView.findViewById(R.id.photofram_item_fram);
                coverImage = (ImageView) itemView.findViewById(R.id.imgCropViewCover);
                bitmapImage = (SimpleDraweeView) itemView.findViewById(R.id.imgDraweeView);
            }
        }
    }

}
