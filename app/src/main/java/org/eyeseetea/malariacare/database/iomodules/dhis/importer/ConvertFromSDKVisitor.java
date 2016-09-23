/*
 * Copyright (c) 2015.
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

package org.eyeseetea.malariacare.database.iomodules.dhis.importer;

import android.util.Log;

import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.ProgressActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataElementExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.DataValueExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.EventExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OptionSetExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.OrganisationUnitLevelExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.ProgramStageSectionExtended;
import org.eyeseetea.malariacare.database.iomodules.dhis.importer.models.UserAccountExtended;
import org.eyeseetea.malariacare.database.model.Answer;
import org.eyeseetea.malariacare.database.model.CompositeScore;
import org.eyeseetea.malariacare.database.model.ControlDataElement;
import org.eyeseetea.malariacare.database.model.OrgUnit;
import org.eyeseetea.malariacare.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.model.Score;
import org.eyeseetea.malariacare.database.model.Survey;
import org.eyeseetea.malariacare.database.model.Tab;
import org.eyeseetea.malariacare.database.model.TabGroup;
import org.eyeseetea.malariacare.database.model.User;
import org.eyeseetea.malariacare.database.model.Value;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramCompositeScoreDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramDataElementDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramQuestionDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramStageSectionTabDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramSurveyDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramTabDict;
import org.eyeseetea.malariacare.database.utils.multikeydictionaries.ProgramTabGroupDict;
import org.eyeseetea.malariacare.utils.Constants;
import org.hisp.dhis.android.sdk.controllers.metadata.MetaDataController;
import org.hisp.dhis.android.sdk.persistence.models.DataElement;
import org.hisp.dhis.android.sdk.persistence.models.DataValue;
import org.hisp.dhis.android.sdk.persistence.models.Event;
import org.hisp.dhis.android.sdk.persistence.models.Option;
import org.hisp.dhis.android.sdk.persistence.models.OptionSet;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnit;
import org.hisp.dhis.android.sdk.persistence.models.OrganisationUnitLevel;
import org.hisp.dhis.android.sdk.persistence.models.Program;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStage;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageDataElement;
import org.hisp.dhis.android.sdk.persistence.models.ProgramStageSection;
import org.hisp.dhis.android.sdk.persistence.models.UserAccount;
import org.hisp.dhis.android.sdk.utils.api.ProgramType;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConvertFromSDKVisitor implements IConvertFromSDKVisitor {

    private final static String TAG=".ConvertFromSDKVisitor";
    static Map<String,org.eyeseetea.malariacare.database.model.Program> programMapObjects;
    static Map<String,Object> controlDataElementMapObjects;
    static Map<String,OrgUnitLevel> orgUnitLevelMap;
    static Map<String,OrgUnit> orgUnitDict;
    static Map<String,Answer> answerMap;
    static ProgramTabDict programTabDict;
    static ProgramTabGroupDict programTabGroupDict;
    static ProgramStageSectionTabDict programStageSectionTabDict;
    static ProgramSurveyDict programSurveyDict;
    static ProgramCompositeScoreDict programCompositeScoreDict;
    static ProgramQuestionDict programQuestionDict;


    /**
     * Builders that helps while linking compositeScores and questions
     */
    CompositeScoreBuilder compositeScoreBuilder;
    QuestionBuilder questionBuilder;
    private final String ATTRIBUTE_PRODUCTIVITY_CODE="OUProductivity";
    private final String SDKDateFormat="yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    Program currentProgram;

    public ConvertFromSDKVisitor(){
        programMapObjects = new HashMap();
        controlDataElementMapObjects =new HashMap();
        orgUnitLevelMap = new HashMap();
        orgUnitDict = new HashMap();
        answerMap = new HashMap();
        programTabDict = new ProgramTabDict();
        programTabGroupDict = new ProgramTabGroupDict();
        programStageSectionTabDict = new ProgramStageSectionTabDict();
        programQuestionDict = new ProgramQuestionDict();
        programSurveyDict = new ProgramSurveyDict();
        programCompositeScoreDict = new ProgramCompositeScoreDict();
        compositeScoreBuilder = new CompositeScoreBuilder();
        questionBuilder = new QuestionBuilder();

        //Reload static dataElement codes
        DataElementExtended.reloadDataElementTypeCodes();
    }

    /**
     * Turns a sdk Program into an app Program
     * @param sdkProgramExtended
     */
    public void visit(ProgramExtended sdkProgramExtended){
        //Build program
        Program program=sdkProgramExtended.getProgram();
        currentProgram =program;
        org.eyeseetea.malariacare.database.model.Program appProgram=new org.eyeseetea.malariacare.database.model.Program();
        appProgram.setUid(program.getUid());
        appProgram.setName(program.getDisplayName());
        appProgram.save();


        //Annotate built program
        programMapObjects.put(program.getUid(), appProgram);

        //Visit children
        for(ProgramStage ps:program.getProgramStages()){
            new ProgramStageExtended(ps).accept(this);
        }
    }

    /**
     * Turns a sdk ProgramStage into a TabGroup
     * @param sdkProgramStageExtended
     */
    @Override
    public void visit(ProgramStageExtended sdkProgramStageExtended) {
        //Build tabgroup
        ProgramStage programStage=sdkProgramStageExtended.getProgramStage();
        org.eyeseetea.malariacare.database.model.Program appProgram=programMapObjects.get(currentProgram.getUid());
        TabGroup appTabGroup = new TabGroup();
        //FIXME TabGroup has no UID right now
        appTabGroup.setName(programStage.getDisplayName());
        appTabGroup.setProgram(appProgram);
        appTabGroup.setUid(programStage.getUid());
        appTabGroup.save();

        //Annotate built tabgroup
        programTabGroupDict.put(currentProgram.getUid(),programStage.getUid(), appTabGroup);

        //Visit children
        for(ProgramStageSection pss:programStage.getProgramStageSections()){
            new ProgramStageSectionExtended(pss).accept(this);
        }
    }

    /**
     * Turns a sdk level into an app level
     * @param sdkOrganisationUnitLevelExtended
     */
    @Override
    public void visit(OrganisationUnitLevelExtended sdkOrganisationUnitLevelExtended){
        OrganisationUnitLevel organisationUnitLevel = sdkOrganisationUnitLevelExtended.getOrganisationUnitLevel();
        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
        orgUnitLevel.setUid(organisationUnitLevel.getId());
        orgUnitLevel.setName(organisationUnitLevel.getDisplayName());
        orgUnitLevel.save();

        orgUnitLevelMap.put(sdkOrganisationUnitLevelExtended.buildKey(),orgUnitLevel);
    }

    /**
     * Turns a sdk organisationUnit into an app OrgUnit
     *
     * @param sdkOrganisationUnitExtended
     */
    @Override
    public void visit(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        //Create and save OrgUnitLevel
        OrganisationUnit organisationUnit=sdkOrganisationUnitExtended.getOrgUnit();
        OrgUnitLevel appOrgUnitLevel = orgUnitLevelMap.get(OrganisationUnitLevelExtended.buildKey(organisationUnit.getLevel()));
        //create the orgUnit
        org.eyeseetea.malariacare.database.model.OrgUnit appOrgUnit= new org.eyeseetea.malariacare.database.model.OrgUnit();
        //Set name
        if(organisationUnit.getLabel()==null)
            appOrgUnit.setName(organisationUnit.getName());
        else
            appOrgUnit.setName(organisationUnit.getLabel());
        //Set uid
        appOrgUnit.setUid(organisationUnit.getId());
        //Set orgUnitLevel
        appOrgUnit.setOrgUnitLevel(appOrgUnitLevel);
        //Since there is no guaranteed order in orgunits parent unit might not be yet converted or even pulled at all
        //Thus building hierarchy must be done in a second step

        appOrgUnit.save();
        //Annotate built orgunit
        orgUnitDict.put(organisationUnit.getId(), appOrgUnit);

        //Associate programs
        sdkOrganisationUnitExtended.setAppOrgUnit(appOrgUnit);
        buildOrgUnitProgramRelation(sdkOrganisationUnitExtended);
    }

    /**
     * Turns a sdk ProgramStageSection into a Tab
     * @param sdkProgramStageSectionExtended
     */
    @Override
    public void visit(ProgramStageSectionExtended sdkProgramStageSectionExtended) {
        //Build Tab

        ProgramStageSection programStageSection=sdkProgramStageSectionExtended.getProgramStageSection();
        org.eyeseetea.malariacare.database.model.TabGroup appTabGroup=programTabGroupDict.get(currentProgram.getUid(),programStageSection.getProgramStage());
        Tab appTab = new Tab();
        //FIXME TabGroup has no UID right now
        appTab.setName(programStageSection.getDisplayName());
        appTab.setType(Constants.TAB_AUTOMATIC);
        appTab.setOrder_pos(programStageSection.getSortOrder());
        appTab.setTabGroup(appTabGroup);
        appTab.save();
        //Annotate build tab
        programTabDict.put(currentProgram.getUid(),sdkProgramStageSectionExtended.getProgramStageSection().getUid(), appTab);
        programStageSectionTabDict.put(currentProgram.getUid(),programStageSection.getUid(), appTab);
    }


    /**
     * Turns a sdk OptionSet into an Answer
     * @param sdkOptionSetExtended
     */
    @Override
    public void visit(OptionSetExtended sdkOptionSetExtended) {
        //Build answer
        OptionSet sdkOptionSet=sdkOptionSetExtended.getOptionSet();
        Answer appAnswer = new Answer();
        appAnswer.setName(sdkOptionSet.getName());
        appAnswer.save();

        //Annotate built answer
        answerMap.put(sdkOptionSet.getUid(), appAnswer);

        //Visit children
        for(Option option:sdkOptionSet.getOptions()){
            new OptionExtended(option).accept(this);
        }
    }

    /**
     * Turns a sdk Option into an Option
     * @param sdkOptionExtended
     */
    @Override
    public void visit(OptionExtended sdkOptionExtended) {
        //Build option
        Option sdkOption=sdkOptionExtended.getOption();
        Answer appAnswer= answerMap.get(sdkOption.getOptionSet());
        org.eyeseetea.malariacare.database.model.Option appOption= new org.eyeseetea.malariacare.database.model.Option();
        appOption.setName(sdkOption.getName());
        appOption.setCode(sdkOption.getCode());
        appOption.setAnswer(appAnswer);
        appOption.setFactor(sdkOptionExtended.getFactor());
        appOption.save();
    }

    /**
     * Turns a sdk userAccount into a User
     * @param sdkUserAccountExtended
     */
    @Override
    public void visit(UserAccountExtended sdkUserAccountExtended) {
        UserAccount userAccount=sdkUserAccountExtended.getUserAccount();
        User appUser = new User();
        appUser.setUid(userAccount.getUId());
        appUser.setName(userAccount.getName());
        appUser.setUsername(userAccount.getUsername());
        appUser.save();
    }


    /**
     * Turns a dataElement into a question or a compositeScore
     * @param sdkDataElementExtended
     */
    @Override
    public void visit(DataElementExtended sdkDataElementExtended) {
        if(sdkDataElementExtended.isCompositeScore()){
            programCompositeScoreDict.put(currentProgram.getUid(),sdkDataElementExtended.getDataElement().getUid(),buildCompositeScore(sdkDataElementExtended));
        }else if(sdkDataElementExtended.isQuestion()){
            programQuestionDict.put(currentProgram.getUid(),sdkDataElementExtended.getDataElement().getUid(),buildQuestion(sdkDataElementExtended));
            //Question type is annotated in 'answer' from an attribute of the question
        }else if (sdkDataElementExtended.isControlDataElement()) {
            if(!controlDataElementMapObjects.containsKey(sdkDataElementExtended.getDataElement().getUid()))
                controlDataElementMapObjects.put(sdkDataElementExtended.getDataElement().getUid(),buildControlDataElement(sdkDataElementExtended));
        } else {
            Log.d(TAG, "Error" + sdkDataElementExtended.getDataElement().toString());
            return;
        }
    }

    /**
     * Turns an event into a sent survey
     * @param sdkEventExtended
     */
    @Override
    public void visit(EventExtended sdkEventExtended) {
        Event event=sdkEventExtended.getEvent();
        OrgUnit orgUnit = orgUnitDict.get(event.getOrganisationUnitId());
        TabGroup tabGroup=programTabGroupDict.get(currentProgram.getUid(),event.getProgramStageId());

        Survey survey=new Survey();
        //Any survey that comes from the pull has been sent
        survey.setStatus(Constants.SURVEY_SENT);
        //Set dates
        survey.setCompletionDate(sdkEventExtended.getEventDate());
        //This prevent a null dates, but the CreationDation and UploadedDate need be setted in dataValue visitor.
        survey.setCreationDate(sdkEventExtended.getEventDate());
        survey.setUploadedDate(sdkEventExtended.getEventDate());

        survey.setScheduledDate(sdkEventExtended.getScheduledDate());
        //Set fks
        survey.setOrgUnit(orgUnit);
        survey.setTabGroup(tabGroup);
        survey.setEventUid(event.getUid());
        survey.save();

        //Annotate object in map
        programSurveyDict.put(currentProgram.getUid(),event.getUid(), survey);

        //Visit its values
        for(DataValue dataValue:event.getDataValues()){
            DataValueExtended dataValueExtended=new DataValueExtended(dataValue);
            dataValueExtended.accept(this);
        }
    }

    @Override
    public void visit(DataValueExtended sdkDataValueExtended) {

        DataValue dataValue=sdkDataValueExtended.getDataValue();
        Survey survey=programSurveyDict.get(currentProgram.getUid(),dataValue.getEvent());
        //Data value is a value from compositeScore
        if(programCompositeScoreDict.containsKey(currentProgram.getUid(),dataValue.getDataElement())){
            //CHeck if it is a root score -> score
            CompositeScore compositeScore = programCompositeScoreDict.get(currentProgram.getUid(),dataValue.getDataElement());
            if(CompositeScoreBuilder.ROOT_NODE_CODE.equals(compositeScore.getHierarchical_code())){
                Score score = new Score();
                score.setScore(Float.parseFloat(dataValue.getValue()));
                score.setUid(dataValue.getDataElement());
                score.setSurvey(survey);
                score.save();
            }
            return;
        }
        /**
        else{
            if(!controlDataElementMapObjects.containsKey(dataValue.getDataElement()) && !programQuestionDict.containsKey(currentProgram.getUid(),dataValue.getDataElement()) ) {
                Log.i(TAG, "Error recovering Compositescore: programuid " + currentProgram.getUid() + " dataelement" + dataValue.getDataElement());
            }
        }
        */

        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.created_on_code))){
            try{
                Date date = EventExtended.parseDate(dataValue.getValue(),EventExtended.AMERICAN_DATE_FORMAT);
                survey.setCreationDate(date);
                survey.save();
                //Annotate object in map
                programSurveyDict.put(currentProgram.getUid(),dataValue.getEvent(), survey);
            }catch(ParseException e){
                Log.d(TAG,"Error converting creation date from datavalue in survey: "+survey.getId_survey());
            }
            return;
        }

        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.upload_date_code))){
            try{
                Date date = EventExtended.parseDate(dataValue.getValue(),EventExtended.AMERICAN_DATE_FORMAT);
                survey.setUploadedDate(date);
                survey.save();
                //Annotate object in map
                programSurveyDict.put(currentProgram.getUid(),dataValue.getEvent(), survey);
            }catch(ParseException e){
                Log.d(TAG,"Error converting upload date from datavalue in survey:"+survey.getId_survey());
            }
            return;
        }

        if(dataValue.getDataElement().equals(PreferencesState.getInstance().getContext().getResources().getString(R.string.uploaded_by_code))){
            User user=User.getUser(dataValue.getValue());
            if(user==null) {
                user = new User(dataValue.getValue(), dataValue.getValue());
                user.save();
            }
            survey.setUser(user);
            survey.save();
            //Annotate object in map
            programSurveyDict.put(currentProgram.getUid(),dataValue.getEvent(), survey);
            return;
        }

        Value value=new Value();
        //Datavalue is a value from a question
        org.eyeseetea.malariacare.database.model.Option option = null;
        if(programQuestionDict.containsKey(currentProgram.getUid(),dataValue.getDataElement())){
            Question question = programQuestionDict.get(currentProgram.getUid(), dataValue.getDataElement());
            try {
                value.setQuestion(question);
                option = sdkDataValueExtended.findOptionByQuestion(question);
                value.setOption(option);
            } catch (ClassCastException e) {
                Log.d(TAG, "Exception with controlDataelement in DataValue converting");
            }
        }
        /**
        else{
            if(!controlDataElementMapObjects.containsKey(dataValue.getDataElement()) && !programCompositeScoreDict.containsKey(currentProgram.getUid(),dataValue.getDataElement()) ) {
                Log.d(TAG, "Error recovering Question: programuid " + currentProgram.getUid() + " dataelement" + dataValue.getDataElement());
            }
        }
        */

        value.setSurvey(survey);
        //No option -> text question (straight value)
        if(option==null){
            value.setValue(dataValue.getValue());
        }else{
        //Option -> extract value from code
            value.setValue(option.getName());
        }
        value.save();
    }


    /**
     * Turns a dataElement into a question
     * @param dataElementExtended
     */
    private Question buildQuestion(DataElementExtended dataElementExtended) {
        DataElement dataElement = dataElementExtended.getDataElement();
        Question appQuestion = new Question();
        appQuestion.setDe_name(dataElement.getName());
        appQuestion.setUid(dataElement.getUid());
        appQuestion.setShort_name(dataElement.getShortName());
        appQuestion.setForm_name(dataElement.getFormName());
        appQuestion.setFeedback(dataElement.getDescription());
        appQuestion.setCode(dataElement.getCode());
        appQuestion.setOrder_pos(dataElementExtended.findOrder());
        appQuestion.setNumerator_w(dataElementExtended.findNumerator());
        appQuestion.setDenominator_w(dataElementExtended.findDenominator());
        appQuestion.setOutput(compositeScoreBuilder.findAnswerOutput(dataElementExtended));

        //Label does not have an optionset
        if (dataElement.getOptionSet() != null) {
            appQuestion.setAnswer(answerMap.get(dataElement.getOptionSet()));
        }else{
            //A question with NO optionSet is a Label Question
            //Log.d(TAG, String.format("Question (%s) is a LABEL", dataElement.getUid()));
            appQuestion.setAnswer(buildAnswerLabel());
        }

        ProgramStageDataElement programStageDataElement = DataElementExtended.findProgramStageDataElementByDataElementUID(dataElement.getUid());
        appQuestion.setCompulsory(programStageDataElement.getCompulsory());
        appQuestion.setHeader(questionBuilder.saveHeader(dataElementExtended, currentProgram));
        questionBuilder.registerParentChildRelations(dataElementExtended);
        appQuestion.save();
        questionBuilder.add(appQuestion);
        return appQuestion;
    }


    public void buildRelations(DataElementExtended dataElementExtended) {
        questionBuilder.addRelations(dataElementExtended);
    }

    /**
     * A dataElement (question) without optionSet is a Label.
     * This method inits the LABEL answer (the first time) and updates de question.answer to it
     */

    /**
     * Builds a synthetic answer 'LABEL'
     * @return
     */
    public Answer buildAnswerLabel() {

        //Build a sintetic Key (AnswerLABEL)
        final String key=Answer.class+Constants.LABEL;
        //Look for sintetic LABEL (answer) already created
        Answer answer= answerMap.get(key);

        //First time no Label answer has been created
        if(answer==null){
            answer=new Answer(Constants.LABEL);
            answer.save();
            answerMap.put(key,answer);
        }

        return answer;
    }
    /**
     * Turns a dataElement into a question
     * @param sdkDataElementExtended
     */
    private CompositeScore buildCompositeScore(DataElementExtended sdkDataElementExtended){
        DataElement dataElement=sdkDataElementExtended.getDataElement();
        CompositeScore compositeScore = new CompositeScore();
        compositeScore.setUid(dataElement.getUid());
        compositeScore.setLabel(dataElement.getFormName());
        compositeScore.setHierarchical_code(compositeScoreBuilder.findHierarchicalCode(sdkDataElementExtended));
        compositeScore.setOrder_pos(sdkDataElementExtended.findOrder());
        //Parent score and Order can only be set once every score in saved
        compositeScore.save();

        compositeScoreBuilder.add(compositeScore);
        return compositeScore;
    }



    private ControlDataElement buildControlDataElement(DataElementExtended sdkDataElementExtended) {
        DataElement dataElement=sdkDataElementExtended.getDataElement();
        ControlDataElement controlDataElement = new ControlDataElement();
        controlDataElement.setUid(dataElement.getUid());
        controlDataElement.setCode(dataElement.getCode());
        controlDataElement.setName(dataElement.getDisplayName());
        controlDataElement.setValueType(dataElement.getValueType().name());

        //Parent score and Order can only be set once every score in saved
        controlDataElement.save();
        return controlDataElement;

    }

    /**
     * Due to permissions programs 'belongs' to a given orgunit and that relationship has a productivity
     * @param sdkOrganisationUnitExtended Extended sdk orgUnit (used to cache array with values)
     */
    public void buildOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended) {
        OrgUnit appOrgUnit = sdkOrganisationUnitExtended.getAppOrgUnit();
        Log.d(TAG, "buildOrgUnitProgramRelation " + appOrgUnit.getName());
        //Each assigned program
        for (org.hisp.dhis.android.sdk.persistence.models.Program program : MetaDataController.getProgramsForOrganisationUnit(appOrgUnit.getUid(), ProgramType.WITHOUT_REGISTRATION)) {
            ProgramExtended sdkProgramExtended = new ProgramExtended(program);
            sdkProgramExtended.setAppProgram( programMapObjects.get(program.getUid()));

            addOrgUnitProgramRelation(sdkOrganisationUnitExtended,sdkProgramExtended);
        }
    }

    /**
     * Updates the relationship between the given orgUnit and program according to their attribute values
     * @param sdkOrganisationUnitExtended
     * @param sdkProgramExtended
     */
    private void addOrgUnitProgramRelation(OrganisationUnitExtended sdkOrganisationUnitExtended, ProgramExtended sdkProgramExtended){
        //Take app references
        OrgUnit appOrgUnit=sdkOrganisationUnitExtended.getAppOrgUnit();
        org.eyeseetea.malariacare.database.model.Program appProgram=sdkProgramExtended.getAppProgram();

        //Add relationship
        OrgUnitProgramRelation orgUnitProgramRelation=appProgram.addOrgUnit(appOrgUnit);

        //Add productivity to that relationship
        Integer productivityIndex=sdkProgramExtended.getProductivityPosition();
        Integer orgUnitProgramRelationProductivity = sdkOrganisationUnitExtended.getProductivity(productivityIndex);
        orgUnitProgramRelation.setProductivity(orgUnitProgramRelationProductivity);
        orgUnitProgramRelation.save();
    }

    @Override
    public void buildScores() {
        compositeScoreBuilder.buildScores();
    }

    /**
     * Builds the orgunit hierarchy whenever is possible
     * @param assignedOrganisationsUnits
     * @return
     */
    public boolean buildOrgUnitHierarchy(List<OrganisationUnit> assignedOrganisationsUnits) {

        for(OrganisationUnit organisationUnit:assignedOrganisationsUnits){
            if(!ProgressActivity.PULL_IS_ACTIVE) return false;

            OrgUnit appOrgUnit = orgUnitDict.get(organisationUnit.getId());
            String parentUID=organisationUnit.getParent();
            //FIXME: review this algorithm
            if(parentUID==null) {
                //path format=/VaXGMQY18R2/TyoXRBeZ12K/TeqzAowss4n/Doa9u6qkSO3/qeENMD3x6y7
                //path[0] is ""
                //path [1] is the last parent "VaXGMQY18R2"
                String path = organisationUnit.getPath();
                String[] pathUids = path.split("/");
                if (pathUids.length > 2 && !pathUids[1].equals(organisationUnit.getId())) {
                    for (int i = 2; i < pathUids.length; i++) {
                        if (pathUids[i].equals(organisationUnit.getId())) {
                            parentUID = pathUids[i - 1];
                            Log.d(TAG, organisationUnit.getId() + " parent " + parentUID);
                        }
                    }
                }
            }
            //No parent nothing to do
            if(parentUID==null){
                Log.i(TAG,String.format("%s is a root orgUnit",appOrgUnit.getName()));
                continue;
            }

            //Find parent
            OrgUnit parentOrgUnit = orgUnitDict.get(parentUID);

            //Due to server permissions parent unit might not be loaded
            if(parentOrgUnit==null){
                Log.w(TAG,String.format("Cannot find parent orgunit for %s",appOrgUnit.getName()));
                continue;
            }

            appOrgUnit.setOrgUnit(parentOrgUnit.getId_org_unit());
            appOrgUnit.save();
        }
        return true;

    }

}
