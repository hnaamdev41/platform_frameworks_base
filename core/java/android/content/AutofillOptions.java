/*
 * Copyright (C) 2018 The Android Open Source Project
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
package android.content;

import android.annotation.NonNull;
import android.annotation.TestApi;
import android.app.ActivityThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.autofill.AutofillManager;

import java.io.PrintWriter;

/**
 * Autofill options for a given package.
 *
 * <p>This object is created by the Autofill System Service and passed back to the app when the
 * application is created.
 *
 * @hide
 */
@TestApi
public final class AutofillOptions implements Parcelable {

    private static final String TAG = AutofillOptions.class.getSimpleName();

    /**
     * Logging level for {@code logcat} statements.
     */
    public final int loggingLevel;

    /**
     * Whether compatibility mode is enabled for the package.
     */
    public final boolean compatModeEnabled;

    /**
     * Whether package is whitelisted for augmented autofill.
     */
    public boolean augmentedEnabled;
    // TODO(b/123100824): add (optional) list of activities

    public AutofillOptions(int loggingLevel, boolean compatModeEnabled) {
        this.loggingLevel = loggingLevel;
        this.compatModeEnabled = compatModeEnabled;
    }

    /**
     * @hide
     */
    @TestApi
    public static AutofillOptions forWhitelistingItself() {
        final ActivityThread at = ActivityThread.currentActivityThread();
        if (at == null) {
            throw new IllegalStateException("No ActivityThread");
        }

        final String packageName = at.getApplication().getPackageName();

        if (!"android.autofillservice.cts".equals(packageName)) {
            Log.e(TAG, "forWhitelistingItself(): called by " + packageName);
            throw new SecurityException("Thou shall not pass!");
        }

        final AutofillOptions options = new AutofillOptions(
                AutofillManager.FLAG_ADD_CLIENT_VERBOSE, /* compatModeAllowed= */ true);
        options.augmentedEnabled = true;
        // Always log, as it's used by test only
        Log.i(TAG, "forWhitelistingItself(" + packageName + "): " + options);

        return options;
    }

    @Override
    public String toString() {
        return "AutofillOptions [loggingLevel=" + loggingLevel + ", compatMode="
                + compatModeEnabled + ", augmentedEnabled=" + augmentedEnabled + "]";
    }

    /** @hide */
    public void dumpShort(@NonNull PrintWriter pw) {
        pw.print("logLvl="); pw.print(loggingLevel);
        pw.print(", compatMode="); pw.print(compatModeEnabled);
        pw.print(", augmented="); pw.print(augmentedEnabled);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(loggingLevel);
        parcel.writeBoolean(compatModeEnabled);
        parcel.writeBoolean(augmentedEnabled);
    }

    public static final Parcelable.Creator<AutofillOptions> CREATOR =
            new Parcelable.Creator<AutofillOptions>() {

                @Override
                public AutofillOptions createFromParcel(Parcel parcel) {
                    final int loggingLevel = parcel.readInt();
                    final boolean compatMode = parcel.readBoolean();
                    final AutofillOptions options = new AutofillOptions(loggingLevel, compatMode);
                    options.augmentedEnabled = parcel.readBoolean();
                    return options;
                }

                @Override
                public AutofillOptions[] newArray(int size) {
                    return new AutofillOptions[size];
                }
    };
}
