package wuxian.me.filepicker;

import java.util.List;

/**
 * Created by wuxian on 1/9/2016.
 * 我不管实现是listview还是recyclerview 我只认为这是一个长得像listview的东西
 *    当我我调用setData就是加载全部数据,调用addData就是往里面塞数据
 */

public interface IListView {
    void setData(List<FileItem> datas);

    void addData(List<FileItem> datas);

    void notifyDatasetChanged();

    boolean isInMultiSelectMode();
}
