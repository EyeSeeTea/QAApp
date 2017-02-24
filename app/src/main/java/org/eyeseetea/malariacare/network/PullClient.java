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

package org.eyeseetea.malariacare.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.databind.JsonNode;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.remote.SdkController;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by idelcano on 04/04/2016.
 */
public class PullClient {

    private static final String TAG=".PullClient";

    Context applicationContext;
    NetworkUtils networkUtils;

    public static final String EVENT = "event";
    public static final String NO_EVENT_FOUND="NO_EVENT_FOUND";

    public PullClient(Context applicationContext) {
        this.applicationContext = applicationContext;
        networkUtils=new NetworkUtils(applicationContext);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        networkUtils.setDhisServer(sharedPreferences.getString(applicationContext.getResources().getString(R.string.dhis_url), ""));
        networkUtils.setUser(sharedPreferences.getString(applicationContext.getString(R.string.dhis_user), ""));
        networkUtils.setPassword(sharedPreferences.getString(applicationContext.getString(R.string.dhis_password), ""));
    }

    /**
     * Find the last survey in the server for that orgunit and program (given a program) in the last month from now.
     *
     * @param orgUnit
     * @param program
     * @return
     */
    public EventExtended getLastEventInServerWith(OrgUnit orgUnit, Program program){
        EventExtended  lastEventInServer=null;
        Date oneMonthAgo = getOneMonthAgo();

        //Lets for a last event with that orgunit/program
        String data = QueryFormatterUtils.getInstance().prepareLastEventData(orgUnit.getUid(), program.getUid(), oneMonthAgo);
        try {
            JSONObject response = networkUtils.getData(data);
            JsonNode jsonNode=networkUtils.toJsonNode(response);
            List<EventExtended> eventsFromThatDate= SurveyChecker.getEvents(jsonNode);
            for(EventExtended event:eventsFromThatDate){
                //First event or events without date so far
                if(lastEventInServer==null){
                    lastEventInServer=event;
                    continue;
                }

                //Update event only if it comes afterwards
                Date lastEventInServerEventDate = lastEventInServer.getEventDate();
                Date eventDate = event.getEventDate();

                if(eventDate.after(lastEventInServerEventDate)){
                    lastEventInServer=event;
                }
            }
        }catch (Exception ex){
            Log.e(TAG,String.format("Cannot read last event from server with orgunit:%s | program:%s",orgUnit.getUid(),program.getUid()));
            ex.printStackTrace();
        }

        return lastEventInServer;
    }

    private Date getOneMonthAgo(){
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.MONTH,-1);
        return calendar.getTime();
    }

}
