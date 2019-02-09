package com.zhongjh.progresslibrary.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.zhongjh.progresslibrary.R;
import com.zhongjh.progresslibrary.engine.ImageEngine;
import com.zhongjh.progresslibrary.entity.MultiMedia;
import com.zhongjh.progresslibrary.entity.RecordingItem;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 这是返回图片（视频、录音）等文件后，显示的Layout
 * Created by zhongjh on 2018/10/17.
 */
public class MaskProgressLayout extends FrameLayout {

    public ViewHolder mViewHolder;          // 控件集合
    private ImageEngine mImageEngine;       // 图片加载方式
    private int audioProgressColor;                 // 音频 文件的进度条颜色

    public void setMaskProgressLayoutListener(MaskProgressLayoutListener listener) {
        mViewHolder.alfMedia.setListener(listener);
    }

    public MaskProgressLayout(@NonNull Context context) {
        this(context, null);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MaskProgressLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    /**
     * 初始化view
     */
    private void initView(AttributeSet attrs) {
        // 自定义View中如果重写了onDraw()即自定义了绘制，那么就应该在构造函数中调用view的setWillNotDraw(false).
        setWillNotDraw(false);

        mViewHolder = new ViewHolder(View.inflate(getContext(), R.layout.layout_mask_progress, this));

        // 获取系统颜色
        int defaultColor = 0xFF000000;
        int[] attrsArray = {R.attr.colorPrimary, R.attr.colorPrimaryDark, R.attr.colorAccent};
        TypedArray typedArray = getContext().obtainStyledAttributes(attrsArray);
        int colorPrimary = typedArray.getColor(0, defaultColor);
        int colorPrimaryDark = typedArray.getColor(0, defaultColor);
        int colorAccent = typedArray.getColor(0, defaultColor);

        // 获取自定义属性。
        TypedArray maskProgressLayoutStyle = getContext().obtainStyledAttributes(attrs, R.styleable.MaskProgressLayoutStyle);
        // 获取默认图片
        Drawable drawable = maskProgressLayoutStyle.getDrawable(R.styleable.MaskProgressLayoutStyle_album_thumbnail_placeholder);
        // 获取显示图片的类
        String imageEngineStr = maskProgressLayoutStyle.getString(R.styleable.MaskProgressLayoutStyle_imageEngine);
        // 获取最多显示多少个方框
        int imageCount = maskProgressLayoutStyle.getInteger(R.styleable.MaskProgressLayoutStyle_maxCount, 5);
        // 音频，删除按钮的颜色
        int audioDeleteColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioDeleteColor, colorPrimary);
        // 音频 文件的进度条颜色
        audioProgressColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioProgressColor, colorPrimary);
        // 音频 播放按钮的颜色
        int audioPlayColor = maskProgressLayoutStyle.getColor(R.styleable.MaskProgressLayoutStyle_audioPlayColor, colorPrimary);
        if (imageEngineStr == null) {
            throw new RuntimeException("必须定义image_engine属性，指定某个显示图片类");
        } else {
            Class<?> imageEngineClass;//完整类名
            try {
                imageEngineClass = Class.forName(imageEngineStr);
                mImageEngine = (ImageEngine) imageEngineClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mImageEngine == null) {
                throw new RuntimeException("image_engine找不到相关类");
            }
        }
        if (drawable == null) {
            drawable = getResources().getDrawable(R.color.thumbnail_placeholder);
        }
        // 初始化九宫格的控件
        mViewHolder.alfMedia.initConfig(mImageEngine, drawable, imageCount);
        // 设置上传音频等属性
        mViewHolder.imgRemoveRecorder.setColorFilter(audioDeleteColor);
        mViewHolder.numberProgressBar.setProgressTextColor(audioProgressColor);
        mViewHolder.numberProgressBar.setReachedBarColor(audioProgressColor);
        mViewHolder.tvRecorderTip.setTextColor(audioProgressColor);

        // 设置播放控件里面的播放按钮的颜色
        mViewHolder.playView.mViewHolder.imgPlay.setColorFilter(audioPlayColor);
        mViewHolder.playView.mViewHolder.tvCurrentProgress.setTextColor(audioProgressColor);

        maskProgressLayoutStyle.recycle();
        typedArray.recycle();
    }

    /**
     * 设置图片同时更新表格
     *
     * @param imagePaths 图片数据源
     */
    public void addImages(List<String> imagePaths) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (String string : imagePaths) {
            MultiMedia multiMedia = new MultiMedia(string, 0);
            multiMedias.add(multiMedia);
        }
        mViewHolder.alfMedia.addImageData(multiMedias);
    }

    /**
     * 设置视频地址
     */
    public void setVideo(List<String> videoPath) {
        ArrayList<MultiMedia> multiMedias = new ArrayList<>();
        for (String string : videoPath) {
            MultiMedia multiMedia = new MultiMedia(string, 1);
            multiMedias.add(multiMedia);
        }
        mViewHolder.alfMedia.addVideoData(multiMedias);

    }

    /**
     * 设置音频数据
     *
     * @param filePath 音频文件地址
     * @
     */
    public void setAudio(String filePath, int length) {
        MultiMedia multiMedia = new MultiMedia(filePath, 2);
        multiMedia.setViewHolder(mViewHolder);
        mViewHolder.alfMedia.addAudioData(multiMedia);

        // 显示上传中的音频
        mViewHolder.groupRecorderProgress.setVisibility(View.VISIBLE);
        mViewHolder.playView.setVisibility(View.GONE);

        // 初始化播放控件
        RecordingItem recordingItem = new RecordingItem();
        recordingItem.setFilePath(filePath);
        recordingItem.setLength(length);
        mViewHolder.playView.setData(recordingItem, audioProgressColor);
    }

    public static class ViewHolder {
        public View rootView;
        public AutoLineFeedLayout alfMedia;
        public NumberProgressBar numberProgressBar;
        public ImageView imgRemoveRecorder;
        public Group groupRecorderProgress;
        public PlayView playView;
        public TextView tvRecorderTip;

        public ViewHolder(View rootView) {
            this.rootView = rootView;
            this.alfMedia = rootView.findViewById(R.id.alfMedia);
            this.numberProgressBar = rootView.findViewById(R.id.numberProgressBar);
            this.imgRemoveRecorder = rootView.findViewById(R.id.imgRemoveRecorder);
            this.playView = rootView.findViewById(R.id.playView);
            this.groupRecorderProgress = rootView.findViewById(R.id.groupRecorderProgress);
            this.tvRecorderTip = rootView.findViewById(R.id.tvRecorderTip);
        }
    }
}
