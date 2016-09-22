package wuxian.me.filepicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wuxian.me.filepicker.view.DocumentView;
import wuxian.me.filepicker.view.FileItem;

/**
 * Created by wuxian on 1/9/2016.
 *
 * Real listview implementation.
 */

public class ListViewProxy implements IListView {

    private ListView mListView;
    private FileAdapter mFileAdapter;


    private HashMap<String, FileItem> mSelectedFiles = new HashMap<>();

    public ListViewProxy(Context context, ListView listView) {
        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }

        if (listView == null) {
            throw new IllegalArgumentException("listview can't be null");
        }

        mListView = listView;
        mFileAdapter = new FileAdapter(context);
        mListView.setAdapter(mFileAdapter);
    }

    @Override
    public void setData(List<FileItem> datas) {
        mFileAdapter.setData(datas);
    }

    @Override
    public void addData(List<FileItem> datas) {
    }

    @Override
    public void notifyDatasetChanged() {
        mFileAdapter.notifyDataSetChanged();
    }

    @Override
    public List<FileItem> getFileItems() {
        return ((FileAdapter) mListView.getAdapter()).getItemDatas();
    }

    @Override
    public void setItemClickListener(final ItemClickListener listener) {
        if (listener == null) {
            return;
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listener.onItemClick(view, position);
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                return listener.onItemLongClick(view, position);
            }
        });

    }

    private class FileAdapter extends BaseAdapter {
        private List<FileItem> mFileItems = new ArrayList<>();
        private Context mContext;

        public FileAdapter(Context context) {
            mContext = context;
        }

        public List<FileItem> getItemDatas() {
            return mFileItems;
        }

        public void setData(List<FileItem> datas) {
            mFileItems.clear();
            mFileItems = datas;
        }

        @Override
        public int getCount() {
            return mFileItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mFileItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int pos) {
            return mFileItems.get(pos).subtitle.length() > 0 ? 0 : 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new DocumentView(mContext);
            }
            DocumentView docView = (DocumentView) convertView;
            FileItem item = mFileItems.get(position);
            docView.setViewByItem(item);

            return convertView;
        }
    }

}
