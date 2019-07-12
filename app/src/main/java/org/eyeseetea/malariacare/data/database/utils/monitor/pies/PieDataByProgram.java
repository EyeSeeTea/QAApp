/*
 * Copyright (c) 2016.
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

package org.eyeseetea.malariacare.data.database.utils.monitor.pies;

import org.eyeseetea.malariacare.data.database.model.ProgramDB;
import org.eyeseetea.malariacare.utils.AUtils;

/**
 * Created by idelcano on 23/08/2016.
 */
public class PieDataByProgram extends PieDataBase {
    /**
     * Type of program for this chart
     */
    private ProgramDB program;

    /**
     * Constructor per program
     * @param program
     */
    public PieDataByProgram(ProgramDB program) {
        this.program = program;
    }
    public String toJSON(String tipChat){
        String pieTitle = String.format("%s (%s)", AUtils.escapeQuotes(program.getName()), AUtils.escapeQuotes(program.getName()));
        String json = String.format(JSONFORMAT, pieTitle, tipChat, program.getId_program(), this.numA, this.numB, this.numC,this.numNA, program.getUid(), program.getUid());
        return json;
    }
}
