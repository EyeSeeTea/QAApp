/*
 * Copyright (c) 2017.
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

package org.eyeseetea.malariacare.domain.service;

import org.eyeseetea.malariacare.domain.entity.CompetentScoreClassification;

public class CompetentScoreCalculationDomainService {

    public final static float NON_CRITICAL_COMPETENT_SCORE_LIMIT = 80f;

    public CompetentScoreClassification calculateClassification(
            boolean hasCriticalStepsMissed, Float nonCriticalStepsScore) {

        if (!hasCriticalStepsMissed){
            if(nonCriticalStepsScore>= NON_CRITICAL_COMPETENT_SCORE_LIMIT){
                return CompetentScoreClassification.COMPETENT;
            } else {
                return CompetentScoreClassification.COMPETENT_NEEDS_IMPROVEMENT;
            }
        } else {
            return CompetentScoreClassification.NOT_COMPETENT;
        }
    }
}
