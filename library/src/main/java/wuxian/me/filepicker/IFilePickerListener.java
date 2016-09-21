package wuxian.me.filepicker;
import java.util.List;

/**
 * Created by wuxian on 1/9/2016.
 * <p>
 * 抽象出一个接口filePicker作为文件浏览器
 *
 * 一个文件浏览器关心的功能
 * 1 进入多选模式回调 此时可能要更新ui
 * 2 退出多选模式回调 理由同上
 * 3 判断当前是否在多选模式
 * 4 文件选中
 *
 * 并且文件浏览器
 * 1 不关心文件排列的形式(UI)及实现(impl)
 * 2 不关心进入文件夹及返回上一级的ui及实现
 * 3 不关心进入文件夹可能抛出的异常 ???
 */

public interface IFilePickerListener {

    void onEnterMuitiSelectMode();

    void onQuitMultiSelectMode();

    void onFilesSelected(List<String> files);  //每次有file被选中的时候 可能需要刷新一下ui

}
