package photocut.guowj.dev.photocutdemo.imageframs;

import android.graphics.Bitmap;

/**
 * Created by guowj on 2016/10/20.
 */

public class BorderModleTest extends BorderModle {

    public int resBgimg = -1;

    public BorderModleTest(int resBg) {
        this.resBgimg = resBg;

    }

    @Override
    public Bitmap getFramBitmap(int bmpWidth, int bmpHeight) {
        return null;
    }

    @Override
    public int getBitmapLeftPadding(int originLeft) {
        return 0;
    }

    @Override
    public int getBitmapTopPadding(int originRight) {
        return 0;
    }

}
