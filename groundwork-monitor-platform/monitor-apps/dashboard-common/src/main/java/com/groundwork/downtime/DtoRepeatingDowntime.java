package com.groundwork.downtime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DtoRepeatingDowntime {

    private Long id;
    private String year;
    private String month;
    private String day;
    private String week;

    private Boolean weekday0;
    private Boolean weekday1;
    private Boolean weekday2;
    private Boolean weekday3;
    private Boolean weekday4;
    private Boolean weekday5;
    private Boolean weekday6;

    private Integer count;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endDate;
    private Long downtimeId;

    public DtoRepeatingDowntime() {}
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWeek() {
        return week;
    }

    public void setWeek(String week) {
        this.week = week;
    }

    public Boolean getWeekday0() {
        return weekday0;
    }

    public void setWeekday0(Boolean weekday0) {
        this.weekday0 = weekday0;
    }

    public Boolean getWeekday1() {
        return weekday1;
    }

    public void setWeekday1(Boolean weekday1) {
        this.weekday1 = weekday1;
    }

    public Boolean getWeekday2() {
        return weekday2;
    }

    public void setWeekday2(Boolean weekday2) {
        this.weekday2 = weekday2;
    }

    public Boolean getWeekday3() {
        return weekday3;
    }

    public void setWeekday3(Boolean weekday3) {
        this.weekday3 = weekday3;
    }

    public Boolean getWeekday4() {
        return weekday4;
    }

    public void setWeekday4(Boolean weekday4) {
        this.weekday4 = weekday4;
    }

    public Boolean getWeekday5() {
        return weekday5;
    }

    public void setWeekday5(Boolean weekday5) {
        this.weekday5 = weekday5;
    }

    public Boolean getWeekday6() {
        return weekday6;
    }

    public void setWeekday6(Boolean weekday6) {
        this.weekday6 = weekday6;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getDowntimeId() {
        return downtimeId;
    }

    public void setDowntimeId(Long downtimeId) {
        this.downtimeId = downtimeId;
    }
}
