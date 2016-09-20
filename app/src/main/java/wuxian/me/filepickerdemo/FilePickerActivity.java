package wuxian.me.filepickerdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import wuxian.me.filepicker.IListView;
import wuxian.me.filepicker.IMultiFilePicker;
import wuxian.me.filepicker.ListViewProxy;
import wuxian.me.filepicker.MultiFilePickerImpl;

/**
 * Created by wuxian on 20/9/2016.
 */

public class FilePickerActivity extends AppCompatActivity implements IMultiFilePicker {
    MultiFilePickerImpl mPicker = new MultiFilePickerImpl(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filepicker);

        initView();


    }

    private void initView() {

        ListView listView = (ListView) findViewById(R.id.listView);

        IListView iListView = new ListViewProxy(mPicker, listView, this);

        mPicker.setListview(iListView);
        mPicker.listRootFiles();  //默认打开根目录下文件
    }

    @Override
    public void onEnterMuitiSelectMode() {
        Toast.makeText(this,"aha,you have enterred multiseletmode!",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onQuitMultiSelectMode() {
        Toast.makeText(this,"aha,you have quit multiseletmode!",Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean inMultiSelectMode() {
        return mPicker.inMultiSelectMode();
    }

    @Override
    public void onFilesSelected(List<String> files) {
        if(files == null){
            return;
        }

        Toast.makeText(this,"aha,you have select "+files.size()+" files!",Toast.LENGTH_LONG).show();

    }
}