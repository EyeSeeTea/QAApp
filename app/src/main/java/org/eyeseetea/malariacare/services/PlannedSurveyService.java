package org.eyeseetea.malariacare.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.utils.Session;
import org.eyeseetea.malariacare.data.database.utils.planning.PlannedItemBuilder;
import org.eyeseetea.malariacare.data.database.utils.services.PlannedServiceBundle;


public class PlannedSurveyService extends IntentService {

    /**
     * Constant added to the intent in order to reuse the service for different 'methods'
     */
    public static final String SERVICE_METHOD = "serviceMethod";

    /**
     * Name of the parameter that holds every survey that goes into the planned tab
     */
    public static final String PLANNED_SURVEYS_ACTION =
            "org.eyeseetea.malariacare.services.SurveyService.PLANNED_SURVEYS_ACTION";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public PlannedSurveyService(String name) {
        super(name);
    }

    public PlannedSurveyService() {
        super(PlannedSurveyService.class.getSimpleName());
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent.getStringExtra(SERVICE_METHOD).equals(PLANNED_SURVEYS_ACTION)) {
            reloadPlannedSurveys();
        }
    }

    private void reloadPlannedSurveys() {
        Log.d(getClass().getName(), "reloadPlanningSurveys");
        PlannedServiceBundle plannedServiceBundle = new PlannedServiceBundle();
        plannedServiceBundle.setPlannedItems(PlannedItemBuilder.getInstance().buildPlannedItems());
        plannedServiceBundle.addModelList(OrgUnit.class.getName(), OrgUnit.getAllOrgUnit());
        plannedServiceBundle.addModelList(Program.class.getName(), Program.getAllPrograms());
        Session.putServiceValue(PLANNED_SURVEYS_ACTION, plannedServiceBundle);
        //Returning result to anyone listening
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PLANNED_SURVEYS_ACTION));
    }
}
