package data;

import java.util.Date;

/**
 * ExhibitionItem.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class ExhibitionItem {

    private long id;
    private String name;
    private String creator;
    private Date creatationDate;
    private String creationPlace;

    private String genre;
    private int recommendedAge;
    private String recommendedGender;

    public ExhibitionItem(String recommendedGender, String genre, 
            int recommendedAge) {

        this.recommendedGender = recommendedGender;
        this.genre = genre;
        this.recommendedAge = recommendedAge;
    }
    
    public void extendDescription(String name, String creator,
            Date creatationDate, String creationPlace) {
        this.name = name;
        this.creator = creator;
        this.creatationDate = creatationDate;
        this.creationPlace = creationPlace;
    }


    public String getShortItemDescription() {
        return id + "," + genre + "," + recommendedAge + "," + recommendedGender;
    }
    
    public String getFullItemDescription() {
        return id + "," + name + "," + creator + "," + creatationDate + "," 
                + creationPlace;
    }
    
    public void setID(int id) {
        this.id = id;
    }
     
    public String toString() {
        return "short-item-description," + id + "," + genre + "," + 
                recommendedAge + "," + recommendedGender;
    }

    public String getRecommendedGender() {
        return recommendedGender;
    }

    public int getRecommendedAge() {
        return recommendedAge;
    }

    public String getGenre() {
        return genre;
    }
}
