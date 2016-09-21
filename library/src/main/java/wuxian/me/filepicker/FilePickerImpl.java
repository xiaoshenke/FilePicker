package wuxian.me.filepicker;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import wuxian.me.filepicker.view.TeleAndroidUtils;

/**
 * Created by wuxian on 1/9/2016.
 *
 * 文件浏览器接口实现者
 * 1 实现列出当前文件的功能 ui展示交给IListview
 * 2 实现抛出文件浏览异常的功能 ui展示?
 *
 */

public class FilePickerImpl {

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

    public FilePickerImpl(IFilePickerListener listener) {
        mListener = listener;
    }


    private IListView mListView;

    public void setListview(IListView listview) {
        mListView = listview;
    }

    private String getSubtitleOfPath(String path) {
        try {
            StatFs stat = new StatFs(path);
            long total = (long) stat.getBlockCount() * (long) stat.getBlockSize();
            long free = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize();
            if (total == 0) {
                return "";
            }
            return "free " + TeleAndroidUtils.formatFileSize(free) + " of " + TeleAndroidUtils.formatFileSize(total);
        } catch (Exception e) {

        }
        return path;
    }

    /**
     * 拿到sdk卡路径下的文件
     *
     * @return
     */
    @SuppressLint("NewApi")
    private List<FileItem> getRootItems() {

        List<FileItem> items = new ArrayList<>();
        items.clear();

        HashSet<String> paths = new HashSet<>();
        String state = Environment.getExternalStorageState();

        if (state.equals(Environment.MEDIA_MOUNTED) || state.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            FileItem item = new FileItem();
            if (Environment.isExternalStorageRemovable()) {
                item.title = "SdCard";
                item.icon = R.mipmap.ic_external_storage;
            } else {
                item.title = "InternalStorage";
                item.icon = R.mipmap.ic_storage;
            }

            String defaultPath = Environment.getExternalStorageDirectory().getPath();

            item.subtitle = getSubtitleOfPath(defaultPath);
            item.file = Environment.getExternalStorageDirectory();
            items.add(item);
            paths.add(defaultPath);
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
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
                                item.icon = R.mipmap.ic_external_storage;
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
        fs.icon = R.mipmap.ic_directory;
        fs.file = new File("/");
        items.add(fs);

        return items;

        //Todo:列出telegram目录
        // Todo:列出gallery

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
                    item.icon = R.mipmap.ic_directory;
                    item.subtitle = "Folder";
                } else {
                    String fname = file.getName();
                    String[] sp = fname.split("\\.");
                    item.ext = sp.length > 1 ? sp[sp.length - 1] : "?";
                    item.subtitle = TeleAndroidUtils.formatFileSize(file.length());
                    fname = fname.toLowerCase();
                    item.icon = 0;
                    if (fname.endsWith(".jpg") || fname.endsWith(".png") || fname.endsWith(".gif") || fname.endsWith(".jpeg")) {
                        item.thumb = file;
                        Log.e("test", "item.icon is 0 and thumb is " + item.thumb);
                    }
                }
                items.add(item);
            }

            FileItem item = new FileItem();
            item.title = "..";

        /* //Todo 赋值subtitle
        if(history.size()>0){
            HistoryEntry entry = history.get(history.size() - 1);
            if (entry.dir == null) {
                item.subtitle = "Folder";
            } else {
                item.subtitle = entry.dir.toString();
            }
        }else{
            item.subtitle = "Folder";
        }*/

            item.icon = R.mipmap.ic_directory;
            item.file = null;
            items.add(0, item);

            return items;
        } else {
            onFileState(state);
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
            mListener.onEnterMuitiSelectMode();
        }

    }


    public void quitMultiSelectMode() {
        if (mListener != null) {
            mListener.onQuitMultiSelectMode();
        }

    }

    public boolean inMultiSelectMode() {
        return mListView.isInMultiSelectMode();
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
                onFileState(State.STATE_ERROR_UNKNOW);
                return State.STATE_ERROR_UNKNOW;
            } else {
                return State.STATE_DIR_NORMAL;
            }
        } else {
            if (!file.canRead()) {  //Todo: 文件过大?
                return State.STATE_FILE_ACCESS_ERROR;
            } else {
                if (file.length() == 0) {
                    return State.STATE_FILE_ILLEGAL_LENGTH;
                }
                return State.STATE_FILE_NORMAL;
            }
        }
    }

    public void onFileState(State state) {
        //Todo
        if (mListener != null) {
            // mListener.filesSelected(state);
        }
    }


}
