package wuxian.me.filepicker;

import android.view.View;

import java.util.List;

import wuxian.me.filepicker.view.FileItem;


/**
 * Created by wuxian on 1/9/2016.
 *
 * Interface of a listview-like thing.
 *
 * I don't care about whether it is actually a listview or recyclerview.
 * What I only care is when i call setData,it means load total data,
 * and when I call addData,it means load more data.
 *
 * And I care about wheather it has already enterred multiselect mode.
 *
 */

public interface IListView {
    void setData(List<FileItem> datas);

    void addData(List<FileItem> datas);

    void notifyDatasetChanged();

    void setItemClickListener(ItemClickListener listener);

    List<FileItem> getFileItems();

    interface ItemClickListener {
        void onItemClick(View v, int pos);

        boolean onItemLongClick(View v, int pos);
    }
}
