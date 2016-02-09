package org.eyeseetea.malariacare.test.pull;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.eyeseetea.malariacare.LoginActivity;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.Program;
import org.eyeseetea.malariacare.database.utils.PopulateDB;
import org.eyeseetea.malariacare.test.utils.SDKTestUtils;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitAttributeValue;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.HNQIS_DEV_STAGING;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_NO_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_USERNAME_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.TEST_PASSWORD_WITH_PERMISSION;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.login;
import static org.eyeseetea.malariacare.test.utils.SDKTestUtils.waitForPull;

/**
 * Created by idelcano on 8/02/16.
 */
@RunWith(AndroidJUnit4.class)
public class PullTranslationSDKToAppDBTest {

    private final String ATTRIBUTE_PRODUCTIVITY_CODE="OUProductivity";
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(
            LoginActivity.class);

    @BeforeClass
    public static void setupClass(){
        PopulateDB.wipeDatabase();
    }

    @Before
    public void setup(){
        PopulateDB.wipeDatabase();
    }

    @Test
    public void pullTranslationSDKToAppDB(){
        login(HNQIS_DEV_STAGING, TEST_USERNAME_NO_PERMISSION, TEST_PASSWORD_NO_PERMISSION);
        //login(HNQIS_DEV_STAGING, TEST_USERNAME_WITH_PERMISSION, TEST_PASSWORD_WITH_PERMISSION);
        waitForPull(20);

        List<OrgUnit> orgUnitList= OrgUnit.getAllOrgUnit();

        List<Program> programList= Program.getAllPrograms();

        List<org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit> orgUnitSdkList= SDKTestUtils.getAllSDKOrganisationUnits();
        List<org.hisp.dhis.android.sdk.persistence.models.Program> programsSdkList= SDKTestUtils.getAllSDKPrograms();

        //Get all the organisation units saved in the sdk, and checks if is saved in our DB
        for(org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit organisationUnit:orgUnitSdkList){
            boolean isOrgUnitUid=false;
            boolean isOrgUnitName=false;
            boolean isOrgUnitProductivity=false;
            boolean isProgramListInOrgUnit=false;
            boolean isOrgUnitOrgUnitLevel=false;
            for(OrgUnit orgUnit:orgUnitList){
                if(organisationUnit.getId().equals(orgUnit.getUid())){
                    isOrgUnitUid=true;
                    if(organisationUnit.getLabel().equals(orgUnit.getName())){
                        isOrgUnitName=true;
                    }
                    List<OrganisationUnitAttributeValue> attributeValues=organisationUnit.getAttributeValues();
                    for(OrganisationUnitAttributeValue organisationUnitAttributeValue:attributeValues) {
                        if (organisationUnitAttributeValue.getAttribute().getCode().equals(ATTRIBUTE_PRODUCTIVITY_CODE))
                            if (organisationUnitAttributeValue.getValue().equals(String.valueOf(orgUnit.getProductivity()))) {
                                isOrgUnitProductivity = true;
                            }
                    }

                    for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(organisationUnit.getId(), ProgramType.WITHOUT_REGISTRATION)) {
                        boolean isProgramInOrgUnit=false;
                        for(Program orgUnitProgram:orgUnit.getPrograms()){
                            if(program.getUid().equals(orgUnitProgram.getUid())){
                                isProgramInOrgUnit=true;
                            }
                        }
                        if(!isProgramInOrgUnit) {
                            isProgramListInOrgUnit = false;
                            break;
                        }
                        else
                            isProgramListInOrgUnit=true;
                        }
                    }
                }
            assertTrue("Checking organisationUnit uid",isOrgUnitUid);
            assertTrue("Checking organisationUnit name",isOrgUnitName);
            assertTrue("Checking organisationUnit productivity",isOrgUnitProductivity);
            assertTrue("Checking organisationUnit program",isProgramListInOrgUnit);
            //Fixme the orgunitlevel is not pulled.
            //assertTrue("Checking organisationUnit orgUnitLevel",isOrgUnitOrgUnitLevel);
        }

        //We check for all the sdk programs if is saved in our DB
        for(org.hisp.dhis.android.sdk.persistence.models.Program programSDK:programsSdkList){
            boolean isProgramUid=false;
            for(Program program : programList) {
                if(programSDK.getUid().equals(program.getUid())){
                    isProgramUid=true;
                }
            }
            assertTrue("Checking program name",isProgramUid);
        }

    }

}