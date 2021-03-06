package com.mozz.htmlnative.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.mozz.htmlnative.css.Background;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yang Tao, 17/4/18.
 */

public class HNDiv extends ViewGroup implements IBackgroundView {

    private static final String TAG = HNDiv.class.getSimpleName();

    private List<List<View>> mAllViews = new ArrayList<>();
    private List<Integer> mLineLength = new ArrayList<>();

    private BackgroundManager mBackgroundMgr;

    public HNDiv(Context context) {
        super(context);
        mBackgroundMgr = new BackgroundManager(this);
    }

    public HNDiv(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBackgroundMgr = new BackgroundManager(this);
    }

    public HNDiv(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBackgroundMgr = new BackgroundManager(this);
    }


    public HNDiv(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBackgroundMgr = new BackgroundManager(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int msWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int msHeight = MeasureSpec.getSize(heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        int width = 0;
        int height = getPaddingLeft();

        int length = getChildCount();

        int lineWidth = getPaddingLeft();
        int lineHeight = 0;

        boolean firstLine = true;

        for (int i = 0; i < length; i++) {
            View child = getChildAt(i);

            measureChild(child, widthMeasureSpec, heightMeasureSpec);

            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (childWidth + lineWidth + getPaddingRight() > msWidth) {
                width = Math.max(lineWidth + getPaddingRight(), childWidth + getPaddingLeft());
                height += lineHeight;

                lineWidth = childWidth + getPaddingLeft();
                lineHeight = childHeight;

                if (firstLine) {
                    firstLine = false;
                }
            } else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            if (i == length - 1) {
                width = Math.max(lineWidth, width);
                height += lineHeight;
            }
        }

        height = height + getPaddingBottom();

        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? msWidth : width, heightMode ==
                MeasureSpec.EXACTLY ? msHeight : height);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineLength.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        // 存储每一行所有的childView
        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();
        // 遍历所有的孩子
        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            // 如果已经需要换行
            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth + paddingLeft +
                    paddingRight > width) {
                // 记录这一行所有的View以及最大高度
                mLineLength.add(lineHeight);
                // 将当前行的childView保存，然后开启新的ArrayList保存下一行的childView
                mAllViews.add(lineViews);
                lineWidth = childWidth;// 重置行宽
                lineViews = new ArrayList<>();
                lineViews.add(child);
                lineHeight = childHeight;
            } else {
                /**
                 * 如果不需要换行，则累加
                 */
                lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
                lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
                lineViews.add(child);
            }
        }
        // 记录最后一行
        mLineLength.add(lineHeight);
        mAllViews.add(lineViews);

        int left = paddingLeft;
        int top = paddingTop;
        // 得到总行数
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            // 每一行的所有的views
            lineViews = mAllViews.get(i);
            // 当前行的最大高度
            lineHeight = mLineLength.get(i);

            // 遍历当前行所有的View
            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                //计算childView的left,top,right,bottom
                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);

                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            left = paddingLeft;
            top += lineHeight;
        }
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams
                .WRAP_CONTENT);
    }

    public void setHtmlBackground(Bitmap bitmap, Background background) {
        mBackgroundMgr.setHtmlBackground(bitmap, background);
    }

    @Override
    public void setBackground(Drawable background) {
        // don't support the background!! Use setHtmlBackground instead
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBackgroundMgr.onDraw(canvas);
        super.onDraw(canvas);

    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new MarginLayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }


}
