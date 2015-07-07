package org.eyeseetea.malariacare.database.model;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CompositeScore extends SugarRecord<CompositeScore> {

    private static final String LIST_BY_PROGRAM_SQL="select distinct cs.* from composite_score cs left join question q on q.composite_score=cs.id "+
            "left join header h on q.header=h.id "+
            "left join tab t on h.tab=t.id "+
            "left join program p on t.program=p.id where p.id=?";

    String code;
    String label;
    String uid;
    CompositeScore compositeScore;

    @Ignore
    List<CompositeScore> _compositeScoreChildren;

    @Ignore
    List<Question> _questions;

    public CompositeScore() {
    }

    public CompositeScore(String code, String label, CompositeScore compositeScore) {
        this.code = code;
        this.label = label;
        this.compositeScore = compositeScore;
    }

    public CompositeScore(String code, String label, String uid, CompositeScore compositeScore) {
        this.code = code;
        this.label = label;
        this.uid = uid;
        this.compositeScore = compositeScore;
    }


    public String getCode() { return code; }

    public void setCode(String code) { this.code = code; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public CompositeScore getComposite_score() {
        return compositeScore;
    }

    public void setCompositeScore(CompositeScore compositeScore) {
        this.compositeScore = compositeScore;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean hasParent(){
        return getComposite_score() != null;
    }

    public List<CompositeScore> getCompositeScoreChildren() {
        if (this._compositeScoreChildren == null){
            this._compositeScoreChildren = CompositeScore.find(CompositeScore.class, "composite_score = ?", String.valueOf(this.getId()));
        }
        return this._compositeScoreChildren;
    }

    public List<Question> getQuestions(){
        if (_questions == null) {
            _questions = Select.from(Question.class)
                    .where(Condition.prop("composite_score")
                    .eq(String.valueOf(this.getId()))).list();
        }
        return _questions;
    }

    /**
     * Select all composite score that belongs to a program
     * @param program Program whose composite scores are searched.
     * @return
     */
    public static List<CompositeScore> listAllByProgram(Program program){
        if(program==null || program.getId()==null){
            return new ArrayList<>();
        }
        //Take scores associated to questions of the program ('leaves')
        List<CompositeScore> compositeScoresByProgram = CompositeScore.findWithQuery(CompositeScore.class, LIST_BY_PROGRAM_SQL, program.getId().toString());

        //Find parent scores from 'leaves'
        Set<CompositeScore> parentCompositeScores = new HashSet<CompositeScore>();
        for(CompositeScore compositeScore: compositeScoresByProgram){
            parentCompositeScores.addAll(listParentCompositeScores(compositeScore));
        }
        compositeScoresByProgram.addAll(parentCompositeScores);

        //return all scores
        return compositeScoresByProgram;
    }

    public static List<CompositeScore> listParentCompositeScores(CompositeScore compositeScore){
        ArrayList<CompositeScore> parentScores= new ArrayList<CompositeScore>();
        if(compositeScore==null || !compositeScore.hasParent()){
            return parentScores;
        }
        CompositeScore currentScore=compositeScore;
        while(currentScore!=null && currentScore.hasParent()){
            currentScore=currentScore.getComposite_score();
            parentScores.add(currentScore);
        }
        return parentScores;
    }

    public boolean hasChildren(){
        return !getCompositeScoreChildren().isEmpty();
    }

    @Override
    public String toString() {
        return "CompositeScore{" +
                "code='" + code + '\'' +
                ",label='" + label + '\'' +
                ", compositeScore=" + compositeScore +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositeScore that = (CompositeScore) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (compositeScore != null ? !compositeScore.equals(that.compositeScore) : that.compositeScore != null)
            return false;
        if (label != null ? !label.equals(that.label) : that.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (compositeScore != null ? compositeScore.hashCode() : 0);
        return result;
    }
}
