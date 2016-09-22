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
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import java.io.File;
import wuxian.me.filepicker.R;

/**
 * DocumentView --> SharedDocumentCell
 */
public class DocumentView extends FrameLayout {
    private static final String TAG = "DocView";
    private static Paint mPaint;
    private int icons[] = {
            R.mipmap.media_doc_blue,
            R.mipmap.media_doc_green,
            R.mipmap.media_doc_red,
            R.mipmap.media_doc_yellow
    };

    private View mView;
    private Context mContext;

    private PipelineDraweeControllerBuilder mControllerBuilder;
    private SimpleDraweeView fileIcon;  //图标 可能从本地load 也可能是一个网络url
    private TextView fileType;          //如果该file是一个已知类型比如pdf word,显示该类型

    private TextView fileTitle;
    private TextView fileSubTitle;
    private CheckBox checkbox;

    private boolean needDivider;

    public DocumentView(Context mContext) {
        super(mContext);
        this.mContext = mContext;

        if (mPaint == null) {
            mPaint = new Paint();
            mPaint.setColor(0xffd9d9d9);
            mPaint.setStrokeWidth(1);
        }

        mView = LayoutInflater.from(mContext).inflate(R.layout.view_document, null, false);

        fileType = (TextView) mView.findViewById(R.id.tv_file_type);
        fileTitle = (TextView) mView.findViewById(R.id.tv_file_title);
        fileSubTitle = (TextView) mView.findViewById(R.id.tv_file_subtitle);

        checkbox = (CheckBox) mView.findViewById(R.id.checkbox);
        checkbox.setDrawableResource(R.mipmap.ic_round_check);
        checkbox.setVisibility(GONE);

        fileIcon = (SimpleDraweeView) mView.findViewById(R.id.iv_file_icon);


        mControllerBuilder = Fresco.newDraweeControllerBuilder().setControllerListener(new ControllerListener<ImageInfo>() {
            @Override
            public void onSubmit(String id, Object callerContext) {
            }

            @Override
            public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                fileType.setVisibility(INVISIBLE);
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

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP);
        addView(mView, layoutParams);
    }

    private int getThumbResForTitle(String name) {
        Log.d(TAG,"getThumbResForTitle: "+name);
        int color = -1;
        if (!TextUtils.isEmpty(name)) {

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

        } else {
            color = 0;
        }
        Log.d(TAG,"return position:"+color+"'s color");
        return icons[color];
    }

    public void setViewByItem(FileItem item){
        if(item == null){
            return;
        }

        fileTitle.setText(item.title);
        fileSubTitle.setText(item.subtitle);


        if(item.iconRes != 0){  //set placeholder
            fileIcon.setImageDrawable(mContext.getResources().getDrawable(item.iconRes));
            fileIcon.getHierarchy().setPlaceholderImage(item.iconRes);
        }else{
            fileIcon.setImageDrawable(mContext.getResources().getDrawable(getThumbResForTitle(item.title)));
            fileIcon.getHierarchy().setPlaceholderImage(getThumbResForTitle(item.title));
        }

        Uri uri = getThumbnailFromFile(item.thumbFile);
        if(uri != null){  //load thumbnail icon by url
            fileIcon.setController(mControllerBuilder.setUri(uri).build());
        }

        if(!TextUtils.isEmpty(item.type)){
            fileType.setText(item.type);
            fileType.setVisibility(VISIBLE);
        }else{
            fileType.setVisibility(INVISIBLE);
        }

        setChecked(item.isChecked,false);
    }

    private Uri getThumbnailFromFile(File file){
        //Todo: to be implemented
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setChecked(boolean checked, boolean animated) {
        if (checkbox.getVisibility() != VISIBLE) {
            checkbox.setVisibility(VISIBLE);
        }

        checkbox.setChecked(checked, animated);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Utils.dp(getContext(), 56) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(Utils.dp(getContext(),72), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, mPaint);
        }
    }
}
