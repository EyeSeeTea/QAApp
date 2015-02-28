package org.eyeseetea.malariacare.layout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import org.eyeseetea.malariacare.MainActivity;
import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.adapters.IQATestArrayAdapter;
import org.eyeseetea.malariacare.adapters.ReportingResultsArrayAdapter;
import org.eyeseetea.malariacare.data.Header;
import org.eyeseetea.malariacare.data.Option;
import org.eyeseetea.malariacare.data.Question;
import org.eyeseetea.malariacare.data.Tab;
import org.eyeseetea.malariacare.models.DataHolder;
import org.eyeseetea.malariacare.models.ReportingResults;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.utils.LoadCustomQuestions;
import org.eyeseetea.malariacare.utils.NumDenRecord;
import org.eyeseetea.malariacare.utils.TabConfiguration;
import org.eyeseetea.malariacare.utils.Utils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by adrian on 19/02/15.
 */
public class Layout {

    //static final Score scores=new Score();
    static Map<Integer, NumDenRecord> numDenRecordMap = new HashMap<Integer, NumDenRecord>();
    static final int [] backgrounds = {R.drawable.background_even, R.drawable.background_odd};

    // This method fill in a tab layout
    public static int insertTab(MainActivity mainActivity, Tab tab, final TabConfiguration tabConfiguration) {

        int iterBacks = 0;
        // This layout inflater is for joining other layouts
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // This layout is for the tab content (questions)
        LinearLayout layoutGrandParent = (LinearLayout) mainActivity.findViewById(tabConfiguration.getTabId());
        layoutGrandParent.setTag(tab);
        ScrollView layoutParentScroll = (ScrollView) layoutGrandParent.getChildAt(0);
        GridLayout layoutParent = (GridLayout) layoutParentScroll.getChildAt(0);

        // Given the layoutGrandParent, we use the setTag method to associate the tab object, being able to instanciate it later


        numDenRecordMap.put(tabConfiguration.getTabId(), new NumDenRecord());

        BigDecimal decimalNumber;
        // We do this to have a default value in the ddl
        Option defaultOption = new Option(Constants.DEFAULT_SELECT_OPTION);

        Log.i(".Layout", "Get View For Tab");
        TabHost tabHost = (TabHost)mainActivity.findViewById(R.id.tabHost);
        tabHost.setup();

        Log.i(".Layout", "Generate Tab");
        String name = tab.getName();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(Long.toString(tab.getId())); // Here we set the tag, we'll use later to move between tabs
        tabSpec.setIndicator(name);
        tabSpec.setContent(tabConfiguration.getTabId());
        tabHost.addTab(tabSpec);

        if (tabConfiguration.getLayoutId() != null){

            View customView = inflater.inflate(tabConfiguration.getLayoutId(), layoutParent, false);

            switch (tabConfiguration.getLayoutId()){
                case R.layout.scoretab:
                    layoutParent.addView(customView);
                    break;
                case R.layout.reportingtab:
                    ListView list=(ListView) customView.findViewById(R.id.listView);
                    ArrayAdapter<ReportingResults> adapter = new ReportingResultsArrayAdapter(mainActivity, LoadCustomQuestions.addReportingQuestions());
                    list.setAdapter(adapter);
                    layoutParent.addView(customView);
                    break;
                case R.layout.adherencetab:
                    ListView list_supervision = (ListView) customView.findViewById(R.id.listTestSupervisor);
                    //ArrayAdapter<DataHolder> adapterSupervision = new IQATestArrayAdapter(mainActivity, )
                    break;

                case R.layout.iqatab:
                    //Mi mierda
                    break;

            }

            return tabConfiguration.getLayoutId();
        }

        Log.i(".Layout", "Generate Headers");
        int child = -1;
        for (Header header: tab.getHeaders()){
            // First we introduce header text according to the template
            child = R.layout.headers;
            //Log.i(".Layout", "Reading header " + header.toString());
            View headerView = inflater.inflate(child, layoutParent, false);

            TextView headerText = (TextView) headerView.findViewById(R.id.headerName);
            headerText.setBackgroundResource(R.drawable.background_header);
            headerText.setText(header.getName());
            //Set Visibility to false until we check if it has any question visible
            headerView.setVisibility(View.GONE);
            layoutParent.addView(headerView);


            //Log.i(".Layout", "Reader questions for header " + header.toString());
            for (Question question : header.getQuestions()){
                // The statement is present in every kind of question
                TextView statement;
                switch(question.getAnswer().getOutput()){
                    case Constants.DROPDOWN_LIST:
                        child = R.layout.ddl;
                        View questionView = inflater.inflate(child, layoutParent, false);
                        questionView.setBackgroundResource(backgrounds[iterBacks%backgrounds.length]);

                        statement = (TextView) questionView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        statement.setTag(tabConfiguration.getTabId());
                        TextView denominator = (TextView) questionView.findViewById(R.id.den);
                        // If the question has children, we load the denominator, else we hide the question
                        if (!question.hasParent()) {
                            if (question.hasChildren()) {
                                questionView.setBackgroundResource(R.drawable.background_parent);
                            }
                            denominator.setText(Utils.round(question.getDenominator_w()));
                            //set header to visible
                            headerView.setVisibility(View.VISIBLE);

                            numDenRecordMap.get(tabConfiguration.getTabId()).addRecord(question, 0F, question.getDenominator_w());
                        } else {
                            questionView.setVisibility(View.GONE);
                        }

                        Spinner dropdown = (Spinner)questionView.findViewById(R.id.answer);
                        //dropdown.setTag(question);

                        dropdown.setTag(R.id.QuestionTag, question);
                        dropdown.setTag(R.id.HeaderTag, headerView);


                        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                                Spinner spinner = (Spinner) parentView;
                                Option triggeredOption = (Option) spinner.getItemAtPosition(position);

                                Question triggeredQuestion = (Question) spinner.getTag(R.id.QuestionTag);
                                TextView numeratorView = (TextView) Utils.findParentRecursively(spinner, R.id.ddl).findViewById(R.id.num);
                                TextView denominatorView = (TextView) Utils.findParentRecursively(spinner, R.id.ddl).findViewById(R.id.den);
                                TextView statementView = (TextView) Utils.findParentRecursively(spinner, R.id.ddl).findViewById(R.id.statement);
                                TextView partialScoreView = (TextView) Utils.findParentRecursively(spinner, Utils.getLayoutIds()).findViewById(R.id.score);
                                int generalScoreId = ((Integer) partialScoreView.getTag()).intValue();
                                TextView generalScoreView = (TextView) Utils.findParentRecursively(spinner, R.id.Grid).findViewById(generalScoreId);
                                TextView numSubtotal = (TextView) ((LinearLayout) Utils.findParentRecursively(spinner, Utils.getLayoutIds())).findViewById(R.id.total_num);
                                TextView denSubtotal = (TextView) ((LinearLayout) Utils.findParentRecursively(spinner, Utils.getLayoutIds())).findViewById(R.id.total_den);
                                Float numerator, denominator;

                                if (triggeredOption.getName() != null && triggeredOption.getName() != Constants.DEFAULT_SELECT_OPTION) { // This is for capture the user selection
                                    // First we do the calculus
                                    numerator = triggeredOption.getFactor() * triggeredQuestion.getNumerator_w();
                                    Log.i(".Layout", "numerator: " + numerator);
                                    denominator = new Float(0.0F);

                                    if (triggeredQuestion.getNumerator_w().compareTo(triggeredQuestion.getDenominator_w()) == 0) {
                                        denominator = triggeredQuestion.getDenominator_w();
                                        Log.i(".Layout", "denominator: " + denominator);
                                    } else {
                                        if (triggeredQuestion.getNumerator_w().compareTo(new Float(0.0F)) == 0 && triggeredQuestion.getDenominator_w().compareTo(new Float(0.0F)) != 0) {
                                            denominator = triggeredOption.getFactor() * triggeredQuestion.getDenominator_w();
                                            Log.i(".Layout", "denominator: " + denominator);
                                        }
                                    }

                                    numDenRecordMap.get((Integer) statementView.getTag()).addRecord(triggeredQuestion, numerator, denominator);

                                    // If the option is changed to positive numerator and has children, we need to show the children and take their denominators into account
                                    if (triggeredQuestion.hasChildren()) {
                                        View parent = Utils.findParentRecursively(spinner, Utils.getLayoutIds());
                                        View child;
                                        for (Question childQuestion : triggeredQuestion.getQuestionChildren()) {
                                            if (position == 1) { //FIXME: There must be a smarter way for saying "if the user selected yes"
                                                Utils.toggleVisible(parent, childQuestion, View.VISIBLE);
                                                numDenRecordMap.get((Integer) statementView.getTag()).addRecord(childQuestion, 0F, childQuestion.getDenominator_w());
                                            } else {
                                                Utils.toggleVisible(parent, childQuestion, View.GONE);
                                                numDenRecordMap.get((Integer) statementView.getTag()).deleteRecord(childQuestion);
                                            }
                                        }
                                    }

                                    numeratorView.setText(Utils.round(numerator));
                                    denominatorView.setText(Utils.round(denominator));

                                } else {
                                    // This is for capturing the event when the user leaves the dropdown list without selecting any option
                                    numerator = new Float(0.0F);
                                    denominator = triggeredQuestion.getDenominator_w();
                                    if (selectedItemView != null) {
                                        numeratorView.setText(Utils.round(numerator));
                                        denominatorView.setText(Utils.round(denominator));
                                    }
                                    numDenRecordMap.get((Integer) statementView.getTag()).addRecord(triggeredQuestion, numerator, denominator);
                                }


                                List<Float> numDenSubTotal = numDenRecordMap.get((Integer) statementView.getTag()).calculateNumDenTotal();

                                if (numSubtotal != null && denSubtotal != null && partialScoreView != null) {
                                    numSubtotal.setText(Utils.round(numDenSubTotal.get(0)));
                                    denSubtotal.setText(Utils.round(numDenSubTotal.get(1)));
                                    float score = (numDenSubTotal.get(0) / numDenSubTotal.get(1)) * 100;
                                    partialScoreView.setText(Utils.round(score)); // We set the score in the tab score
                                    generalScoreView.setText(Utils.round(score)); // We set the score in the score tab
                                }

                                // Then we set the score in the Score tab

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parentView) {
                                // your code here
                            }

                        });

                        List<Option> optionList = question.getAnswer().getOptions();
                        optionList.add(0, defaultOption);
                        ArrayAdapter adapter = new ArrayAdapter(mainActivity, android.R.layout.simple_spinner_item, optionList);
                        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        dropdown.setAdapter(adapter);
                        layoutParent.addView(questionView);
                        break;
                    case Constants.INT:
                        child = R.layout.integer;
                        View questionIntView = inflater.inflate(child, layoutParent, false);
                        questionIntView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        statement = (TextView) questionIntView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerI = (EditText) questionIntView.findViewById(R.id.answer);
                        layoutParent.addView(questionIntView);
                        //set header to visible
                        headerView.setVisibility(View.VISIBLE);
                        break;
                    case Constants.LONG_TEXT:
                        child = R.layout.longtext;
                        View questionLTView = inflater.inflate(child, layoutParent, false);
                        questionLTView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        statement = (TextView) questionLTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerLT = (EditText) questionLTView.findViewById(R.id.answer);
                        layoutParent.addView(questionLTView);
                        //set header to visible
                        headerView.setVisibility(View.VISIBLE);
                        break;
                    case Constants.SHORT_TEXT:
                        child = R.layout.shorttext;
                        View questionSTView = inflater.inflate(child, layoutParent, false);
                        questionSTView.setBackgroundResource(backgrounds[iterBacks % backgrounds.length]);
                        statement = (TextView) questionSTView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerST = (EditText) questionSTView.findViewById(R.id.answer);
                        layoutParent.addView(questionSTView);
                        //set header to visible
                        headerView.setVisibility(View.VISIBLE);
                        break;
                    case Constants.SHORT_DATE: case Constants. LONG_DATE:
                        child = R.layout.date;
                        View questionSDView = inflater.inflate(child, layoutParent, false);
                        questionSDView.setBackgroundResource(backgrounds[iterBacks%backgrounds.length]);
                        statement = (TextView) questionSDView.findViewById(R.id.statement);
                        statement.setText(question.getForm_name());
                        EditText answerSD = (EditText) questionSDView.findViewById(R.id.answer);
                        layoutParent.addView(questionSDView);
                        //set header to visible
                        headerView.setVisibility(View.VISIBLE);
                        break;
                }
                iterBacks++;
            }
        }

        if (tabConfiguration.isAutomaticTab()) {
            // This layout is for showing the accumulated score
            GridLayout layoutParentScore = (GridLayout) layoutGrandParent.getChildAt(1);
            Log.i(".Layout", "Grandpa layout children: " + layoutGrandParent.getChildCount());
            child = R.layout.subtotal_num_dem;
            View subtotalView = inflater.inflate(child, layoutParentScore, false);
            TextView total_num_text = (TextView) subtotalView.findViewById(R.id.total_num);
            total_num_text.setText("0.0");
            TextView total_den_text = (TextView) subtotalView.findViewById(R.id.total_den);
            List<Float> numDenSubTotal = numDenRecordMap.get(tabConfiguration.getTabId()).calculateNumDenTotal();
            total_den_text.setText(Utils.round(numDenSubTotal.get(1)));
            layoutParentScore.addView(subtotalView);
            TextView subscore_text = (TextView) subtotalView.findViewById(R.id.score);
            int generalScoreId = tabConfiguration.getScoreFieldId();
            subscore_text.setTag(generalScoreId);
            Log.i(".Layout", "after generated tab: " + numDenSubTotal.get(0) + " " + numDenSubTotal.get(1));
        }

        return child;
    }
}


