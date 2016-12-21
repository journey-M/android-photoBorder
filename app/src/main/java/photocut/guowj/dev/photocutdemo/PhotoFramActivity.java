package photocut.guowj.dev.photocutdemo;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by guowj on 2016/10/8.
 */

public class PhotoFramActivity extends Activity implements View.OnClickListener, Runnable {

    private ArrayList<String> selectedPathList;
    private PhotoFramView borderPhotoView;

    private GifImageView mGifImageView;
    private TextView mTextLoadingGif;
    private GifDrawable gifDrawable;
    private LinearLayout mLinearCover;
    private LinearLayout mLinearLoadingGif;

    private PhotoCropResult photoResult = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photofram);
        initView();
        initGifDravable();
        dobusiness();
    }


    private void initView() {
        mLinearCover = (LinearLayout) findViewById(R.id.linear_loading_gif);
        mLinearLoadingGif = (LinearLayout) findViewById(R.id.linear_loading_gif);
        mGifImageView = (GifImageView) findViewById(R.id.gif_imageview);
        mTextLoadingGif = (TextView) findViewById(R.id.text_loading_gif);

        borderPhotoView = (PhotoFramView) findViewById(R.id.borderPhotoView);

        findViewById(R.id.title_tv_left).setOnClickListener(this);
        findViewById(R.id.title_tv_right).setOnClickListener(this);
    }


    private final int RESULT_LOAD_IMAGE = 0x18;

    private void dobusiness() {
//        Intent intent = new Intent(this, AlbumActivity.class);
//        intent.putExtra("MAX_COUNT", 1);
//        startActivityForResult(intent, REQUEST_CODE_ADDPIC);

        //打开本地相册
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        //设定结果返回
        startActivityForResult(i, RESULT_LOAD_IMAGE);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //点完添加图片的加号 后的返回
//        if (requestCode == REQUEST_CODE_ADDPIC) {
//            if (data != null && resultCode == RESULT_OK) {
//                selectedPathList = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS_STRING);
//                if (selectedPathList != null && selectedPathList.size() > 0) {
//                    borderPhotoView.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            onSetImage();
//                        }
//                    });
//                } else {
//                    PhotoFramActivity.this.finish();
//                }
//            } else {
//                PhotoFramActivity.this.finish();
//            }
//        }

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            //获取返回的数据，这里是android自定义的Uri地址
            final Uri selectedImage = data.getData();
//            String[] filePathColumn = {MediaStore.Images.Media.DATA};
//            //获取选择照片的数据视图
//            Cursor cursor = getContentResolver().query(selectedImage,
//                    filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            //从数据视图中获取已选择图片的路径
//            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//            final String picturePath = cursor.getString(columnIndex);
//            cursor.close();
            borderPhotoView.post(new Runnable() {
                @Override
                public void run() {
                    onSetImage(selectedImage);
                }
            });

        }


    }

    private void onSetImage() {
        borderPhotoView.setImagePhotoPath(selectedPathList.get(0));
    }

    private void onSetImage(Uri path) {
        borderPhotoView.setImagePhotoUri(path);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.title_tv_left:
                PhotoFramActivity.this.finish();
                break;
            case R.id.title_tv_right:
                showLoading();
                new Thread(PhotoFramActivity.this).start();
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
                PhotoFramActivity.this.finish();
                hideLoading();
            }
            if (msg.what == 1 && photoResult == null) {
                Toast.makeText(PhotoFramActivity.this, "图片尺寸太大请选择其它相框", Toast.LENGTH_SHORT).show();
//                PhotoFramActivity.this.finish();
                hideLoading();
            }
        }
    };

    @Override
    public void run() {
        photoResult = borderPhotoView.getBorderPhotoResult();
        Message msg = startNewHandler.obtainMessage();
        msg.what = 1;
        startNewHandler.sendMessage(msg);
    }
}
