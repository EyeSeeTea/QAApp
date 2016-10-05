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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.common.primitives.Booleans;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.database.model.Header;
import org.eyeseetea.malariacare.database.model.Option;
import org.eyeseetea.malariacare.database.model.Question;
import org.eyeseetea.malariacare.database.utils.PreferencesState;
import org.eyeseetea.malariacare.database.utils.ReadWriteDB;
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
    private static final String zero = PreferencesState.getInstance().getContext().getString(R.string.number_zero);

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
            if(PreferencesState.getInstance().isShowNumDen()) {
                num.setText(numText);
                denum.setText(denumText);
            }
        }

        public void setNum(String numText) {
            if(PreferencesState.getInstance().isShowNumDen()) {
                num.setText(numText);
            }
        }
    }

    /**
     * Static class just to get the answer about the children deletion inside a listener
     */
    public static class QuestionVisibility{
        public static Question question;
        public static LinkedHashMap<BaseModel, Boolean> elementInvisibility;
        public static AutoTabAdapter adapter;
        public static Option option;
    }

    //Store the views references for each view in the footer
    public static class ScoreHolder {
        public CustomTextView subtotalscore;
        public CustomTextView score;
        public CustomTextView totalNum;
        public CustomTextView totalDenum;
        public CustomTextView qualitativeScore;
    }

    /**
     * Enables/Disables input view according to the state of the survey.
     * Sent surveys cannot be modified.
     *
     * @param view
     */
    public static void updateReadOnly(View view, boolean readOnly) {
        if (view == null) {
            return;
        }

        //RadioGroup is different
        if (view instanceof RadioGroup) {
            RadioGroup radioGroup = (RadioGroup) view;
            for (int i = 0; i < radioGroup.getChildCount(); i++) {
                radioGroup.getChildAt(i).setEnabled(!readOnly);
            }
            return;
        }

        view.setEnabled(!readOnly);        
    }

    /**
     * Checks if given question should be hidden according to the current survey or not.
     * @param question
     * @return
     */
    public static boolean isHidden(Question question, float idSurvey) {
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

    public static View initialiseDropDown(int position, ViewGroup parent, Question question, ViewHolder viewHolder, LayoutInflater lInflater, Context context) {
        View rowView;
        if(PreferencesState.getInstance().isShowNumDen()) {
            rowView = initialiseView(R.layout.ddl_scored, parent, question, viewHolder, position, lInflater);
            initialiseScorableComponent(rowView, viewHolder);
        }else{
            rowView = initialiseView(R.layout.ddl, parent, question, viewHolder, position, lInflater);
        }

        // In case the option is selected, we will need to show num/dems
        List<Option> optionList = new ArrayList<>(question.getAnswer().getOptions());
        optionList.add(0, new Option(Constants.DEFAULT_SELECT_OPTION));
        Spinner spinner = (Spinner) viewHolder.component;
        spinner.setAdapter(new OptionArrayAdapter(context, optionList));
        return rowView;
    }


    public static View initialiseView(int resource, ViewGroup parent, Question question, ViewHolder viewHolder, int position, LayoutInflater lInflater) {
        View rowView = lInflater.inflate(resource, parent, false);

        //Set background
        int background=R.drawable.background_parent;
        if (!question.hasChildren()) {
            background=LayoutUtils.calculateBackgrounds(position);
        }
        rowView.setBackgroundResource(background);        

        //Set statement
        CustomTextView statement = (CustomTextView) rowView.findViewById(R.id.statement);        
        if(question.getCompulsory()){
            Spanned spannedQuestion= Html.fromHtml(String.format("<font color=\"#%s\"><b>", compulsoryColorString) + "*  " + "</b></font>" + question.getForm_name());
            statement.setText(spannedQuestion);
        }else {
            statement.setText(question.getForm_name());
        }

        //Annotate component (to attach listeners and stuff)
        viewHolder.component = rowView.findViewById(R.id.answer);

        return rowView;
    }

    public static void initialiseScorableComponent(View rowView, ViewHolder viewHolder) {
        // In case the option is selected, we will need to show num/dems
        viewHolder.num = (CustomTextView) rowView.findViewById(R.id.num);
        viewHolder.denum = (CustomTextView) rowView.findViewById(R.id.den);
    }

    /**
     * Decide whether we need or not to hide this header (if every question inside is hidden)
     * @param header header that
     * @return true if every header question is hidden, false otherwise
     */
    public static boolean hideHeader(Header header, LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        // look in every question to see if every question is hidden. In case one cuestion is not hidden, we return false
        for (Question question : header.getQuestions()) {
            if (!elementInvisibility.get(question)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the number of elements that are hidden
     * @return number of elements hidden (true in elementInvisibility Map)
     */
    public static int getHiddenCount(LinkedHashMap<BaseModel, Boolean> elementInvisibility) {
        // using Guava library and its Booleans utility class
        return Booleans.countTrue(Booleans.toArray(elementInvisibility.values()));
    }

    public static void autoFillAnswer(AutoTabLayoutUtils.ViewHolder viewHolder, Question question, Context context, LinkedHashMap<BaseModel, Boolean> elementInvisibility, AutoTabAdapter adapter, float idSurvey, String module) {
        //FIXME Yes|No are 'hardcoded' here by using options 0|1
        int option=question.isTriggered(idSurvey)?0:1;

        //Select option according to trigger
        itemSelected(viewHolder, question, question.getAnswer().getOptions().get(option), context, elementInvisibility, adapter, idSurvey, module);
    }

    /**
     * Do the logic after a DDL option change
     * @param viewHolder private class that acts like a cache to quickly access the different views
     * @param question the question that changes his value
     * @param option the option that has been selected
     */
    public static void itemSelected(final AutoTabLayoutUtils.ViewHolder viewHolder, Question question, Option option, Context context, LinkedHashMap<BaseModel, Boolean> elementInvisibility, AutoTabAdapter adapter, final float idSurvey, final String module) {

        //No children -> save and calculate values
        if (!question.hasChildren()) {
            // Write option to DB
            ReadWriteDB.saveValuesDDL(question, option, module);
            recalculateScores(viewHolder, question, idSurvey, module);
            return;
        }

        QuestionVisibility.question = question;
        QuestionVisibility.elementInvisibility = elementInvisibility;
        QuestionVisibility.adapter = adapter;
        QuestionVisibility.option = option;

        //Children + Values -> Ask for confirm
        if (hasChildValues(question,module)) {
            new AlertDialog.Builder(context)
                    .setTitle(null)
                    .setMessage(context.getString(R.string.dialog_deleting_children))
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            expandChildren(viewHolder, idSurvey, module);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            QuestionVisibility.adapter.notifyDataSetChanged();
                        }
                    }).create().show();
            return;
        }

        //Children questions without values (nothing to confirm)
        expandChildren(viewHolder, idSurvey, module);
    }

    private static boolean hasChildValues(Question question, String module){
        for (Question childQuestion: question.getChildren()){
            if (childQuestion.getValueBySession(module)!=null && childQuestion.getOutput()!=Constants.DROPDOWN_LIST_DISABLED){
                return true;
            }
        }
        return false;
    }

    public static void expandChildren(ViewHolder viewHolder, float idSurvey, String module){
        // Write option to DB
        ReadWriteDB.saveValuesDDL(QuestionVisibility.question, QuestionVisibility.option, module);
        recalculateScores(viewHolder, QuestionVisibility.question, idSurvey, module);
        toggleChildrenVisibility(idSurvey, module);
        QuestionVisibility.adapter.notifyDataSetChanged();
    }

    /**
     * Recalculate num and denum of a quetsion, update them in cache vars and save the new num/denum in the score register associated with the question
     * @param viewHolder views cache
     * @param question question that change its values
     */
    public static void recalculateScores(AutoTabLayoutUtils.ViewHolder viewHolder, Question question, float idSurvey, String module) {
        Float num = ScoreRegister.calcNum(question, idSurvey);
        Float denum = ScoreRegister.calcDenum(question, idSurvey);
        //Update scores in register
        ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        if(num!=null){
            ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        }else {
            ScoreRegister.deleteRecord(question, idSurvey, module);
        }
        //update views
        updateViewHolderNumDen(viewHolder,num,denum);
    }

    /**
     * Updates views for num/den scores
     * @param viewHolder
     * @param num
     * @param den
     */
    private static void updateViewHolderNumDen(AutoTabLayoutUtils.ViewHolder viewHolder, Float num, Float den){
        if(viewHolder==null){
            return;
        }

        if(num!=null && den!=null){
            viewHolder.setNumAndDenum(num.toString(),den.toString());
            return;
        }
        viewHolder.setNumAndDenum(zero,zero);
    }

    /**
     * Given a question, make visible or invisible their children. In case all children in a header
     * became invisible, that header is also hidden
     */
    private static void toggleChildrenVisibility(float idSurvey, String module) {
        Question question = QuestionVisibility.question;
        LinkedHashMap<BaseModel, Boolean> elementInvisibility = QuestionVisibility.elementInvisibility;
        List<Question> children = question.getChildren();
        Question cachedQuestion = null;
        boolean visible;

        //Update each children -> visibility, score, value
        for (Question child : children) {
            Header childHeader = child.getHeader();
            visible=!child.isHiddenBySurvey(idSurvey);
            elementInvisibility.put(child, !visible);
            if (!visible) {
                List<Float> numdenum = ScoreRegister.getNumDenum(child, idSurvey, module);
                if (numdenum != null) {
                    ScoreRegister.deleteRecord(child, idSurvey, module);
                }
                ReadWriteDB.deleteValue(child, module); // when we hide a question, we remove its value
                // little cache to avoid double checking same
                if(cachedQuestion == null || (cachedQuestion.getHeader().getId_header() != child.getHeader().getId_header())) {
                    elementInvisibility.put(childHeader, AutoTabLayoutUtils.hideHeader(childHeader, elementInvisibility));
                }
            } else {
                elementInvisibility.put(childHeader, false);
            }
            cachedQuestion = question;
        }
    }

    public static void initScoreQuestion(Question question, float totalNum, float totalDenum, float idSurvey, String module) {

        //Not a (dropdown || radio) -> done
        if (question.getOutput() != Constants.DROPDOWN_LIST
                && question.getOutput() != Constants.RADIO_GROUP_HORIZONTAL
                && question.getOutput() != Constants.RADIO_GROUP_VERTICAL) {
            return;
        }

        //Init scores and register
        Float num = ScoreRegister.calcNum(question, idSurvey);
        if(num!=null) {
            Float denum = ScoreRegister.calcDenum(question, idSurvey);
            ScoreRegister.addRecord(question, num, denum, idSurvey, module);
        }
    }
}
