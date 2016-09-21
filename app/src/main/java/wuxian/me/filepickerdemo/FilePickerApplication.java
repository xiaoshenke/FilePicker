package wuxian.me.filepickerdemo;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by wuxian on 21/9/2016.
 */

public class FilePickerApplication extends Application{

    @Override
    public void onCreate(){
        super.onCreate();

        Fresco.initialize(this);  //add this code to fix: Error ,Binary XML file line #10: Error inflating class com.facebook.drawee.view.SimpleDraweeView
    }
}
