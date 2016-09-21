package wuxian.me.filepicker;
import java.util.List;

/**
 * Created by wuxian on 1/9/2016.
 * <p> Interface of a FilePickerListener
 *
 * A FilePickerListener should care about these functions
 * 1 enter multiselect mode, maybe you should change your UI
 * 2 quit multiselect mode, maybe you should change your UI
 * 3 be notified after have selected some file or files
 *
 * And it does not care about
 * 1 how the files is showed
 * 2 does not need to be notified enterring a child directory or going back to parent directory
 * 3 does not need to handle exceptions
 *
 */

public interface IFilePickerListener {

    void onEnterMultiSelectMode();

    void onQuitMultiSelectMode();

    void onFilesSelected(List<String> files);

}
