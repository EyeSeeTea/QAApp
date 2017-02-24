/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.eyeseetea.malariacare.data.database.iomodules.dhis.importer.models;

import org.hisp.dhis.client.sdk.android.api.persistence.flow.ProgramStageDataElementFlow;

import java.util.ArrayList;
import java.util.List;

public class ProgramStageDataElementExtended {
    ProgramStageDataElementFlow programStageDataElementFlow;

    public ProgramStageDataElementExtended(ProgramStageDataElementFlow programStageDataElementFlow){
        this.programStageDataElementFlow = programStageDataElementFlow;
    }
    public ProgramStageDataElementExtended(ProgramStageDataElementExtended programStageDataElementExtended){
        this.programStageDataElementFlow = programStageDataElementExtended.getProgramStageDataElementFlow();
    }

    public ProgramStageDataElementFlow getProgramStageDataElementFlow() {
        return programStageDataElementFlow;
    }

    public Boolean getCompulsory() {
        return programStageDataElementFlow.isCompulsory();
    }

    public DataElementExtended getDataElement() {
        return new DataElementExtended(programStageDataElementFlow.getDataElement());
    }

    //Returns dataElement uid
    public String getDataelement() {
        return programStageDataElementFlow.getDataElement().getUId();
    }

    public String getProgramStage() {
        return programStageDataElementFlow.getProgramStage().getUId();
    }

    public static List<ProgramStageDataElementExtended> getExtendedList(List<ProgramStageDataElementFlow> flowList) {
        List <ProgramStageDataElementExtended> extendedsList = new ArrayList<>();
        for(ProgramStageDataElementFlow flowPojo:flowList){
            extendedsList.add(new ProgramStageDataElementExtended(flowPojo));
        }
        return extendedsList;
    }
}
