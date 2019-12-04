/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.systemui;

import android.content.Context;
import android.testing.LeakCheck;
import android.testing.TestableContext;
import android.view.Display;

public class SysuiTestableContext extends TestableContext {
    public SysuiTestableContext(Context base) {
        super(base);
        setTheme(R.style.Theme_SystemUI);
    }

    public SysuiTestableContext(Context base, LeakCheck check) {
        super(base, check);
        setTheme(R.style.Theme_SystemUI);
    }

    @Override
    public Context createDisplayContext(Display display) {
        if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        }

        SysuiTestableContext context =
                new SysuiTestableContext(getBaseContext().createDisplayContext(display));
        return context;
    }
}
