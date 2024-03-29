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

import org.eyeseetea.malariacare.domain.entity.ServerClassification;
import org.eyeseetea.malariacare.domain.entity.Survey;

import java.util.ArrayList;
import java.util.List;

public class FacilityColumnData {
    private List<Survey> surveys;
    private ServerClassification serverClassification;

    public FacilityColumnData(ServerClassification serverClassification){
        this.serverClassification = serverClassification;
        surveys=new ArrayList<>();
    }

    public void addSurvey(Survey survey){
        surveys.add(survey);
    }

    public String getAsJSON(){
        if(!hasSurveys()){
            return "null";
        }
        String jsonObject="[";
        for(Survey survey:surveys){
            if (serverClassification == ServerClassification.COMPETENCIES){
                jsonObject+="{\"id\":'"+survey.getSurveyUid() + "',\"competency\":" +  survey.getCompetency().getId()+"},";
            } else {
                jsonObject+="{\"id\":'"+survey.getSurveyUid() + "',\"score\":" +  survey.getScore().getScore()+"},";
            }
        }
        jsonObject=jsonObject.substring(0,jsonObject.lastIndexOf(","));
        jsonObject= jsonObject +"]";
        return jsonObject;
    }

    private boolean hasSurveys(){
        return surveys!=null && surveys.size()>0;
    }
}
