/*
 * Copyright (c) 2016.
 *
 * This file is part of QA App.
 *
 *  Health Network QIS App is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Health Network QIS App is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.eyeseetea.malariacare.data.database.utils.monitor.facilities;

import android.content.Context;
import android.util.Log;
import android.webkit.WebView;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.data.database.model.SurveyDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by idelcano on 23/08/2016.
 */
public class FacilityTableBuilderByOrgUnit extends  FacilityTableBuilderBase {
    public static final String JAVASCRIPT_UPDATE_TABLE = "javascript:buildTablesPerOrgUnit('%s',%s)";
    private static final String TAG=".FacilityTableBuilderOU";
    Map<ProgramDB,FacilityTableDataByOrgUnit> facilityTableDataMap;
    public static final String JAVASCRIPT_SHOW = "javascript:renderPieChartsByOrgUnit()";
    /**
     * Default constructor
     *
     * @param surveys
     */
    public FacilityTableBuilderByOrgUnit(List<SurveyDB> surveys) {
        super(surveys);
        this.facilityTableDataMap = new HashMap<>();
    }

    /**
     * Build table data from surveys
     * @param surveys
     * @return
     */
    private void build(List<SurveyDB> surveys){
        for(SurveyDB survey:surveys){

            //Get right table
            FacilityTableDataByOrgUnit facilityTableDataByOrgUnit= facilityTableDataMap.get(survey.getProgram());

            //Init entry first time of a program
            if(facilityTableDataByOrgUnit==null){
                facilityTableDataByOrgUnit=new FacilityTableDataByOrgUnit(survey.getProgram(), survey.getOrgUnit());
                facilityTableDataMap.put(survey.getProgram(),facilityTableDataByOrgUnit);
            }


            //Add survey to that table
            facilityTableDataByOrgUnit.addSurvey(survey);
        }
    }

    /**
     * Adds calculated entries to the given webView
     * @param webView
     */
    public void addDataInChart(WebView webView){
        //Build tables
        build(surveys);
        //Inyect tables in view
        for(Map.Entry<ProgramDB,FacilityTableDataByOrgUnit> tableEntry: facilityTableDataMap.entrySet()){
            ProgramDB program=tableEntry.getKey();
            FacilityTableDataByOrgUnit facilityTableData=tableEntry.getValue();
            inyectDataInChart(webView, String.valueOf(program.getUid()), facilityTableData.getAsJSON());
        }

    }

    public static void showFacilities(WebView webView) {
        Log.d(TAG, JAVASCRIPT_SHOW);
        webView.loadUrl(String.format(JAVASCRIPT_SHOW));
    }

    void inyectDataInChart(WebView webView, String id, String json) {
        //Inyect in browser
        String updateChartJS=String.format(JAVASCRIPT_UPDATE_TABLE,id,json);
        Log.d(TAG, updateChartJS);
        webView.loadUrl(updateChartJS);
    }
}
