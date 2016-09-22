package wuxian.me.filepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import wuxian.me.filepicker.view.DocumentView;
import wuxian.me.filepicker.view.FileItem;
import wuxian.me.filepicker.view.Utils;

/**
 * Created by wuxian on 1/9/2016.
 *
 * Implementation class of filepicker.
 *
 * It implements these functions.
 * 1 list files data of current directory,show them in @IListView
 * 2 deal with exceptions for eg. access error,and show some message in Ui
 *
 */

public class FilePickerImpl {
    private static final String ERROR_DIR_ACCESS = "directory access error!";
    private static final String ERROR_FILE_ACCESS = "file access error!";

    public enum State {
        STATE_ERROR_UNKNOW,
        STATE_SDCARD_NOT_MOUNTED,
        STATE_USB_ACTIVE,
        STATE_DIR_NO_FILES,
        STATE_DIR_ACCESS_ERROR,
        STATE_DIR_NORMAL,
        STATE_FILE_NORMAL,
        STATE_FILE_ACCESS_ERROR,
        STATE_FILE_ILLEGAL_LENGTH,
    }

    private IFilePickerListener mListener;  //传入的回调
    private IListView mListView;

    private File mCurrentDir = null;
    private List<HistoryEntry> mHistories = new ArrayList<>();
    private HashMap<String, FileItem> mSelectedFiles = new HashMap<>();
    private boolean mInMultiSelectMode = false;

    private Context mContext;
    public FilePickerImpl(Context context, IListView listView, IFilePickerListener listener) {
        mListView = listView;
        mListener = listener;
        mContext = context;

        mListView.setItemClickListener(getItemClickListener());

    }

    boolean isInMultiSelectMode() {
        return mInMultiSelectMode;
    }

    private IListView.ItemClickListener getItemClickListener() {

        return new IListView.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                List<FileItem> items = mListView.getFileItems();
                if (items == null || items.size() == 0) {
                    return;
                }

                if (position < 0 || position >= items.size()) {
                    return;
                }

                FileItem item = items.get(position);
                File file = item.file;
                if (file == null) { //出现空的情况:比如当前是gallery的item --> 返回上一级
                    if (mHistories.isEmpty()) {
                        mCurrentDir = null;
                        listRootFiles();
                        return;
                    }
                    HistoryEntry he = mHistories.remove(mHistories.size() - 1);
                    //Utils.clearDrawableAnimation(listView);
                    if (he.dir != null) {
                        mCurrentDir = he.dir;
                        listFilesUnder(he.dir);
                    } else {
                        mCurrentDir = null;
                        listRootFiles();  //为null说明在root页面
                    }

                    //listView.setSelectionFromTop(he.scrollItem, he.scrollOffset);

                } else if (file.isDirectory()) {
                    if (isInMultiSelectMode()) {  //多选模式选中文件夹的处理?
                        return;
                    }

                    FilePickerImpl.State state = getFileState(file);

                    if (state == FilePickerImpl.State.STATE_DIR_NORMAL) {
                        HistoryEntry he = new HistoryEntry();
                        //he.scrollItem = listView.getFirstVisiblePosition();
                        //he.scrollOffset = listView.getChildAt(0).getTop();
                        he.dir = mCurrentDir;
                        mHistories.add(he);

                        mCurrentDir = file;
                        listFilesUnder(file);
                    } else {
                        dealFileErrorState(state);
                    }
                } else {
                    FilePickerImpl.State state = getFileState(file);
                    if (state == FilePickerImpl.State.STATE_FILE_NORMAL) {
                        if (mInMultiSelectMode) {
                            if (mSelectedFiles.containsKey(file.toString())) {
                                mSelectedFiles.remove(file.toString());
                            } else {
                                mSelectedFiles.put(file.toString(), item);
                            }
                            if (mSelectedFiles.isEmpty()) {
                                filesSelected(new ArrayList<String>());
                                quitMultiSelectMode();
                                mInMultiSelectMode = false;
                            } else {
                                List<String> files = new ArrayList<String>();
                                files.addAll(mSelectedFiles.keySet());
                                filesSelected(files);
                            }

                            if (view instanceof DocumentView) {
                                item.isChecked = mSelectedFiles.containsKey(item.file.toString());
                                ((DocumentView) view).setChecked(mSelectedFiles.containsKey(item.file.toString()), true);
                            }
                        } else {
                            mSelectedFiles.put(file.toString(), item);
                            List<String> files = new ArrayList<String>();
                            files.addAll(mSelectedFiles.keySet());
                            filesSelected(files);
                        }
                    } else {
                        dealFileErrorState(state);
                    }
                }

            }

            @Override
            public boolean onItemLongClick(View view, int position) {
                List<FileItem> items = mListView.getFileItems();

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

                FilePickerImpl.State state = getFileState(file);

                if (state == FilePickerImpl.State.STATE_FILE_NORMAL) {
                    mInMultiSelectMode = true;
                    enterMuitiSelectMode();

                    if (view instanceof DocumentView) {
                        item.isChecked = true;
                        ((DocumentView) view).setChecked(true, true);
                    }
                    mSelectedFiles.put(file.toString(), item);

                    List<String> files = new ArrayList<String>();
                    files.addAll(mSelectedFiles.keySet());
                    filesSelected(files);

                    return true;
                } else {
                    dealFileErrorState(state);
                    return false;
                }
            }
        };
    }


    private String getSubtitleOfPath(String path) {
        try {
            StatFs stat = new StatFs(path);
            long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
            long free = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
            if (total == 0) {
                return "";
            }
            return "free " + Utils.formatFileSize(free) + " of " + Utils.formatFileSize(total);
        } catch (Exception e) {

        }
        return path;
    }

    /**
     * get files in '/' directory.
     * @return
     */
    @SuppressLint("NewApi")
    private List<FileItem> getRootItems() {
        List<FileItem> items = new ArrayList<>();
        items.clear();
        HashSet<String> paths = new HashSet<>();

        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            //正常挂载 加一行sdcard条目
            FileItem item = new FileItem();
            if (Environment.isExternalStorageRemovable()) {
                item.title = "SdCard";
                item.iconRes = R.mipmap.ic_external_storage;
            } else {
                item.title = "InternalStorage";
                item.iconRes = R.mipmap.ic_storage;
            }

            String defaultPath = Environment.getExternalStorageDirectory().getPath();
            item.subtitle = getSubtitleOfPath(defaultPath);
            item.file = Environment.getExternalStorageDirectory();
            items.add(item);
            paths.add(defaultPath);
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts")); //????
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken();
                    String path = tokens.nextToken();
                    if (paths.contains(path)) {
                        continue;
                    }
                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                            if (!new File(path).isDirectory()) {
                                int index = path.lastIndexOf('/');
                                if (index != -1) {
                                    String newPath = "/storage/" + path.substring(index + 1);
                                    if (new File(newPath).isDirectory()) {
                                        path = newPath;
                                    }
                                }
                            }
                            paths.add(path);
                            try {
                                FileItem item = new FileItem();
                                if (path.toLowerCase().contains("sd")) {
                                    item.title = "SdCard";
                                } else {
                                    item.title = "ExternalStorage";
                                }
                                item.iconRes = R.mipmap.ic_external_storage;
                                item.subtitle = getSubtitleOfPath(path);
                                item.file = new File(path);
                                items.add(item);
                            } catch (Exception e) {

                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                }
            }
        }

        FileItem fs = new FileItem();
        fs.title = "/";
        fs.subtitle = "SystemRoot";
        fs.iconRes = R.mipmap.ic_directory;
        fs.file = new File("/");
        items.add(fs);

        return items;
    }

    public void listRootFiles() {
        if (mListView != null) {
            mListView.setData(getRootItems());
            mListView.notifyDatasetChanged();
        }

    }

    private List<FileItem> getFilesUnder(File dir) {
        List<FileItem> items = new ArrayList<>();

        if (!dir.isDirectory()) {
            return items;
        }

        State state = getFileState(dir);

        if (state == State.STATE_DIR_NORMAL) {
            File[] files;
            try {
                files = dir.listFiles();
            } catch (Exception e) {
                return items;
            }

            items.clear();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    if (lhs.isDirectory() != rhs.isDirectory()) {
                        return lhs.isDirectory() ? -1 : 1;
                    }
                    return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }
                    }

            );
            for (int a = 0; a < files.length; a++) {
                File file = files[a];
                if (file.getName().indexOf('.') == 0) {
                    continue;
                }
                FileItem item = new FileItem();
                item.title = file.getName();
                item.file = file;
                if (file.isDirectory()) {
                    item.iconRes = R.mipmap.ic_directory;
                    item.subtitle = "Folder";
                } else {
                    String fname = file.getName();
                    String[] sp = fname.split("\\.");
                    item.type = sp.length > 1 ? sp[sp.length - 1] : "?";
                    item.subtitle = Utils.formatFileSize(file.length());
                    fname = fname.toLowerCase();
                    item.iconRes = 0;
                    if (fname.endsWith(".jpg") || fname.endsWith(".png") || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                        item.thumbFile = file;
                    }
                }
                items.add(item);
            }

            FileItem item = new FileItem();
            item.title = "..";

            if(mHistories.size()>0){
                HistoryEntry entry = mHistories.get(mHistories.size() - 1);
                if (entry.dir == null) {
                    item.subtitle = "Folder";
                } else {
                    item.subtitle = entry.dir.toString();
                }
            }else{
                item.subtitle = "Folder";
            }

            item.iconRes = R.mipmap.ic_directory;
            item.file = null;
            items.add(0, item);

            return items;
        } else {
            dealFileErrorState(state);
            return items;
        }
    }

    public void listFilesUnder(File file) {
        if (mListView != null) {
            mListView.setData(getFilesUnder(file));
            mListView.notifyDatasetChanged();
        }

    }


    public void enterMuitiSelectMode() {
        if (mListener != null) {
            mListener.onEnterMultiSelectMode();
        }
    }

    public void quitMultiSelectMode() {
        if (mListener != null) {
            mListener.onQuitMultiSelectMode();
        }
    }


    public void filesSelected(List<String> files) {
        if (mListener != null) {
            mListener.onFilesSelected(files);
        }
    }

    /**
     * 获取该file的状态。比如是否可读,是否是dir,是否有权限读取。等等。
     *
     * @param file
     * @return
     */
    public State getFileState(File file) {
        if (file.isDirectory()) {
            if (!file.canRead()) {
                if (file.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().toString())
                        || file.getAbsolutePath().startsWith("/sdcard")
                        || file.getAbsolutePath().startsWith("/mnt/sdcard")) {
                    if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                            && !Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {

                        String state = Environment.getExternalStorageState();
                        if (Environment.MEDIA_SHARED.equals(state)) {
                            return State.STATE_USB_ACTIVE;  //这个状态指?
                        } else {
                            return State.STATE_SDCARD_NOT_MOUNTED;  //sd卡未挂载
                        }
                    }
                }
                return State.STATE_DIR_ACCESS_ERROR;
            }

            File[] files;
            try {
                files = file.listFiles();
            } catch (Exception e) {
                return State.STATE_ERROR_UNKNOW;
            }
            if (files == null) {
                dealFileErrorState(State.STATE_ERROR_UNKNOW);
                return State.STATE_ERROR_UNKNOW;
            } else {
                return State.STATE_DIR_NORMAL;
            }
        } else {
            if (!file.canRead()) {
                return State.STATE_FILE_ACCESS_ERROR;
            } else {
                if (file.length() == 0) {
                    return State.STATE_FILE_ILLEGAL_LENGTH;
                }
                return State.STATE_FILE_NORMAL;
            }
        }
    }

    public void dealFileErrorState(State state) {
        if(state == State.STATE_DIR_ACCESS_ERROR){
            Toast.makeText(mContext,ERROR_DIR_ACCESS,Toast.LENGTH_LONG).show();
        } else if(state == State.STATE_FILE_ACCESS_ERROR){
            Toast.makeText(mContext,ERROR_FILE_ACCESS,Toast.LENGTH_LONG).show();
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
