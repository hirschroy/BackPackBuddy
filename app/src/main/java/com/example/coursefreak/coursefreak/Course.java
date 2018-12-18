package com.example.coursefreak.coursefreak;

import java.util.ArrayList;
import java.util.List;

public class Course {
    public String courseID;
    public String name;
    public Double points;
    public Integer numLikes;
    public Double average;
    public String categories;
    public List<String> parsedCategories;
    public String requirements;
    public List<String> parsedRequirements;

    public Course() {
        // Default constructor required.
    }
    public Course(String courseID, String name, Double points,
                  Integer numLikes, Double average, String categories,
                  String requirements) {
        this.courseID = courseID;
        this.name = name;
        this.points = points;
        this.numLikes = numLikes;
        this.average = average;
        this.categories = categories;
        this.requirements = requirements;
        this.parseCatsReqs();
    }

    public void parseCatsReqs() {
        this.parseCategories();
        this.parseRequirements();
    }

    private void parseCategories() {
        String[] parts = this.categories.split("$");
        this.parsedCategories = new ArrayList<>();
        for(String s : parts) {
            this.parsedCategories.add(s);
        }
    }

    private void parseRequirements() {
        String[] parts = this.requirements.split("$");
        this.parsedRequirements = new ArrayList<>();
        for(String s : parts) {
            this.parsedRequirements.add(s);
        }
    }

    public String getCourseID() {
        return this.courseID;
    }

    public void setCourseID(String id) {
        this.courseID = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPoints() {
        return this.points;
    }

    public void setPoints(Double d) {
        this.points = d;
    }

    public Integer getNumLikes() {
        return this.numLikes;
    }

    public void setNumLikes(Integer numLikes) {
        this.numLikes = numLikes;
    }

    public Double getAverage() {
        return this.average;
    }

    public void setAverage(Double d) {
        this.average = d;
    }

    public String getCategories() {
        return this.categories;
    }

    public void setCategories(String s) {
        this.categories = s;
        this.parseCategories();
    }

    public String getRequirements() {
        return this.requirements;
    }

    public void setRequirements(String s) {
        this.requirements = s;
        this.parseRequirements();
    }
}