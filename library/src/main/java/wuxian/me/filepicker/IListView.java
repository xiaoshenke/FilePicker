package wuxian.me.filepicker;

import java.util.List;

/**
 * Created by wuxian on 1/9/2016.
 * 我不管这是不是个listview 但我认为这是一个长得像listview的东西
 * 我调用setData就是显示全部
 * 调用addData就是往里面加数据
 */

public interface IListView {
    void setData(List<FileItem> datas);

    void addData(List<FileItem> datas);

    void notifyDatasetChanged();

    boolean inMultiSelectMode();
}
