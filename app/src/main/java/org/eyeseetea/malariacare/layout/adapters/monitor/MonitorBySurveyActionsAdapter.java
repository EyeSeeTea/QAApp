package org.eyeseetea.malariacare.layout.adapters.monitor;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;

import org.eyeseetea.malariacare.R;
import org.eyeseetea.malariacare.data.database.utils.PreferencesState;
import org.eyeseetea.malariacare.domain.entity.Survey;
import org.eyeseetea.malariacare.layout.adapters.sectionDetail.SectionDetailAdapter;
import org.eyeseetea.malariacare.presentation.viewmodels.SectionViewModel;
import org.eyeseetea.malariacare.presentation.viewmodels.SurveyViewModel;
import org.eyeseetea.malariacare.utils.CompetencyUtils;
import org.eyeseetea.malariacare.utils.DateParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorBySurveyActionsAdapter extends SectionDetailAdapter {

    public interface OnItemMenuClickListener{
        void itemMenuClick(SurveyViewModel surveyViewModel);
    }

    private OnItemMenuClickListener onItemMenuClickListener;

    private SectionViewModel incompleteSection;
    private SectionViewModel completeSection;

    private Map<SectionViewModel, List<SurveyViewModel>> surveysMap = new HashMap<>();
    private Context context;

    private int numSections = 0;

    public MonitorBySurveyActionsAdapter(Context context) {
        this.context = context;
    }

    public void setSurveys(
            List<SurveyViewModel> incompleteSurveys,
            List<SurveyViewModel> completedSurveys) {

        surveysMap = new HashMap<>();

        incompleteSection = new
                SectionViewModel(
                String.format("%s (%d)", context.getString(R.string.survey_imcomplete_text),
                        incompleteSurveys.size()),
                R.color.red);

        completeSection = new
                SectionViewModel(
                String.format("%s (%d)", context.getString(R.string.survey_complete_text),
                        completedSurveys.size()),
                R.color.green);

        surveysMap.put(incompleteSection, incompleteSurveys);
        surveysMap.put(completeSection, completedSurveys);

        numSections = 2;

        super.refreshData();
    }

    public void setOnItemMenuClickListener(OnItemMenuClickListener listener){
        this.onItemMenuClickListener = listener;
    }

    @Override
    protected int getSectionsCount() {
        return numSections;
    }

    @Override
    protected int getItemsCountInSection(int section) {
        List<SurveyViewModel> surveysBySection;

        if (section == 0) {
            surveysBySection = surveysMap.get(incompleteSection);
        } else {
            surveysBySection = surveysMap.get(completeSection);
        }

        if (surveysBySection != null){
            return surveysBySection.size();
        } else {
            return 0;
        }
    }

    @Override
    protected RecyclerView.ViewHolder onCreateSectionViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey_section, parent, false);
        return new SurveySectionViewHolder(itemView);
    }

    @Override
    protected RecyclerView.ViewHolder onCreateRowViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_survey_detail, parent, false);
        return new SurveyDetailViewHolder(itemView);
    }

    @Override
    protected void onBindSectionViewHolder(RecyclerView.ViewHolder holder, int sectionPosition) {
        SectionViewModel sectionViewModel;

        if (sectionPosition == 0) {
            sectionViewModel = incompleteSection;
        } else {
            sectionViewModel = completeSection;
        }

        ((SurveySectionViewHolder) holder).bindView(sectionViewModel);

        holder.itemView.setOnClickListener(view -> expandOrCollapse(sectionViewModel));
    }

    private void expandOrCollapse(SectionViewModel sectionViewModel) {
        sectionViewModel.setExpanded(!sectionViewModel.isExpanded());

        for (SurveyViewModel surveyViewModel : surveysMap.get(sectionViewModel)) {
            surveyViewModel.setVisible(sectionViewModel.isExpanded());
        }

        super.refreshData();
    }

    @Override
    protected void onBindRowViewHolder(RecyclerView.ViewHolder holder, int sectionPosition,
            int rowPositionInSection) {
        if (sectionPosition == 0) {
            ((SurveyDetailViewHolder) holder).bindView(surveysMap.get(incompleteSection),
                    rowPositionInSection);
        } else {
            ((SurveyDetailViewHolder) holder).bindView(surveysMap.get(completeSection),
                    rowPositionInSection);
        }
    }

    class SurveySectionViewHolder extends RecyclerView.ViewHolder {

        private TextView surveySectionNameView;
        private LinearLayout surveySectionContainer;
        private ImageView expandCollapseView;

        public SurveySectionViewHolder(View itemView) {
            super(itemView);

            surveySectionNameView = itemView.findViewById(R.id.survey_section_name);
            surveySectionContainer = itemView.findViewById((R.id.survey_section_container));
            expandCollapseView = itemView.findViewById(R.id.expand_collapse_view);
        }

        void bindView(SectionViewModel section) {
            surveySectionNameView.setText(section.getTitle());
            surveySectionContainer.setBackgroundResource(section.getColor());

            if (section.isExpanded()) {
                expandCollapseView.setRotation(180);
            } else {
                expandCollapseView.setRotation(0);
            }
        }
    }

    class SurveyDetailViewHolder extends RecyclerView.ViewHolder {
        int itemOrder;

        private TextView orgUnitTextView;
        private TextView programTextView;
        private TextView competencyView;
        private TextView scheduledDateTextView;
        private ImageView dotsMenu;

        public SurveyDetailViewHolder(View itemView) {
            super(itemView);

            orgUnitTextView = itemView.findViewById(R.id.survey_org_unit_name);
            programTextView = itemView.findViewById(R.id.survey_program_name);
            competencyView = itemView.findViewById(R.id.survey_competency_view);
            scheduledDateTextView = itemView.findViewById(R.id.survey_schedule_date_view);
            dotsMenu = itemView.findViewById(R.id.menu_dots);
        }

        void bindView(List<SurveyViewModel> surveys, int position) {
            SurveyViewModel survey = surveys.get(position);
            itemOrder = position;

            orgUnitTextView.setText(survey.getOrgUnit());
            programTextView.setText(survey.getProgram());

            CompetencyUtils.setTextByCompetencyAbbreviation(competencyView, survey.getCompetency());

            scheduledDateTextView.setText(DateParser.getEuropeanFormattedDate(survey.getDate()));

            dotsMenu.setOnClickListener(view -> {
                if (onItemMenuClickListener != null){
                    onItemMenuClickListener.itemMenuClick(survey);
                }
            });

            assignBackgroundColor();

            if (survey.isVisible()) {
                itemView.setVisibility(View.VISIBLE);
                itemView.setLayoutParams(
                        new RecyclerView.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT));
            } else {
                itemView.setVisibility(View.GONE);
                itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
            }
        }

        private void assignBackgroundColor() {
            int colorId;
            if (itemOrder == 0 || itemOrder % 2 == 0) {
                colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.white);
                itemView.setBackgroundColor(colorId);
            } else {
                colorId = PreferencesState.getInstance().getContext().getResources().getColor(
                        R.color.white_grey);
                itemView.setBackgroundColor(colorId);
            }
        }
    }
}
