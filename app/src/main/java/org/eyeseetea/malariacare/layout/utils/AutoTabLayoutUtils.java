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

package org.eyeseetea.malariacare.layout.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.model.OptionDB;
import org.eyeseetea.malariacare.data.database.model.QuestionDB;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.data.database.utils.ReadWriteDB;
import org.eyeseetea.malariacare.layout.adapters.general.OptionArrayAdapter;
import org.eyeseetea.malariacare.layout.adapters.survey.AutoTabAdapter;
import org.eyeseetea.malariacare.layout.score.ScoreRegister;
import org.eyeseetea.malariacare.utils.Constants;
import org.eyeseetea.malariacare.views.CustomRadioButton;
import org.eyeseetea.malariacare.views.CustomTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by adrian on 15/08/15.
 */
public class AutoTabLayoutUtils {

    private static final String TAG = ".ATLayoutUtils";
    private static String compulsoryColorString;
    private static final String ZERO = PreferencesState.getInstance().getContext().getString(R.string.number_zero);

    /**
     * Inits red color to avoid going into resources every time
     */
    public static void init(){
        int red = PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed);
        compulsoryColorString = String.format("%X", red).substring(2);
    }

    //Store the Views references for each row (to avoid many calls to getViewById)
    public static class ViewHolder {

        // Main component in the row: Spinner, EditText or RadioGroup
        public View component;

        public CustomTextView num;
        public CustomTextView denum;
        public int type;

        /**
         * Fixes a bug in older apis where a RadioGroup cannot find its children by id
         * @param id
         * @return
         */
        public CustomRadioButton findRadioButtonById(int id){
            //No component -> done
            if (component==null || ! (component instanceof RadioGroup)){
                return null;
            }

            //Modern api -> delegate in its method
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN){
                return (CustomRadioButton)component.findViewById(id);
            }

            //Find button manually
            for(int i=0;i<((RadioGroup) component).getChildCount();i++){
                View button=((RadioGroup) component).getChildAt(i);
                if(button.getId()==id){
                    return (CustomRadioButton) button;
                }
            }
            return null;
        }

        public void setNumAndDenum(String numText, String denumText) {
            if(PreferencesState.getInstance().isDevelopOptionActive()) {
                num.setText(numText);
                denum.setText(denumText);
            }
        }

        public void setNum(String numText) {
            if(PreferencesState.getInstance().isDevelopOptionActive()) {
                num.setText(numText);
            }
        }
    }

    /**
     * Static class just to get the answer about the children deletion inside a listener
     */
    public static class QuestionVisibility{
        public static QuestionDB question;
        public static LinkedHashMap<BaseModel, Boolean> elementInvisibility;
        public static AutoTabAdapter adapter;
        public static OptionDB option;
    }

    //Store the views references for each view in the footer
    public static class ScoreHolder {
        public CustomTextView subtotalscore;
        public CustomTextView score;
        public CustomTextView totalNum;
        public CustomTextView totalDenum;
        public CustomTextView qualitativeScore;
    }

    public static void updateReadOnly(AutoTabViewHolder viewHolder, QuestionRow questionRow, boolean readonly){
        if(viewHolder==null || questionRow==null){
            return;
        }

        for(int i=0;i<questionRow.sizeColumns();i++){
            View component = viewHolder.getColumnComponent(i);
            QuestionDB question = questionRow.getQuestions().get(i);

            updateReadOnly(component,question,readonly);
        }
    }

    /**
     * Enables/Disables input view according to the state of the survey.
     * Sent surveys cannot be modified.
     *
     * @param view
     */
    public static void updateReadOnly(View view, QuestionDB question, boolean readOnly) {
        if (view == null || question==null) {
            return;
        }

        //RadioGroup is different
        if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(!readOnly);
            }
        } else {
            view.setEnabled(!readOnly);
        }
    }

    /**
     * Checks if given question should be hidden according to the current survey or not.
     * @param question
     * @return
     */
    public static boolean isHidden(QuestionDB question, float idSurvey) {
        return question.isHiddenBySurvey(idSurvey);
    }

    /**
     * Given a desired position (that means, the position shown in the screen) of an element, get the
     * real position (that means, the position in the stored items list taking into account the hidden
     * elements)
     * @param position
     * @return the real position in the elements list
     */
    public static int getRealPosition(int position, LinkedHashMap<BaseModel, Boolean> elementInvisibility, List<? extends BaseModel> items){
        int hElements = getHiddenCountUpTo(position, elementInvisibility);
        int diff = 0;

        for (int i = 0; i < hElements; i++) {
            diff++;
            if (elementInvisibility.get(items.get(position + diff))) i--;
        }
        return (position + diff);
    }

    /**
     * Get the number of elements that are hidden until a given position
     * @param position
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    private static int getHiddenCountUpTo(int position, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        boolean [] upper = Arrays.copyOfRange(Booleans.toArray(elementInvisibility.values()), 0, position + 1);
        int hiddens = Booleans.countTrue(upper);
        return hiddens;
    }

    public static View initialiseDropDown(int position, ViewGroup parent, QuestionDB question, AutoTabViewHolder viewHolder, LayoutInflater lInflater, Context context) {
        View rowView;
        if(PreferencesState.getInstance().isDevelopOptionActive()) {
            rowView = initialiseView(R.layout.ddl_scored, parent, question, viewHolder, position, lInflater);
            initialiseScorableComponent(rowView, viewHolder);
        }else{
            rowView = initialiseView(R.layout.ddl, parent, question, viewHolder, position, lInflater);
        }

        // In case the option is selected, we will need to show num/dems
        List<OptionDB> optionList = new ArrayList<>(question.getAnswer().getOptions());
        optionList.add(0, new OptionDB(Constants.DEFAULT_SELECT_OPTION));
        Spinner spinner = (Spinner) viewHolder.component;
        spinner.setAdapter(new OptionArrayAdapter(context, optionList));
        return rowView;
    }

    public static View initialiseView(int resource, ViewGroup parent, QuestionDB question, AutoTabViewHolder viewHolder, int position, LayoutInflater lInflater) {
        View rowView = lInflater.inflate(resource, parent, false);
        if (question.hasChildren())
            rowView.setBackgroundResource(R.drawable.background_parent);
        else
            rowView.setBackgroundResource(LayoutUtils.calculateBackgrounds(position));

        viewHolder.component = rowView.findViewById(R.id.answer);
        viewHolder.statement = (CustomTextView) rowView.findViewById(R.id.statement);
        String questionFormHtml = question.getForm_name();
        String questionUId = "";
        if(PreferencesState.getInstance().isDevelopOptionActive()) {
            questionUId = " <a href=\"" + PreferencesState.getInstance().getServer().getUrl()
                    + PreferencesState.getInstance().getContext().getString(
                    R.string.api_data_elements) + question.getUid() + "\">(" + question.getUid()
                    + ")</a>";
        }
        if(question.getCompulsory()){
            int red = PreferencesState.getInstance().getContext().getResources().getColor(R.color.darkRed);
            String appNameColorString = String.format("%X", red).substring(2);
            Spanned compulsoryMark= Html.fromHtml(String.format("<font color=\"#%s\"><b>", appNameColorString) + "*  " + "</b></font>");

            viewHolder.statement.setText(compulsoryMark);
            viewHolder.statement.append(questionFormHtml);
            viewHolder.statement.append(Html.fromHtml(questionUId));
        }else{
            viewHolder.statement.setText(questionFormHtml);
            viewHolder.statement.append(Html.fromHtml(questionUId));
        }
        viewHolder.statement.setMovementMethod(LinkMovementMethod.getInstance());


        return rowView;
    }

    public static void initialiseScorableComponent(View rowView, AutoTabViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (CustomTextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (CustomTextView) rowView.findViewById(R.id.den);

        configureViewByPreference(viewHolder);
    }

    public static void createRadioGroupComponent(QuestionDB question, AutoTabViewHolder viewHolder, int orientation, LayoutInflater lInflater, Context context) {
        ((RadioGroup) viewHolder.component).setOrientation(orientation);

        for (OptionDB option : question.getAnswer().getOptions()) {
            CustomRadioButton button = (CustomRadioButton) lInflater.inflate(R.layout.uncheckeable_radiobutton, null);
            button.setOption(option);
            button.updateProperties(PreferencesState.getInstance().getScale(), context.getString(R.string.font_size_level1), context.getString(R.string.medium_font_name));
            ((RadioGroup) viewHolder.component).addView(button);
        }
    }

    /**
     * Set visibility of numerators and denominators depending on the user preference selected in the settings activity
     *
     * @param viewHolder view that holds the component to be more efficient
     */
    private static void configureViewByPreference(AutoTabViewHolder viewHolder) {
        int visibility = View.GONE;

        if (PreferencesState.getInstance().isDevelopOptionActive()) {
            visibility = View.VISIBLE;
        }

        viewHolder.num.setVisibility(visibility);
        viewHolder.denum.setVisibility(visibility);
    }

    public static void autoFillAnswer(AutoTabViewHolder viewHolder, QuestionDB question, Context context, AutoTabInVisibilityState inVisibilityState, AutoTabAdapter adapter, float idSurvey, String module) {
        //FIXME Yes|No are 'hardcoded' here by using options 0|1
        int optionPosition=question.isTriggered(idSurvey)?0:1;

        //Build selected item
        OptionDB option=question.getAnswer().getOptions().get(optionPosition);
        AutoTabSelectedItem autoTabSelectedItem = new AutoTabSelectedItem(adapter,inVisibilityState, idSurvey,module).buildSelectedItem(question,option,viewHolder,idSurvey,module);

        //Select that item to force related switch
        itemSelected(autoTabSelectedItem, idSurvey, module);
    }

    /**
     * Do the logic after a DDL option change
     * @param autoTabSelectedItem
     */
    public static void itemSelected(final AutoTabSelectedItem autoTabSelectedItem, final float idSurvey, final String module) {

        final QuestionDB question = autoTabSelectedItem.getQuestion();
        final OptionDB option = autoTabSelectedItem.getOption();
        Context context = autoTabSelectedItem.getContext();
        final AutoTabViewHolder viewHolder = autoTabSelectedItem.getViewHolder();

        //No children -> Save, check scores, done.
        if (!question.hasChildren()) {
            // Write option to DB
            ReadWriteDB.saveValuesDDL(question, option, module);
            recalculateScores(viewHolder, question, idSurvey,module);
            return;
        }

        //Children -> Save, Expand|Collapse, Notify, ..

        //No children answers will be deleted -> Save, Expand|Collapse
        if (!isRemovingValuesFromChildren(question, module)) {
            saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
            return;
        }

        processParentQuestionWithoutQuestion(autoTabSelectedItem, idSurvey, module, question, context);
    }

    /**
     * Process the parent question with active childs
     * @param
     * @return
     */
    private static void processParentQuestionWithoutQuestion(AutoTabSelectedItem autoTabSelectedItem, float idSurvey, String module, QuestionDB question, Context context) {
        int maxActiveParentsForActiveChild=0;
        for(QuestionDB childQuestion:question.getChildren()) {
            if (childQuestion.isHiddenBySurvey(idSurvey) == false) {
                if (childQuestion.getValueBySurvey(idSurvey) != null) {
                    int activeParents = childQuestion.numberOfActiveParents(idSurvey);
                    if (maxActiveParentsForActiveChild < activeParents)
                        maxActiveParentsForActiveChild = activeParents;
                    break;
                }
            }
        }
        //The current question  not have relevant values to delete children (0 active or more than 1 or question without value)
        if(maxActiveParentsForActiveChild!=1 || question.getOptionBySurvey(idSurvey)==null) {
            saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
            return;
        }
        //The current question  not have relevant values to delete children(1 active parent, clicked question not null but not relevant for active children)
        else if(maxActiveParentsForActiveChild==1
                && question.getOptionBySurvey(idSurvey)!=null
                && !question.getOptionBySurvey(idSurvey).isActiveChildren(question)){
            saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
            return;
        }
        //ask and delete the children questions
        askDeleteChildrenQuestion(autoTabSelectedItem, idSurvey, module, question, context);
    }

    /**
     * Children answers will be deleted -> Confirm -> Save, Expand|Collapse
     * @param
     * @return
     */
    private static void askDeleteChildrenQuestion(final AutoTabSelectedItem autoTabSelectedItem, final float idSurvey, final String module, final QuestionDB question, Context context) {
        new AlertDialog.Builder(context)
                .setTitle(null)
                .setMessage(context.getString(R.string.dialog_deleting_children))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        saveAndExpandChildren(autoTabSelectedItem, idSurvey, module);
                        //Remove the children when the option is the match option
                        if (autoTabSelectedItem.getOption().isActiveChildren(question)) {
                            AutoTabSelectedItem positiveAutoTabSelectedItem = autoTabSelectedItem;
                            positiveAutoTabSelectedItem.setOption(new OptionDB(Constants.DEFAULT_SELECT_OPTION));
                            saveAndExpandChildren(positiveAutoTabSelectedItem, idSurvey, module);
                        }
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        autoTabSelectedItem.notifyDataSetChanged();
                    }
                }).create().show();
    }

    /**
     * Looks for a children question with value (that is going to be removed due to a parent question 'No')
     * @param question
     * @return
     */
    private static boolean isRemovingValuesFromChildren(QuestionDB question, String module){
        for (QuestionDB childQuestion: question.getChildren()){
            if (childQuestion.getValueBySession(module)!=null && childQuestion.getOutput()!=Constants.DROPDOWN_LIST_DISABLED){
                return true;
            }
        }
        return false;
    }

    public static void saveAndExpandChildren(AutoTabSelectedItem autoTabSelectedItem, float idSurvey, String module){
        //Save value
        ReadWriteDB.saveValuesDDL(autoTabSelectedItem.getQuestion(), autoTabSelectedItem.getOption(), module);
        //Recalculate score
        recalculateScores(autoTabSelectedItem.getViewHolder(), autoTabSelectedItem.getQuestion(), idSurvey, module);
        //Toggle children
        autoTabSelectedItem.toggleChildrenVisibility(idSurvey, module);
        //Notify adapter
        autoTabSelectedItem.notifyDataSetChanged();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    private static void recalculateScores(AutoTabViewHolder viewHolder, QuestionDB question, float idSurvey, String module) {
        Float num = ScoreRegister.calcNum(question, idSurvey);
        Float denum = ScoreRegister.calcDenum(question, idSurvey);
        //if the num is null, the question haven't a valid numerator, and the denominator should be ignored
        viewHolder.setNumText(ZERO);
        viewHolder.setDenumText(ZERO);
        if(num!=null){
            viewHolder.setNumText(num.toString());
            viewHolder.setDenumText(denum.toString());
            ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        }
        else
            ScoreRegister.deleteRecord(question, idSurvey, module);
    }

    public static void initScoreQuestion(QuestionDB question, float idSurvey, String module) {

        if (question.getOutput() == Constants.DROPDOWN_LIST
                || question.getOutput() == Constants.RADIO_GROUP_HORIZONTAL
                || question.getOutput() == Constants.RADIO_GROUP_VERTICAL) {

            Float num = ScoreRegister.calcNum(question, idSurvey);
            Float denum = ScoreRegister.calcDenum(question, idSurvey);
            if(num==null)
                denum=0f;
            ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        }
    }

    public static void initScoreQuestion(QuestionRow questionRow, float idSurvey, String module){
        for(QuestionDB question: questionRow.getQuestions()){
            initScoreQuestion(question, idSurvey, module);
        }
    }
}
