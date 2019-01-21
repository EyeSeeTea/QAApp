/*
 * Copyright (c) 2015.
 *
 * This file is part of Facility QA Tool App.
 *
 *  Facility QA Tool App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Facility QA Tool App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;

import org.eyeseetea.malariacare.data.database.utils.LanguageContextWrapper;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.repositories.UserAccountRepository;
import org.eyeseetea.malariacare.domain.usecase.LogoutUseCase;
import org.eyeseetea.malariacare.layout.dashboard.builder.AppSettingsBuilder;
import org.eyeseetea.malariacare.views.SimpleDividerItemDecoration;

/**
 * A {@link android.support.v7.app.AppCompatActivity} that presents a set of application settings
 * . On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final boolean ALWAYS_SIMPLE_PREFS = false;

    private static final String TAG = ".SettingsActivity";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        PreferencesState.getInstance().initalizateActivityDependencies();
    }


    /**
     * Sets the application languages and populate the language in the preference
     */
    private static void setLanguageOptions(Preference preference) {
        ListPreference listPreference = (ListPreference) preference;
        listPreference.setEntries(R.array.languages_strings);
        listPreference.setEntryValues(R.array.languages_codes);
    }


    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    //Remove the last text size option if the screen size is small.
    private static void filterTextSizeOptions(Preference preference) {
        if (!PreferencesState.getInstance().isLargeTextShown()) {
            ListPreference listPreference = (ListPreference) preference;
            CharSequence[] entries = removeLastItem(listPreference.getEntries());
            CharSequence[] values = removeLastItem(listPreference.getEntryValues());
            listPreference.setEntries(entries);
            listPreference.setEntryValues(values);
        }
    }

    //Returns the provided charSequence without the last position.
    private static CharSequence[] removeLastItem(CharSequence[] entries) {
        CharSequence[] newEntries = new CharSequence[4];
        for (int i = 0; i < entries.length - 1; i++) {
            newEntries[i] = entries[i];
        }
        return newEntries;
    }

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }


    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object value) {
                    String stringValue = value.toString();

                    if (preference instanceof ListPreference) {
                        // For list preferences, look up the correct display value in
                        // the preference's 'entries' list.
                        ListPreference listPreference = (ListPreference) preference;
                        int index = listPreference.findIndexOfValue(stringValue);

                        // Set the summary to reflect the new value.
                        preference.setSummary(
                                index >= 0
                                        ? listPreference.getEntries()[index]
                                        : null);

                    } else {
                        // For all other preferences, set the summary to the value's
                        // simple string representation.
                        preference.setSummary(stringValue);
                    }
                    return true;
                }
            };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.language_code))) {
            restartActivity();
        }

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            if (!isSimplePreferences(getActivity())) {
                return;
            }

            // In the simplified UI, fragments are not used at all and we instead
            // use the older PreferenceActivity APIs.

            // Add 'general' preferences.
            addPreferencesFromResource(R.xml.pref_general);


            PreferencesState.getInstance().initalizateActivityDependencies();

            // fitler the font options by screen size
            filterTextSizeOptions(
                    findPreference(getActivity().getString(R.string.font_sizes)));

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
            // their values. When their values change, their summaries are updated
            // to reflect the new value, per the Android Design guidelines.
            if (BuildConfig.translations) {
                setLanguageOptions(
                        findPreference(getActivity().getString(R.string.language_code)));
                bindPreferenceSummaryToValue(
                        findPreference(getActivity().getString(R.string.language_code)));
            }


            bindPreferenceSummaryToValue(
                    findPreference(getActivity().getString(R.string.font_sizes)));
            bindPreferenceSummaryToValue(findPreference(getString(R.string.dhis_max_items)));

            bindPreferenceSummaryToValue(findPreference(getString(R.string.monitoring_target)));

            Preference serverUrlPreference = findPreference(
                    getResources().getString(R.string.dhis_url));
            Preference userPreference = findPreference(
                    getResources().getString(R.string.dhis_user));
            Preference passwordPreference = findPreference(
                    getResources().getString(R.string.dhis_password));

            //Hide developer option if is not active in the json
            if (!AppSettingsBuilder.isDeveloperOptionsActive()) {
                getPreferenceScreen().removePreference(getPreferenceScreen().findPreference(
                        getResources().getString(R.string.developer_option)));
            }

            bindPreferenceSummaryToValue(serverUrlPreference);
            bindPreferenceSummaryToValue(userPreference);

            serverUrlPreference.setOnPreferenceClickListener(
                    new LoginRequiredOnPreferenceClickListener(getActivity()));
            userPreference.setOnPreferenceClickListener(
                    new LoginRequiredOnPreferenceClickListener(getActivity()));
            passwordPreference.setOnPreferenceClickListener(
                    new LoginRequiredOnPreferenceClickListener(getActivity()));

            PreferenceScreen preferenceScreen = getPreferenceScreen();

            if (BuildConfig.customFontHidden) {
                hideFontCustomisationOption(preferenceScreen);
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        //Reload changes into PreferencesState
        PreferencesState.getInstance().reloadPreferences();
    }

    @Override
    public void onBackPressed() {
        Class callerActivityClass = getCallerActivity();
        Intent returnIntent=new Intent(this,callerActivityClass);
        returnIntent.putExtra(getString(R.string.show_announcement_key), false);
        startActivity(returnIntent);
    }

    private Class getCallerActivity() {
        //FIXME Not working as it should the intent param is always null
        Intent creationIntent = getIntent();
        if (creationIntent == null) {
            return DashboardActivity.class;
        }
        Class callerActivity = (Class) creationIntent.getSerializableExtra(
                BaseActivity.SETTINGS_CALLER_ACTIVITY);
        if (callerActivity == null) {
            return DashboardActivity.class;
        }

        return callerActivity;
    }

    private static void hideFontCustomisationOption(@NonNull PreferenceScreen preferenceScreen) {
        Context context = preferenceScreen.getContext();

        Preference customizeFonts = preferenceScreen.findPreference(
                context.getString(R.string.customize_fonts));

        Preference fontSizes = preferenceScreen.findPreference(
                context.getString(R.string.font_sizes));

        preferenceScreen.removePreference(customizeFonts);
        preferenceScreen.removePreference(fontSizes);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        String currentLanguage = PreferencesState.getInstance().getCurrentLocale();
        Context context = LanguageContextWrapper.wrap(newBase, currentLanguage);
        super.attachBaseContext(context);
    }
}

/**
 * Listener that moves to the LoginActivity before changing DHIS config
 */
class LoginRequiredOnPreferenceClickListener implements Preference.OnPreferenceClickListener {

    private static final String TAG = "LoginPreferenceListener";

    /**
     * Reference to the activity so you can use this from the activity or the fragment
     */
    Activity activity;

    LoginRequiredOnPreferenceClickListener(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        new AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.settings_menu_logout))
                .setMessage(activity.getString(R.string.dialog_content_dhis_preference_login))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        logout();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
        Log.i(TAG, "Returning from dialog -> true");
        return true;
    }

    private void logout() {
        Log.d(TAG, "Logging out...");
        UserAccountRepository userAccountRepository = new UserAccountRepository(activity);
        LogoutUseCase logoutUseCase = new LogoutUseCase(userAccountRepository);

        logoutUseCase.execute(new LogoutUseCase.Callback() {
            @Override
            public void onLogoutSuccess() {
                Intent loginIntent = new Intent(activity, LoginActivity.class);
                activity.finish();
                activity.startActivity(loginIntent);
            }

            @Override
            public void onLogoutError(String message) {
                Log.e(TAG, message);
            }
        });
    }
}
