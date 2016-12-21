package photocut.guowj.dev.photocutdemo.imageframs;

import android.graphics.Bitmap;

/**
 * Created by guowj on 2016/11/15.
 */

public interface IBorderModle {


    public abstract Bitmap getLeftTopConer();

    public abstract Bitmap getLeftBottomConer();

    public abstract Bitmap getRightTopConer();

    public abstract Bitmap getRightBottomConer();

    public abstract Bitmap getLeftBorder(int height);

    public abstract Bitmap getTopBorder(int width);

    public abstract Bitmap getRightBorder(int height);

    public abstract Bitmap getBottomBorder(int width);


    public abstract int getLeftBorderWidth();

    public abstract int getTopBorderWidth();

    public abstract int getRightBorderWidth();

    public abstract int getBottomBorderWidth();


    public abstract int getCornerWidth();

    public abstract int getExtraHeight();

    public abstract void setExtraheight(int extraheight);


}
