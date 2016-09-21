package wuxian.me.filepickerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;

import java.util.List;

import wuxian.me.filepicker.IListView;
import wuxian.me.filepicker.IFilePickerListener;
import wuxian.me.filepicker.ListViewProxy;
import wuxian.me.filepicker.FilePickerImpl;

/**
 * Created by wuxian on 20/9/2016.
 */

public class FilePickerActivity extends AppCompatActivity implements IFilePickerListener {

    private IListView mListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filepicker);

        initView();
    }

    private void initView() {

        ListView listView = (ListView) findViewById(R.id.listView);

        mListView = new ListViewProxy(listView,this, this);
    }

    @Override
    public void onEnterMultiSelectMode() {
        Toast.makeText(this,"aha,you have enterred multiseletmode!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQuitMultiSelectMode() {
        Toast.makeText(this,"aha,you have quit multiseletmode!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onFilesSelected(List<String> files) {
        if(files == null){
            return;
        }

        Toast.makeText(this,"aha,you have select "+files.size()+" files!",Toast.LENGTH_LONG).show();

    }
}
