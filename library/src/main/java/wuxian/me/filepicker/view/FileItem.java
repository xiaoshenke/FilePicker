package wuxian.me.filepicker.view;

import java.io.File;

/**
 * Created by wuxian on 21/9/2016.
 *
 * Item data of Documentview --> R.layout.view_document
 */

public class FileItem {
    public int iconRes;               //resourceId of iconRes ,for eg. R.mipmap.ic_directory
    public String type = "";          //file type, for eg. word,pdf,..
    public File thumbFile;            //url of file --> 根据file的url拿到thumbnail

    public String title;
    public String subtitle = "";

    public File file;                 //real file

    public boolean isChecked = false;
}
