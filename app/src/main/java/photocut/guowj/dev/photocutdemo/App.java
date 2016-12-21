package photocut.guowj.dev.photocutdemo;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;

/**
 * Created by guowj on 2016/12/21.
 */

public class App extends Application {


    private static App instanse = null;

    @Override
    public void onCreate() {
        super.onCreate();


//        ImagePipelineConfig pipelineConfig = ImagePipelineConfigFactory.getOkHttpImagePipelineConfig(getApplicationContext());
        //0.8.1版本没有setWebpSupportEnabled方法,0.9.0版本提供setWebpSupportEnabled,但是对低版本机型支持不好
//        Fresco.initialize(this, pipelineConfig);

        instanse = this;
        // 初始化
        Fresco.initialize(this);

    }


    public static Context getInstanse() {
        return instanse;
    }


}
