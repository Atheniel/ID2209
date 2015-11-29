package data;

import jade.core.AID;

/**
 * ProfilerData.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class ProfilerData {

    private int age;
    private String occupation;
    private String gender;
    private String[] interest;
    private AID aid;


    public ProfilerData(AID aid, int age, String occupation, String gender, 
            String[] interest) {
        this.aid = aid;
        this.age = age;
        this.occupation = occupation;
        this.gender = gender;
        this.interest = interest;
    }

    public int getAge() {
        return age;
    }

    public String getOccupation() {
        return occupation;
    }

    public String getGender() {
        return gender;
    }

    public String[] getInterest() {
        return interest;
    }
    
    public AID getAid() {
        return aid;
    }
}
