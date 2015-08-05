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

package org.eyeseetea.malariacare.database.model;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.eyeseetea.malariacare.database.AppDatabase;

/**
 * Created by adrian on 14/02/15.
 */
@Table(databaseName = AppDatabase.NAME)
public class Score extends BaseModel {

    @Column
    @PrimaryKey(autoincrement = true)
    Long id;
    @Column
    Float value;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "id_tab",
            columnType = Long.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    Tab tab;
    @Column
    String uid;

    public Score() {
    }

    public Score(Float real, Tab tab, String uid) {
        this.value = real;
        this.tab = tab;
        this.uid = uid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float real) {
        this.value = real;
    }

    public Tab getTab() {
        return tab;
    }

    public void setTab(Tab tab) {
        this.tab = tab;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score)) return false;

        Score score = (Score) o;

        if (!id.equals(score.id)) return false;
        if (value != null ? !value.equals(score.value) : score.value != null) return false;
        if (!tab.equals(score.tab)) return false;
        return !(uid != null ? !uid.equals(score.uid) : score.uid != null);

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + tab.hashCode();
        result = 31 * result + (uid != null ? uid.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Score{" +
                "id=" + id +
                ", value=" + value +
                ", tab=" + tab +
                ", uid='" + uid + '\'' +
                '}';
    }
}
