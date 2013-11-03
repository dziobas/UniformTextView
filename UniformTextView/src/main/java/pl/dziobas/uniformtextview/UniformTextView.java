/*
 * Copyright (C) 2013, Paweł Zięba
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package pl.dziobas.uniformtextview;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Layout;
import android.text.StaticLayout;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView with possibly the smallest width for preferred line number.
 */
public class UniformTextView extends TextView {
    private static final int THRESHOLD = 5;
    private float mLineSpacingAdd;
    private float mLineSpacingMult;
    private int mMinimizedWidht;
    private int mPrefLineNumber;

    public UniformTextView(Context context) {
        this(context, null, 0);
    }

    public UniformTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UniformTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.UniformTextView);
        if (typedArray != null) {
            mPrefLineNumber = typedArray.getInteger(R.styleable.UniformTextView_prefLineNumber, 2);
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        if (mMinimizedWidht <= 0) {
            //minimize only once
            mMinimizedWidht = minimizeWidth(width);
        }
        if (mMinimizedWidht > 0) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMinimizedWidht, MeasureSpec.getMode(widthMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        mLineSpacingAdd = add;
        mLineSpacingMult = mult;
        super.setLineSpacing(add, mult);
    }

    /**
     * Find minimum possible width for preffered lines count with binary search.
     *
     * @param maxWidth Maximum width for the view
     * @return Minimal width
     */
    private int minimizeWidth(int maxWidth) {
        int currentLineCount = getLineCount(maxWidth);
        if (currentLineCount > mPrefLineNumber) {
            return maxWidth;
        }

        int diff = maxWidth;
        int width = maxWidth;
        int bestUpperBound = maxWidth;
        int last;
        do {
            boolean canShrink = currentLineCount <= mPrefLineNumber;
            last = width;
            if (canShrink) {
                bestUpperBound = width;
                width = width - diff / 2;
            } else {
                width = width + diff / 2;
            }
            diff = Math.abs(last - width);
            currentLineCount = getLineCount(width);
        } while (diff > THRESHOLD);

        return bestUpperBound;
    }

    private int getLineCount(int width) {
        final CharSequence text = getText();
        StaticLayout layout = new StaticLayout(text != null ? text : "", getPaint(), width,
                Layout.Alignment.ALIGN_NORMAL, mLineSpacingMult, mLineSpacingAdd, true);
        return layout.getLineCount();
    }
}
