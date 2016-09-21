/*
 * This is the source code of Telegram for Android v. 2.0.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2016.
 */

package wuxian.me.filepicker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import wuxian.me.filepicker.FilePickerImpl.FileItem;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import java.io.File;
import wuxian.me.filepicker.R;

public class DocumentView extends FrameLayout {
    private View mView;
    private SimpleDraweeView mFileIcon;
    private TextView mFileName;
    private TextView mFileExt;
    private TextView mFileDate;
    private ImageView mFileStatus;
    private CheckBox mCheckbox;
    private Context context;

    private PipelineDraweeControllerBuilder mControllerBuilder;

    private static Paint mPaint;
    private int icons[] = {
            R.mipmap.media_doc_blue,
            R.mipmap.media_doc_green,
            R.mipmap.media_doc_red,
            R.mipmap.media_doc_yellow
    };

    private boolean mNeedDivider;

    public DocumentView(Context context) {
        super(context);
        this.context = context;

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(0xffd9d9d9);
            mPaint.setStrokeWidth(1);
        }

        mView = LayoutInflater.from(context).inflate(R.layout.view_document, null, false);

        mFileExt = (TextView) mView.findViewById(R.id.tv_file_ext);
        //mFileIcon = (SimpleDraweeView) mView.findViewById(R.id.iv_file_icon);

        /*
        mControllerBuilder = Fresco.newDraweeControllerBuilder().setControllerListener(new ControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                mFileExt.setVisibility(INVISIBLE);
            }

            @Override
            public void onIntermediateImageSet(String id, ImageInfo imageInfo) {
            }

            @Override
            public void onIntermediateImageFailed(String id, Throwable throwable) {
            }

            @Override
            public void onFailure(String id, Throwable throwable) {
            }

            @Override
            public void onRelease(String id) {
            }
        });
        */

        mFileName = (TextView) mView.findViewById(R.id.tv_file_name);
        mFileStatus = (ImageView) mView.findViewById(R.id.iv_status);
        mFileStatus.setVisibility(INVISIBLE);
        mFileDate = (TextView) mView.findViewById(R.id.tv_file_date);

        mCheckbox = (CheckBox) mView.findViewById(R.id.checkbox);
        mCheckbox.setDrawableResource(R.mipmap.round_check2);
        mCheckbox.setVisibility(GONE);

        addView(mView, LayoutHelper.createFrame(context,LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 0, 0, 0));
    }

    private int getThumbForNameOrMime(String name, String mime) {
        if (name != null && name.length() != 0) {
            int color = -1;
            if (name.contains(".doc") || name.contains(".txt") || name.contains(".psd")) {
                color = 0;
            } else if (name.contains(".xls") || name.contains(".csv")) {
                color = 1;
            } else if (name.contains(".pdf") || name.contains(".ppt") || name.contains(".key")) {
                color = 2;
            } else if (name.contains(".zip") || name.contains(".rar") || name.contains(".ai") || name.contains(".mp3") || name.contains(".mov") || name.contains(".avi")) {
                color = 3;
            }
            if (color == -1) {
                int idx;
                String ext = (idx = name.lastIndexOf('.')) == -1 ? "" : name.substring(idx + 1);
                if (ext.length() != 0) {
                    color = ext.charAt(0) % icons.length;
                } else {
                    color = name.charAt(0) % icons.length;
                }
            }
            return icons[color];
        }
        return icons[0];
    }

    public void setFileItem(FileItem item){
        if(item == null){
            return;
        }


    }

    public void setTextAndValueAndTypeAndThumb(String text, String value, String type, File url, int resourceId) {
        mFileName.setText(text);
        mFileDate.setText(value);

        if (type != null) {
            mFileExt.setVisibility(VISIBLE);
            mFileExt.setText(type);
        } else {
            mFileExt.setVisibility(INVISIBLE);
        }

        if (resourceId == 0) {  //设置placeholder
            //mFileIcon.setImageDrawable(context.getResources().getDrawable(getThumbForNameOrMime(text, type)));
            //mFileIcon.getHierarchy().setPlaceholderImage(getThumbForNameOrMime(text, type));
        } else {
            //mFileIcon.setImageDrawable(context.getResources().getDrawable(resourceId));
            //mFileIcon.getHierarchy().setPlaceholderImage(resourceId);
        }

        if (url != null && url.exists()) {
            Uri uri = Uri.fromFile(url);
            ////mFileIcon.setImageURI(uri); //Not working --> from sourcecode
            ////mFileIcon.setController(mControllerBuilder.setUri(uri).build());  //Todo:拿缩略图

        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setChecked(boolean checked, boolean animated) {
        if (mCheckbox.getVisibility() != VISIBLE) {
            mCheckbox.setVisibility(VISIBLE);
        }

        mCheckbox.setChecked(checked, animated);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Utils.dp(getContext(),56) + (mNeedDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mNeedDivider) {
            canvas.drawLine(Utils.dp(getContext(),72), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, mPaint);
        }
    }
}
