package photocut.guowj.dev.photocutdemo.imageframs;

import android.graphics.Bitmap;

/**
 * Created by guowj on 2016/10/20.
 */

public interface IBorder {


    public Bitmap getFramBitmap(int bmpWidth, int bmpHeight);

    public int getBitmapLeftPadding(int originLeft);

    public int getBitmapTopPadding(int originRight);


}
