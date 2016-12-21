package photocut.guowj.dev.photocutdemo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by guowj on 2016/10/8.
 */

public class PhotoCropActivity extends AppCompatActivity implements View.OnClickListener, CutPhotoView.IOnImageEraseCallBack,
        RadioGroup.OnCheckedChangeListener, Runnable {

    public final static int REQUEST_CODE_ADDPIC = 0x11;
    private ArrayList<String> selectedPathList;
    private CutPhotoView cutphotoview;
    private ImageView eraseEnlargeShowView;
    private TextView tvTitleleft = null;
    private TextView tvTitleMidle = null;
    private TextView tvTitleRight = null;
    private RelativeLayout relativeCrop;
    private RelativeLayout relativFigure;
    private FrameLayout framContainer;
    private RadioGroup radioGroup;
    private RadioButton radioSmall;
    private RelativeLayout relativeErase;
    private TextView tvEraseReCrop = null;
    private TextView tvEraseCancle = null;
    private GifImageView mGifImageView;
    private TextView mTextLoadingGif;
    private GifDrawable gifDrawable;
    private LinearLayout mLinearCover;
    private LinearLayout mLinearLoadingGif;

    private boolean isAnimateStaring = false;
    private boolean isFistLoad = false;
    private PhotoCropResult photoResult = null;


    private String colorLight = "#ff8a00";
    private String colorGray = "#cdcdcd";
    private String colorBlack = "#000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photocrop);
        initView();
        initGifDravable();
        dobusiness();
    }


    private void initView() {
        mLinearCover = (LinearLayout) findViewById(R.id.linear_loading_gif);
        mLinearLoadingGif = (LinearLayout) findViewById(R.id.linear_loading_gif);
        mGifImageView = (GifImageView) findViewById(R.id.gif_imageview);
        mTextLoadingGif = (TextView) findViewById(R.id.text_loading_gif);
        tvTitleleft = (TextView) findViewById(R.id.title_tv_left);
        tvTitleMidle = (TextView) findViewById(R.id.title_tv_middle);
        tvTitleRight = (TextView) findViewById(R.id.title_tv_right);
        relativFigure = (RelativeLayout) findViewById(R.id.relative_crop_figure);
        eraseEnlargeShowView = (ImageView) findViewById(R.id.img_erase_enlarge);
        cutphotoview = (CutPhotoView) findViewById(R.id.cutphotoview);
        radioGroup = (RadioGroup) findViewById(R.id.radio_container);
        framContainer = (FrameLayout) findViewById(R.id.fram_container);
        radioSmall = (RadioButton) findViewById(R.id.radio_size_small);
        relativeCrop = (RelativeLayout) findViewById(R.id.relative_crop);
        relativeErase = (RelativeLayout) findViewById(R.id.relative_erase);
        tvEraseCancle = (TextView) findViewById(R.id.erase_cancel);
        tvEraseReCrop = (TextView) findViewById(R.id.erase_recrop);
        radioGroup.setOnCheckedChangeListener(this);
        cutphotoview.setOnImageEraseCallback(this);
        tvTitleleft.setOnClickListener(this);
        tvTitleRight.setOnClickListener(this);
        tvEraseCancle.setOnClickListener(this);
        tvEraseReCrop.setOnClickListener(this);
        tvTitleleft.setText("取消");
        tvTitleMidle.setText("抠图");
    }

    private final int RESULT_LOAD_IMAGE = 0x18;

    private void dobusiness() {
        switchToEraseMode(false);
        radioSmall.setChecked(true);

//        Intent intent = new Intent(this, AlbumActivity.class);
//        intent.putExtra("MAX_COUNT", 1);
//        startActivityForResult(intent, REQUEST_CODE_ADDPIC);

//        //打开本地相册
//        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        //设定结果返回
//        startActivityForResult(i, RESULT_LOAD_IMAGE);

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);


    }

    public void initGifDravable() {
        mTextLoadingGif.setText("正在生成图片···");
        try {
            gifDrawable = new GifDrawable(getResources(), R.drawable.loading_more);
            mGifImageView.setImageDrawable(gifDrawable);
            gifDrawable.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLoading() {
        if (mLinearLoadingGif != null) {
            mLinearLoadingGif.setVisibility(View.VISIBLE);
        }
        gifDrawable.start();
    }

    private void hideLoading() {
        if (mLinearLoadingGif != null) {
            mLinearLoadingGif.setVisibility(View.GONE);
        }
        gifDrawable.stop();
    }

    private void switchToEraseMode(boolean b) {
        if (!b) {
            //抠图撤销置灰
            tvEraseCancle.setTextColor(Color.parseColor(colorGray));
            tvTitleRight.setTextColor(Color.parseColor(colorGray));
            tvTitleRight.setText("下一步");
            relativFigure.setVisibility(View.GONE);
            relativeCrop.setVisibility(View.VISIBLE);
            relativeErase.setVisibility(View.GONE);
            cutphotoview.setBackgroundColor(Color.TRANSPARENT);
            if (isFistLoad) {
                startPropertyAnim();
                isFistLoad = true;
            }
        } else {
            tvTitleRight.setTextColor(Color.BLACK);
            tvTitleRight.setText("完成");
            relativFigure.setVisibility(View.VISIBLE);
            relativeCrop.setVisibility(View.GONE);
            relativeErase.setVisibility(View.VISIBLE);
            cutphotoview.setBackgroundColor(Color.parseColor("#e9e9e9"));
        }
    }

    private void onSetImage() {
        cutphotoview.setImagePhotoPath(selectedPathList.get(0));
    }

    private void onSetImage(String photoPath) {
        cutphotoview.setImagePhotoPath(photoPath);
    }

    private void onSetImage(Uri photoPath) {
        cutphotoview.setImagePhotoUri(photoPath);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.title_tv_left:
                PhotoCropActivity.this.finish();
                break;
            case R.id.title_tv_right:
                if (((TextView) view).getText().toString().equals("下一步")) {
                    if (cutphotoview.cropBitmapByPath()) switchToEraseMode(true);
                } else {
                    //TODO  生成图片并返回
                    showLoading();
                    new Thread(this).start();
                }
                break;
            case R.id.erase_cancel:
                //TODO  橡皮擦 撤销
                boolean b = cutphotoview.undoErase();
                if (b) {
                    tvEraseCancle.setTextColor(Color.parseColor(colorBlack));
                } else {
                    tvEraseCancle.setTextColor(Color.parseColor(colorGray));
                }
                break;
            case R.id.erase_recrop:
                //TODO  重新抠图
                cutphotoview.reLoadOriginImage();
                radioSmall.setChecked(true);
                switchToEraseMode(false);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //点完添加图片的加号 后的返回
//        if (requestCode == REQUEST_CODE_ADDPIC) {
//            if (data != null && resultCode == RESULT_OK) {
//                selectedPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS_STRING);
//                if (selectedPathList != null && selectedPathList.size() > 0) {
//                    cutphotoview.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            onSetImage();
//                        }
//                    });
//                } else {
//                    PhotoCropActivity.this.finish();
//                }
//            } else {
//                PhotoCropActivity.this.finish();
//            }
//        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            //获取返回的数据，这里是android自定义的Uri地址
            final Uri selectedImage = data.getData();
            cutphotoview.post(new Runnable() {
                @Override
                public void run() {
                    onSetImage(selectedImage);
                }
            });

        }


    }

    @Override
    public void onImageErase(Bitmap bitmap, int touchx, int touchy) {
        if (bitmap != null) {
            eraseEnlargeShowView.setImageBitmap(bitmap);
            relativFigure.setBackgroundResource(R.drawable.shape_crop_bg_conor);
        } else {
            eraseEnlargeShowView.setImageBitmap(null);
            relativFigure.setBackgroundColor(Color.TRANSPARENT);
        }
        Rect rect = new Rect();
        relativFigure.getGlobalVisibleRect(rect);
        if (rect.contains(touchx, touchy) && !isAnimateStaring) {
            startPropertyAnim();
        }
        //设置擦除按钮变黑
        tvEraseCancle.setTextColor(Color.parseColor(colorBlack));
    }

    @Override
    public void onPathSet(Path cropPath) {
        if (cropPath == null) {
            tvTitleRight.setTextColor(Color.parseColor(colorGray));
        } else {
            tvTitleRight.setTextColor(Color.parseColor(colorLight));
        }
    }


    // 动画实际执行
    private void startPropertyAnim() {
        // X轴方向上的坐标
        float translationX = relativFigure.getTranslationX();
        float toTranslate = 0;
        if (translationX < 20) {
            FrameLayout.LayoutParams layoutParam = (FrameLayout.LayoutParams) relativFigure.getLayoutParams();
            toTranslate = framContainer.getWidth() - relativFigure.getWidth() - layoutParam.leftMargin * 2;
        }
        ObjectAnimator anim = ObjectAnimator.ofFloat(relativFigure, "translationX", toTranslate);
        anim.setDuration(300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimateStaring = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimateStaring = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isAnimateStaring = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
            case R.id.radio_size_small:
                cutphotoview.setErasePaintSize(15);
                break;
            case R.id.radio_size_middle:
                cutphotoview.setErasePaintSize(25);
                break;
            case R.id.radio_size_large:
                cutphotoview.setErasePaintSize(38);
                break;
        }
    }

    private Handler startNewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1 && photoResult != null) {

                Bundle bundle = new Bundle();
                bundle.putString("path", photoResult.bitmapSavedPath);
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(RESULT_OK, intent);
                PhotoCropActivity.this.finish();
                hideLoading();
            }
        }
    };

    @Override
    public void run() {

        photoResult = cutphotoview.getPhotoCropResult();
        Message msg = startNewHandler.obtainMessage();
        msg.what = 1;
        startNewHandler.sendMessage(msg);
    }
}
