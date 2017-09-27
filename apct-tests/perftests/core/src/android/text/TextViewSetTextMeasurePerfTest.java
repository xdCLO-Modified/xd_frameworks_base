/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package android.text;

import static android.view.View.MeasureSpec.AT_MOST;
import static android.view.View.MeasureSpec.UNSPECIFIED;

import android.graphics.Canvas;
import android.perftests.utils.BenchmarkState;
import android.perftests.utils.PerfStatusReporter;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.text.NonEditableTextGenerator.TextType;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.widget.TextView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Performance test for multi line, single style {@link StaticLayout} creation/draw.
 */
@LargeTest
@RunWith(Parameterized.class)
public class TextViewSetTextMeasurePerfTest {

    private static final boolean[] BOOLEANS = new boolean[]{false, true};

    // keep it one char longer than 32 so that 32 chars can fit into one line
    private static final int LINE_LENGTH = 33;

    @Rule
    public PerfStatusReporter mPerfStatusReporter = new PerfStatusReporter();

    @Parameterized.Parameters(name = "cached={3},{1} chars,{0}")
    public static Collection cases() {
        final List<Object[]> params = new ArrayList<>();
        for (int length : new int[]{32, 64, 128, 256, 512}) {
            for (boolean cached : BOOLEANS) {
                for (boolean multiLine : BOOLEANS) {
                    for (TextType textType : TextType.values()) {
                        params.add(new Object[]{
                                (multiLine ? "MultiLine" : "SingleLine") + "," + textType.name(),
                                length, textType, cached, multiLine});
                    }
                }
            }
        }
        return params;
    }

    private final int mLineWidth;
    private final int mLength;
    private final TextType mTextType;
    private final boolean mCached;
    private final boolean mMultiLine;
    private final TextPaint mTextPaint;

    public TextViewSetTextMeasurePerfTest(String label, int length, TextType textType,
            boolean cached, boolean multiLine) {
        mLength = length;
        mTextType = textType;
        mCached = cached;
        mMultiLine = multiLine;
        mTextPaint = new TextPaint();
        mTextPaint.setTextSize(10);
        final CharSequence text = createRandomText(LINE_LENGTH);
        mLineWidth = mMultiLine ? (int) mTextPaint.measureText(text.toString()) : Integer.MAX_VALUE;
    }

    /**
     * Measures the time to setText and measure for a {@link TextView}.
     */
    @Test
    public void timeCreate() throws Exception {
        final BenchmarkState state = mPerfStatusReporter.getBenchmarkState();

        state.pauseTiming();
        Canvas.freeTextLayoutCaches();
        final CharSequence text = createRandomText(mLength);
        final TextView textView = new TextView(InstrumentationRegistry.getTargetContext());
        textView.setText(text);
        state.resumeTiming();

        while (state.keepRunning()) {
            state.pauseTiming();
            textView.setTextLocale(Locale.UK);
            textView.setTextLocale(Locale.US);
            if (!mCached) Canvas.freeTextLayoutCaches();
            state.resumeTiming();

            textView.setText(text);
            textView.measure(AT_MOST | mLineWidth, UNSPECIFIED);
        }
    }

    /**
     * Measures the time to draw for a {@link TextView}.
     */
    @Test
    public void timeDraw() throws Exception {
        final BenchmarkState state = mPerfStatusReporter.getBenchmarkState();

        state.pauseTiming();
        Canvas.freeTextLayoutCaches();
        final RenderNode node = RenderNode.create("benchmark", null);
        final CharSequence text = createRandomText(mLength);
        final TextView textView = new TextView(InstrumentationRegistry.getTargetContext());
        textView.setText(text);
        state.resumeTiming();

        while (state.keepRunning()) {

            state.pauseTiming();
            final DisplayListCanvas canvas = node.start(1200, 200);
            int save = canvas.save();
            textView.setTextLocale(Locale.UK);
            textView.setTextLocale(Locale.US);
            if (!mCached) Canvas.freeTextLayoutCaches();
            state.resumeTiming();

            textView.draw(canvas);

            state.pauseTiming();
            canvas.restoreToCount(save);
            node.end(canvas);
            state.resumeTiming();
        }
    }

    private CharSequence createRandomText(int length) {
        return new NonEditableTextGenerator(new Random(0))
                .setSequenceLength(length)
                .setCreateBoring(false)
                .setTextType(mTextType)
                .build();
    }
}
