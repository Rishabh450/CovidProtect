package com.rishabh.covidprotect.Models;

public class ReportModel {
    public String id;
    public String img;
    public float location_lat;
    public float location_lon;
    public String report_time;


    public ReportModel(String id, String img, float location_lat, float location_lon, String report_time) {
        this.id=id;
        this.img = img;
        this.location_lat = location_lat;
        this.location_lon = location_lon;
        this.report_time = report_time;


    }


}