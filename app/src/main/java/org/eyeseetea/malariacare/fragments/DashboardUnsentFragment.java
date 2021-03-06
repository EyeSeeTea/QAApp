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

package org.eyeseetea.malariacare.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.eyeseetea.malariacare.DashboardActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OrgUnitDB;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelationDB;
import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.layout.adapters.dashboard.AssessmentUnsentAdapter;
import org.eyeseetea.malariacare.services.SurveyService;
import org.eyeseetea.malariacare.views.CustomTextView;
import org.eyeseetea.malariacare.views.filters.OrgUnitProgramFilterView;

import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class DashboardUnsentFragment extends ListFragment implements IModuleFragment {

    public static final String TAG = ".DetailsFragment";
    private SurveyReceiver surveyReceiver;
    private List<SurveyDB> surveys;
    protected AssessmentUnsentAdapter adapter;
    DashboardActivity dashboardActivity;

    OrgUnitProgramFilterView orgUnitProgramFilterView;
    FloatingActionButton startButton;
    TextView noSurveysText;
    ListView listView;

    public DashboardUnsentFragment() {
        this.surveys = new ArrayList();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        loadFilter();

        orgUnitProgramFilterView.setFilterType(OrgUnitProgramFilterView.FilterType.NON_EXCLUSIVE);

        orgUnitProgramFilterView.setFilterChangedListener(
                new OrgUnitProgramFilterView.FilterChangedListener() {
                    @Override
                    public void onProgramFilterChanged(ProgramDB selectedProgramFilter) {
                        reloadInProgressSurveys();
                        saveCurrentFilters();
                    }

                    @Override
                    public void onOrgUnitFilterChanged(OrgUnitDB selectedOrgUnitFilter) {
                        reloadInProgressSurveys();
                        saveCurrentFilters();
                    }
                });
        View view =  inflater.inflate(R.layout.assess_listview, null);

        noSurveysText = (TextView) view.findViewById(R.id.no_surveys);
        startButton = (FloatingActionButton) view.findViewById(R.id.start_button);
        return view;
    }

    private void saveCurrentFilters() {
        PreferencesState.getInstance().setProgramUidFilter(
                orgUnitProgramFilterView.getSelectedProgramFilter().getUid());
        PreferencesState.getInstance().setOrgUnitUidFilter(
                orgUnitProgramFilterView.getSelectedOrgUnitFilter().getUid());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initListView();
        registerForContextMenu(getListView());
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //Listen for data
        registerSurveysReceiver();
        super.onResume();
    }

    private void updateSelectedFilters() {
        if (orgUnitProgramFilterView == null) {
            loadFilter();
        }
        String programUidFilter = PreferencesState.getInstance().getProgramUidFilter();
        String orgUnitUidFilter = PreferencesState.getInstance().getOrgUnitUidFilter();
        orgUnitProgramFilterView.changeSelectedFilters(programUidFilter, orgUnitUidFilter);
    }

    private void loadFilter() {
        orgUnitProgramFilterView =
                (OrgUnitProgramFilterView) DashboardActivity.dashboardActivity
                        .findViewById(R.id.assess_org_unit_program_filter_view);
    }

    private void showOrHiddenButton(SurveyDB survey) {
        OrgUnitDB orgUnit = orgUnitProgramFilterView.getSelectedOrgUnitFilter();
        ProgramDB program = orgUnitProgramFilterView.getSelectedProgramFilter();
        if(orgUnit.getName().equals(
                PreferencesState.getInstance().getContext().getString(R.string.filter_all_org_units))
        || program.getName().equals(
                PreferencesState.getInstance().getContext().getString(R.string.filter_all_org_assessments))){
            startButton.setVisibility(View.VISIBLE);
            noSurveysText.setText(R.string.assess_no_surveys);
        }else if (survey != null || !OrgUnitProgramRelationDB.existProgramAndOrgUnitRelation(program.getId_program(), orgUnit.getId_org_unit())){
            startButton.setVisibility(View.INVISIBLE);
            noSurveysText.setText(R.string.survey_not_assigned_facility);
        }else{
            startButton.setVisibility(View.VISIBLE);
            noSurveysText.setText(R.string.assess_no_surveys);
        }
    }

    /**
     * Inits adapter.
     * Most of times is just an AssessmentAdapter.
     * In a version with several adapters in dashboard (like in 'mock' branch) a new one like the
     * one in session is created.
     */
    private void initAdapter() {
        this.adapter = new AssessmentUnsentAdapter(this.surveys, getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        dashboardActivity = (DashboardActivity) activity;
    }

    //Remove survey from the list and reload list.
    public void removeSurveyFromAdapter(SurveyDB survey) {
        adapter.remove(survey);
        adapter.notifyDataSetChanged();
        showOrHiddenList(adapter.getItemList().isEmpty());
    }

    @Override
    public void reloadData() {
        updateSelectedFilters();

        //Reload data using service
        Intent surveysIntent = new Intent(
                PreferencesState.getInstance().getContext().getApplicationContext(),
                SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD, SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    public void reloadToSend() {
        //Reload data using service
        Intent surveysIntent = new Intent(PreferencesState.getInstance().getContext().getApplicationContext()
                , SurveyService.class);
        surveysIntent.putExtra(SurveyService.SERVICE_METHOD,
                SurveyService.ALL_COMPLETED_SURVEYS_ACTION);
        PreferencesState.getInstance().getContext().getApplicationContext().startService(
                surveysIntent);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        unregisterSurveysReceiver();

        super.onPause();
    }

    /**
     * Initializes the listview component, adding a listener for swiping right
     */
    private void initListView() {
        listView = getListView();
        if (PreferencesState.getInstance().isVerticalDashboard()) {
            CustomTextView title = (CustomTextView) getActivity().findViewById(
                    R.id.titleInProgress);
            title.setText(adapter.getTitle());
        }
        setListAdapter(adapter);
    }

    /**
     * Register a survey receiver to load surveys into the listadapter
     */
    private void registerSurveysReceiver() {
        Log.d(TAG, "registerSurveysReceiver");

        if (surveyReceiver == null) {
            surveyReceiver = new SurveyReceiver();
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(surveyReceiver,
                    new IntentFilter(SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION));
        }
    }

    /**
     * Unregisters the survey receiver.
     * It really important to do this, otherwise each receiver will invoke its code.
     */
    public void unregisterSurveysReceiver() {
        Log.d(TAG, "unregisterSurveysReceiver");
        if (surveyReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(surveyReceiver);
            surveyReceiver = null;
        }
    }

    public void reloadInProgressSurveys() {
        List<SurveyDB> surveysInProgressFromService = (List<SurveyDB>) Session.popServiceValue(
                SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION);
        if(surveysInProgressFromService==null){
            return;
        }
        reloadSurveys(getSurveysByOrgUnitAndProgram(surveysInProgressFromService));
    }

    private List<SurveyDB> getSurveysByOrgUnitAndProgram(
            List<SurveyDB> surveysInProgressFromService) {
        List<SurveyDB> filteredSurveys = new ArrayList<>();

        for (SurveyDB survey : surveysInProgressFromService) {
            if (surveyHasOrgUnitFilter(survey) && surveyHasProgramFilter(survey)) {
                filteredSurveys.add(survey);
            }
        }

        return filteredSurveys;
    }

    private boolean surveyHasOrgUnitFilter(SurveyDB survey) {
        OrgUnitDB orgUnitFilter = orgUnitProgramFilterView.getSelectedOrgUnitFilter();

        return survey.getOrgUnit().getUid().equals(orgUnitFilter.getUid()) ||
                orgUnitFilter.getName().equals(
                        PreferencesState.getInstance().getContext().getString(
                                R.string.filter_all_org_units));
    }

    private boolean surveyHasProgramFilter(SurveyDB survey) {
        ProgramDB programFilter = orgUnitProgramFilterView.getSelectedProgramFilter();

        return survey.getProgram().getUid().equals(programFilter.getUid()) ||
                programFilter.getName().equals(
                        PreferencesState.getInstance().getContext().getString(
                                R.string.filter_all_org_assessments));
    }

    public void reloadSurveys(List<SurveyDB> newListSurveys) {
        if (newListSurveys != null) {
            Log.d(TAG, "refreshScreen (Thread: " + Thread.currentThread().getId() + "): "
                    + newListSurveys.size());
            this.surveys.clear();
            this.surveys.addAll(newListSurveys);
            this.adapter.notifyDataSetChanged();
            SurveyDB surveyDB=null;
            if(newListSurveys.size()>0) {
                surveyDB =newListSurveys.get(0);
            }
            showOrHiddenButton(surveyDB);
            showOrHiddenList(newListSurveys.isEmpty());
        }
    }

    private void showOrHiddenList(boolean hasSurveys) {
        if(hasSurveys){
            noSurveysText.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }else {
            listView.setVisibility(View.VISIBLE);
            noSurveysText.setVisibility(View.GONE);
        }
    }

    /**
     * Inner private class that receives the result from the service
     */
    private class SurveyReceiver extends BroadcastReceiver {
        private SurveyReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive");
            //Listening only intents from this method
            if (SurveyService.ALL_IN_PROGRESS_SURVEYS_ACTION.equals(intent.getAction())) {
                reloadInProgressSurveys();
            }
        }
    }


}