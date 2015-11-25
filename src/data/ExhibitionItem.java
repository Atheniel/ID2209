package data;

import java.util.Date;


/**
 * Created by peter on 11/13/15.
 */
public class ExhibitionItem {

    private long id;
    private String name;
    private String creator;
    private Date creatationDate;
    private String creationPlace;
    private int price;

    private String genre;
    private int recommendedAge;
    private String recommendedGender;

    public ExhibitionItem(String recommendedGender, String genre, int recommendedAge) {

        this.recommendedGender = recommendedGender;
        this.genre = genre;
        this.recommendedAge = recommendedAge;
    }
    public void extendDescription(String name, String creator,Date creatationDate, String creationPlace){
        this.name = name;
        this.creator = creator;
        this.creatationDate = creatationDate;
        this.creationPlace = creationPlace;
    }


    public String getShortDescription(){
        return "short-item-description,"+id+","+genre+","+recommendedAge+","+recommendedGender;
    }
    public String getFullItemDescription(){
        return "full-item-description,"+id+","+name+","+creator+","+creatationDate+","+creationPlace;
    }
    public void setID(int id){
        this.id = id;
    }
    public String toString(){
        return "short-item-description,"+id+","+genre+","+recommendedAge+","+recommendedGender;
    }

    public String getName(){return name;}
    public void setName(String name){this.name = name;}
    public String getRecommendedGender() {
        return recommendedGender;
    }

    public int getRecommendedAge() {
        return recommendedAge;
    }

    public String getGenre() {
        return genre;
    }

    public int getPrice(){
        return price;
    }
    public void setPrice(int price){this.price = price;}
}
