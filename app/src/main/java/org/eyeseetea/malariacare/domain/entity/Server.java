package org.eyeseetea.malariacare.domain.entity;

import static org.eyeseetea.malariacare.domain.utils.RequiredChecker.required;

public class Server {

    String url;
    NextScheduleMonths nextScheduleMatrix;

    public Server(String url, NextScheduleMonths nextScheduleMatrix){
        this.url = required(url,"url is required");
        this.nextScheduleMatrix = required(nextScheduleMatrix,"nextScheduleMatrix is required");
    }

    public String getUrl() {
        return url;
    }

    public NextScheduleMonths getNextScheduleMatrix() {
        return nextScheduleMatrix;
    }
}
