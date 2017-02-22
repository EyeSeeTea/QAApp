package org.eyeseetea.malariacare.data.database.utils;

import android.content.res.AssetManager;
import android.util.Log;

import com.opencsv.CSVReader;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.eyeseetea.malariacare.data.database.AppDatabase;
import org.eyeseetea.malariacare.data.database.model.Answer;
import org.eyeseetea.malariacare.data.database.model.CompositeScore;
import org.eyeseetea.malariacare.data.database.model.Header;
import org.eyeseetea.malariacare.data.database.model.Match;
import org.eyeseetea.malariacare.data.database.model.Media;
import org.eyeseetea.malariacare.data.database.model.Option;
import org.eyeseetea.malariacare.data.database.model.OrgUnit;
import org.eyeseetea.malariacare.data.database.model.OrgUnitLevel;
import org.eyeseetea.malariacare.data.database.model.OrgUnitProgramRelation;
import org.eyeseetea.malariacare.data.database.model.Program;
import org.eyeseetea.malariacare.data.database.model.Question;
import org.eyeseetea.malariacare.data.database.model.QuestionOption;
import org.eyeseetea.malariacare.data.database.model.QuestionRelation;
import org.eyeseetea.malariacare.data.database.model.Score;
import org.eyeseetea.malariacare.data.database.model.ServerMetadata;
import org.eyeseetea.malariacare.data.database.model.Survey;
import org.eyeseetea.malariacare.data.database.model.SurveySchedule;
import org.eyeseetea.malariacare.data.database.model.Tab;
import org.eyeseetea.malariacare.data.database.model.User;
import org.eyeseetea.malariacare.data.database.model.Value;
import org.hisp.dhis.client.sdk.core.common.persistence.DbOperation;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PopulateDB {

    public static final String TAG = ".PopulateDB";
    public static final String CSV_DIRECTORY = "csv/";

    public static final String PROGRAMS_CSV = "Program.csv";
    public static final String TABS_CSV = "Tab.csv";
    public static final String HEADERS_CSV = "Header.csv";
    public static final String ANSWERS_CSV = "Answer.csv";
    public static final String OPTIONS_CSV = "Option.csv";
    public static final String COMPOSITE_SCORES_CSV = "CompositeScore.csv";
    public static final String QUESTIONS_CSV = "Question.csv";
    public static final String QUESTION_RELATIONS_CSV = "QuestionRelation.csv";
    public static final String MATCHES_CSV = "Match.csv";
    public static final String QUESTION_OPTIONS_CSV = "QuestionOption.csv";
    public static final String ORG_UNIT_LEVELS_CSV = "OrgUnitLevel.csv";
    public static final String ORG_UNITS_CSV = "OrgUnit.csv";
    public static final String ORG_UNIT_PROGRAM_RELATIONS = "OrgUnitProgramRelation.csv";

    public static final String SCORE_CSV = "Score.csv";
    public static final String SERVER_METADATA_CSV = "ServerMetadata.csv";
    public static final String SURVEY_CSV = "Survey.csv";
    public static final String SURVEY_SCHEDULE_CSV = "SurveySchedule.csv";
    public static final String USER_CSV = "User.csv";
    public static final String VALUE_CSV = "Value.csv";

    static ArrayList<Model> list;

    public static void populateDB(AssetManager assetManager) throws IOException {

        Log.d(TAG, "Populating metaData from local csv files");

        //Clear maps from previous populations (this might be called after logout)
        initMaps();

        //Clear database
        wipeDatabase();

        List<String> tables2populate = Arrays.asList(PROGRAMS_CSV, TABS_CSV, HEADERS_CSV,
                ANSWERS_CSV, OPTIONS_CSV, COMPOSITE_SCORES_CSV,
                QUESTIONS_CSV, QUESTION_RELATIONS_CSV, MATCHES_CSV, QUESTION_OPTIONS_CSV,
                ORG_UNIT_LEVELS_CSV, ORG_UNITS_CSV, ORG_UNIT_PROGRAM_RELATIONS,
                SURVEY_CSV, VALUE_CSV, SCORE_CSV, USER_CSV, SERVER_METADATA_CSV,
                SURVEY_SCHEDULE_CSV);

        CSVReader reader;
        for (String table : tables2populate) {
            Log.d(TAG, "Populating " + table);
            reader = new CSVReader(new InputStreamReader(assetManager.open(CSV_DIRECTORY + table)),
                    '~', '\"');

            String[] line;
            while ((line = reader.readNext()) != null) {
                switch (table) {
                    case PROGRAMS_CSV:
                        Program program = new Program();
                        program.setId_program(Long.parseLong(line[0]));
                        program.setUid(line[1]);
                        program.setName(line[2]);
                        program.setStageUid(line[3]);
                        list.add(program);
                        break;
                    case TABS_CSV:
                        Tab tab = new Tab();
                        tab.setId_tab(Long.parseLong(line[0]));
                        tab.setName(line[1]);
                        tab.setOrder_pos(Integer.valueOf(line[2]));
                        if (!line[3].equals("")) {
                            tab.setType(Integer.valueOf(line[3]));
                        }
                        tab.setProgram(Long.parseLong(line[4]));
                        list.add(tab);
                        break;
                    case HEADERS_CSV:
                        Header header = new Header();
                        header.setId_header(Long.parseLong(line[0]));
                        header.setShort_name(line[1]);
                        header.setName(line[2]);
                        header.setOrder_pos(Integer.valueOf(line[3]));
                        header.setTab(Long.parseLong(line[4]));
                        list.add(header);
                        break;
                    case ANSWERS_CSV:
                        Answer answer = new Answer();
                        answer.setId_answer(Long.parseLong(line[0]));
                        answer.setName(line[1]);
                        list.add(answer);
                        break;
                    case OPTIONS_CSV:
                        Option option = new Option();
                        option.setId_option(Long.parseLong(line[0]));
                        option.setUid(line[1]);
                        option.setCode(line[2]);
                        option.setName(line[3]);
                        if (!line[4].equals("")) {
                            option.setFactor(Float.valueOf(line[4]));
                        } else {
                            option.setFactor(0f);
                        }
                        option.setAnswer(Long.parseLong(line[5]));
                        list.add(option);
                        break;
                    case COMPOSITE_SCORES_CSV:
                        CompositeScore compositeScore = new CompositeScore();
                        compositeScore.setId_composite_score(Long.parseLong(line[0]));
                        compositeScore.setHierarchical_code(line[1]);
                        compositeScore.setLabel(line[2]);
                        compositeScore.setUid(line[3]);
                        compositeScore.setOrder_pos(Integer.parseInt(line[4]));
                        if (!line[5].equals("")) {
                            compositeScore.setCompositeScore(Long.parseLong(line[5]));
                        }
                        list.add(compositeScore);
                        break;
                    case QUESTIONS_CSV:
                        Question question = new Question();
                        question.setId_question(Long.parseLong(line[0]));
                        question.setCode(line[1]);
                        question.setDe_name(line[2]);
                        question.setShort_name(line[3]);
                        question.setForm_name(line[4]);
                        question.setUid(line[5]);
                        question.setOrder_pos(Integer.valueOf(line[6]));
                        if (!line[7].equals("")) {
                            question.setNumerator_w(Float.valueOf(line[7]));
                        } else {
                            question.setNumerator_w(0f);
                        }
                        if (!line[8].equals("")) {
                            question.setDenominator_w(Float.valueOf(line[8]));
                        } else {
                            question.setDenominator_w(0f);
                        }
                        question.setFeedback(line[9]);
                        if (!line[10].equals("")) {
                            question.setHeader(Long.parseLong(line[10]));
                        }
                        if (!line[11].equals("")) {
                            question.setAnswer(Long.parseLong(line[11]));
                        }
                        if (!line[12].equals("")) {
                            question.setOutput(Integer.parseInt(line[12]));
                        }
                        if (!line[13].equals("")) {
                            Integer compulsory = Integer.valueOf(line[13]);
                            boolean compulsoryValue = (compulsory == 0) ? false : true;
                            question.setCompulsory(compulsoryValue);
                        }
                        //Line [14] is id_parent. Not used
                        if (!line[15].equals("")) {
                            question.setCompositeScore(Long.parseLong(line[15]));
                        }
                        if (!line[16].equals("")) {
                            question.setRow(Integer.parseInt(line[16]));
                        }
                        if (!line[17].equals("")) {
                            question.setColumn(Integer.parseInt(line[17]));
                        }
                        list.add(question);
                        break;
                    case QUESTION_RELATIONS_CSV:
                        QuestionRelation questionRelation = new QuestionRelation();
                        questionRelation.setId_question_relation(Integer.valueOf(line[0]));
                        questionRelation.setQuestion(Long.parseLong(line[1]));
                        questionRelation.setOperation(Integer.valueOf(line[2]));
                        list.add(questionRelation);
                        break;
                    case MATCHES_CSV:
                        Match match = new Match();
                        match.setId_match(Integer.parseInt(line[0]));
                        match.setQuestionRelation(Long.parseLong(line[1]));
                        list.add(match);
                        break;
                    case QUESTION_OPTIONS_CSV:
                        QuestionOption questionOption = new QuestionOption();
                        questionOption.setId_question_option(Integer.valueOf(line[0]));
                        questionOption.setOption(Long.parseLong(line[1]));
                        questionOption.setQuestion(Long.parseLong(line[2]));
                        questionOption.setMatch(Long.parseLong(line[3]));
                        list.add(questionOption);
                        break;
                    case ORG_UNIT_LEVELS_CSV:
                        OrgUnitLevel orgUnitLevel = new OrgUnitLevel();
                        orgUnitLevel.setId_org_unit_level(Long.parseLong(line[0]));
                        orgUnitLevel.setName(line[1]);
                        orgUnitLevel.setUid(line[2]);
                        list.add(orgUnitLevel);
                        break;
                    case ORG_UNITS_CSV:
                        OrgUnit orgUnit = new OrgUnit();
                        orgUnit.setId_org_unit(Long.parseLong(line[0]));
                        orgUnit.setUid(line[1]);
                        orgUnit.setName(line[2]);
                        if (!line[3].equals("")) {
                            orgUnit.setOrgUnit(Long.parseLong(line[3]));
                        }
                        if (!line[4].equals("")) {
                            orgUnit.setOrgUnitLevel(Long.parseLong(line[4]));
                        }
                        list.add(orgUnit);
                        break;
                    case ORG_UNIT_PROGRAM_RELATIONS:
                        OrgUnitProgramRelation orgUnitProgramRelation =
                                new OrgUnitProgramRelation();
                        orgUnitProgramRelation.setId_orgunit_program_relation(
                                Integer.valueOf(line[0]));
                        orgUnitProgramRelation.setOrgUnit(Long.parseLong(line[1]));
                        orgUnitProgramRelation.setProgram(Long.parseLong(line[2]));
                        if (!line[3].equals("")) {
                            orgUnitProgramRelation.setProductivity(Integer.valueOf(line[3]));
                        }
                        list.add(orgUnitProgramRelation);
                        break;
                    case SURVEY_SCHEDULE_CSV:
                        SurveySchedule surveySchedule = new SurveySchedule();
                        surveySchedule.setId_survey_schedule(
                                Integer.valueOf(line[0]));
                        surveySchedule.setSurvey(
                                Long.parseLong(line[1]));
                        surveySchedule.setComment(line[2]);
                        if (!line[3].equals("")) {
                            surveySchedule.setPrevious_date(new Date(Long.parseLong(line[3])));
                        }
                        list.add(surveySchedule);
                        break;
                    case SURVEY_CSV:
                        Survey survey = new Survey();
                        survey.setId_survey(Long.parseLong(line[0]));

                        survey.setProgram(Long.parseLong(line[1]));

                        survey.setOrgUnit(Long.parseLong(line[2]));

                        if (!line[3].equals("")) {
                            survey.setUser(Long.parseLong(line[3]));
                        }
                        if (!line[4].equals("")) {
                            survey.setCreationDate(new Date(Long.parseLong(line[4])));
                        }
                        if (!line[5].equals("")) {
                            survey.setCompletionDate(new Date(Long.parseLong(line[5])));
                        }
                        if (!line[6].equals("")) {
                            survey.setUploadDate(new Date(Long.parseLong(line[6])));
                        }
                        if (!line[7].equals("")) {
                            survey.setScheduledDate(new Date(Long.parseLong(line[7])));
                        }
                        if (!line[8].equals("")) {
                            survey.setStatus(Integer.parseInt(line[8]));
                        }
                        survey.setEventUid(line[9]);
                        list.add(survey);
                        break;
                    case VALUE_CSV:
                        Value value = new Value();
                        value.setId_value(Long.parseLong(line[0]));
                        value.setValue(line[1]);
                        if (!line[2].equals("")) {
                            value.setQuestion(Long.parseLong(line[2]));
                        }
                        value.setSurvey(Long.parseLong(line[3]));
                        if (!line[4].equals("")) {
                            value.setOption(Long.parseLong(line[4]));
                        }
                        if (!line[5].equals("")) {
                            Integer conflict = Integer.valueOf(line[5]);
                            boolean isInConflict = (conflict == 0) ? false : true;
                            value.setConflict(isInConflict);
                        }
                        if (!line[6].equals("")) {
                            value.setUploadDate(new Date(Long.parseLong(line[6])));
                        }
                        list.add(value);
                        break;
                    case SCORE_CSV:
                        Score score = new Score();
                        score.setId_score(Long.parseLong(line[0]));
                        score.setSurvey(Long.parseLong(line[1]));
                        score.setUid(line[2]);
                        score.setScore(Float.parseFloat(line[3]));
                        list.add(score);
                        break;
                    case USER_CSV:
                        User user = new User();
                        user.setId_user(Long.parseLong(line[0]));
                        user.setUid(line[1]);
                        user.setName(line[2]);
                        user.setUsername(line[3]);
                        list.add(user);
                        break;
                    case SERVER_METADATA_CSV:
                        ServerMetadata serverMetadata = new ServerMetadata();
                        serverMetadata.setId_control_dataelement(Long.parseLong(line[0]));
                        serverMetadata.setName(line[1]);
                        serverMetadata.setCode(line[2]);
                        serverMetadata.setUid(line[3]);
                        serverMetadata.setValueType(line[4]);
                        list.add(serverMetadata);
                        break;
                }
            }
            reader.close();
        }

        saveAllItems(list);
    }

    /**
     * Deletes all data from the app database
     */

    public static void wipeDatabase() {
        Delete.tables(
                Value.class,
                Score.class,
                Survey.class,
                SurveySchedule.class,
                OrgUnit.class,
                OrgUnitLevel.class,
                OrgUnitProgramRelation.class,
                User.class,
                QuestionOption.class,
                Match.class,
                QuestionRelation.class,
                Question.class,
                CompositeScore.class,
                org.eyeseetea.malariacare.data.database.model.Option.class,
                Answer.class,
                Header.class,
                Tab.class,
                org.eyeseetea.malariacare.data.database.model.Program.class,
                ServerMetadata.class,
                Media.class
        );
    }

    protected static void saveAllItems(final List<Model> models) {
        DatabaseDefinition databaseDefinition =
                FlowManager.getDatabase(AppDatabase.class); // execute  transaction
        databaseDefinition.executeTransaction(new ITransaction() {
            @Override
            public void execute(DatabaseWrapper databaseWrapper) {
                for (Model model : models) {
                    model.insert();
                }
            }
        });
    }

    protected static void initMaps() {
        list = new ArrayList<>();
    }
}
