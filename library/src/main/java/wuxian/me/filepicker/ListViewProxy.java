package wuxian.me.filepicker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import wuxian.me.filepicker.view.DocumentView;
import wuxian.me.filepicker.view.TeleAndroidUtils;

/**
 * Created by wuxian on 1/9/2016.
 */

public class ListViewProxy implements IListView {

    private ListView mListView;
    private FileAdapter mAdapter;
    private FilePickerImpl mPicker;

    private boolean mInMultiSelectMode = false;

    private File mCurrentDir = null;
    private List<HistoryEntry> mHistories = new ArrayList<>();
    private HashMap<String, FileItem> mSelectedFiles = new HashMap<>();

    public ListViewProxy(FilePickerImpl picker, ListView listView, Context context) {
        if (listView == null) {
            throw new IllegalArgumentException("listview can't be null");
        }

        if (context == null) {
            throw new IllegalArgumentException("context can't be null");
        }

        if (picker == null) {
            throw new IllegalArgumentException("picker can't be null");
        }

        mPicker = picker;
        mListView = listView;

        mAdapter = new FileAdapter(context);
        mListView.setAdapter(mAdapter);

        setClickListener(mListView);
    }

    @Override
    public boolean isInMultiSelectMode() {
        return mInMultiSelectMode;
    }

    private void setClickListener(final ListView listView) {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<FileItem> items = ((FileAdapter) listView.getAdapter()).getItemDatas();

                if (position < 0 || position >= items.size()) {
                    return;
                }

                FileItem item = items.get(position);
                File file = item.file;
                if (file == null) { //出现空的情况:比如当前是gallery的item --> 返回上一级
                    if (mHistories.isEmpty()) {
                        mCurrentDir = null;
                        mPicker.listRootFiles();
                        return;
                    }
                    HistoryEntry he = mHistories.remove(mHistories.size() - 1);
                    TeleAndroidUtils.clearDrawableAnimation(listView);
                    //Todo: actionbar ui更改?
                    if (he.dir != null) {
                        mCurrentDir = he.dir;
                        mPicker.listFilesUnder(he.dir);
                    } else {
                        mCurrentDir = null;
                        mPicker.listRootFiles();  //为null说明在root页面
                    }

                    //listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);

                } else if (file.isDirectory()) {
                    if (mPicker.inMultiSelectMode()) {  //多选模式选中文件夹的处理?
                        return;
                    }

                    HistoryEntry he = new HistoryEntry();
                    he.scrollItem = listView.getFirstVisiblePosition();
                    he.scrollOffset = listView.getChildAt(0).getTop();
                    he.dir = mCurrentDir;
                    //he.title = actionBar.getTitle();
                    mHistories.add(he);

                    //Todo 滑动处理
                    FilePickerImpl.State state = mPicker.getFileState(file);

                    if (state == FilePickerImpl.State.STATE_DIR_NORMAL) {
                        mCurrentDir = file;
                        mPicker.listFilesUnder(file);
                    } else {
                        //Todo
                    }

                    //listView.setSelection(0);
                } else {
                    FilePickerImpl.State state = mPicker.getFileState(file);
                    if (state == FilePickerImpl.State.STATE_FILE_NORMAL) {
                        if (mInMultiSelectMode) {
                            if (mSelectedFiles.containsKey(file.toString())) {
                                mSelectedFiles.remove(file.toString());
                            } else {
                                mSelectedFiles.put(file.toString(), item);
                            }
                            if (mSelectedFiles.isEmpty()) {
                                mPicker.filesSelected(new ArrayList<String>());
                                mPicker.quitMultiSelectMode();
                                mInMultiSelectMode = false;
                            } else {
                                List<String> files = new ArrayList<String>();
                                files.addAll(mSelectedFiles.keySet());
                                mPicker.filesSelected(files);
                            }

                            if (view instanceof DocumentView) {
                                ((DocumentView) view).setChecked(mSelectedFiles.containsKey(item.file.toString()), true);
                            }
                        } else {
                            mSelectedFiles.put(file.toString(), item);
                            List<String> files = new ArrayList<String>();
                            files.addAll(mSelectedFiles.keySet());
                            mPicker.filesSelected(files);
                        }
                    } else {
                        //Todo:access error,length 0
                    }
                }
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                List<FileItem> items = ((FileAdapter) listView.getAdapter()).getItemDatas();

                if (position < 0 || position >= items.size()) {
                    return false;
                }

                if (mInMultiSelectMode) {  //处于长按状态时进行长按忽略这个动作
                    return false;
                }

                FileItem item = items.get(position);
                File file = item.file;

                if (file == null || file.isDirectory()) {  //文件夹不允许选择
                    return false;
                }

                FilePickerImpl.State state = mPicker.getFileState(file);

                if (state == FilePickerImpl.State.STATE_FILE_NORMAL) {
                    mInMultiSelectMode = true;
                    mPicker.enterMuitiSelectMode();

                    mSelectedFiles.put(file.toString(), item);
                    if (view instanceof DocumentView) {
                        ((DocumentView) view).setChecked(true, true);
                    }

                    List<String> files = new ArrayList<String>();
                    files.addAll(mSelectedFiles.keySet());
                    mPicker.filesSelected(files);

                    return true;
                } else {
                    //Todo 
                }


                return true;
            }
        });
    }

    @Override
    public void setData(List<FileItem> datas) {
        mAdapter.setData(datas);
    }

    @Override
    public void addData(List<FileItem> datas) {
        //Todo
    }

    @Override
    public void notifyDatasetChanged() {
        mAdapter.notifyDataSetChanged();
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
            mFileItems.addAll(datas);
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
            if (item.icon != 0) {
                ((DocumentView) convertView).setTextAndValueAndTypeAndThumb(item.title, item.subtitle, null, null, item.icon);
            } else {
                String type = item.ext.toUpperCase().substring(0, Math.min(item.ext.length(), 4));
                ((DocumentView) convertView).setTextAndValueAndTypeAndThumb(item.title, item.subtitle, type, item.thumb, 0);
            }
            if (item.file != null) {
                docView.setChecked(mSelectedFiles.containsKey(item.file.toString()), false);
            } else {
                docView.setChecked(false, false);
            }
            return convertView;
        }
    }

    /**
     * 用于返回上一级
     */
    private class HistoryEntry {
        int scrollItem, scrollOffset;
        File dir;
        String title;
    }
}
