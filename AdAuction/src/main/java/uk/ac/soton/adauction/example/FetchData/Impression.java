package uk.ac.soton.adauction.example.FetchData;


import java.sql.Timestamp;

/**
 * Object for a row of impression data
 */
public class Impression {
    private Timestamp dateAndTime;
    private Long id;
    private String gender;
    private String age;
    private String income;
    private String context;
    private Double impression_Cost;

    public Impression(Timestamp dateAndTime, Long id, String gender, String age, String income, String context, Double impression_Cost) {
        this.dateAndTime = dateAndTime;
        this.id = id;
        this.gender = gender;
        this.age = age;
        this.income = income;
        this.context = context;
        this.impression_Cost = impression_Cost;
    }

    public Timestamp getDateAndTime() {
        return dateAndTime;
    }

    public long getId() {
        return id;
    }

    public String getGender() {
        return gender;
    }

    public String getAge() {
        return age;
    }

    public String getIncome() {
        return income;
    }

    public String getContext() {
        return context;
    }

    public Double getImpression_Cost() {
        return impression_Cost;
    }
}
