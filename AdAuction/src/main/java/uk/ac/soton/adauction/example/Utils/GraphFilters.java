package uk.ac.soton.adauction.example.Utils;

import java.time.LocalDate;

public class GraphFilters {
    private LocalDate startDate;
    private LocalDate endDate;
    private String age;
    private String gender;
    private String income;
    private String context;

    public GraphFilters(LocalDate startDate, LocalDate endDate, String age, String gender, String income, String context) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.age = age;
        this.gender = gender;
        this.income = income;
        this.context = context;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getIncome() {
        return income;
    }

    public void setIncome(String income) {
        this.income = income;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}