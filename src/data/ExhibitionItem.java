package data;

import java.util.Date;

/**
 * ExhibitionItem.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 2.0
 */
public class ExhibitionItem {

    private int id;
    private String name;
    private String creator;
    //private Date creatationDate;
    private String creationPlace;
    private int price;
	private String curatorName;

    private String genre;
    private int recommendedAge;
    private String recommendedGender;

    public ExhibitionItem(String recommendedGender, String genre, 
            int recommendedAge) {

        this.recommendedGender = recommendedGender;
        this.genre = genre;
        this.recommendedAge = recommendedAge;
    }
    
    public ExhibitionItem(String name, int price, String genre,
			String recommendedGender, int recommendedAge) {
		this.name = name;
		this.price = price;
		this.recommendedGender = recommendedGender;
		this.genre = genre;
		this.recommendedAge = recommendedAge;
	}
    
    public void extendDescription(String name, String creator,
            Date creatationDate, String creationPlace) {
        this.name = name;
        this.creator = creator;
        //this.creatationDate = creatationDate;
        this.creationPlace = creationPlace;
    }


    public String getShortItemDescription() {
        return id + "," + genre + "," + recommendedAge + "," + recommendedGender;
    }
    
    public String getFullItemDescription() {
        return id + "," + name + "," + creator + "," + creationPlace;
    }

	public int getID() {
		return id;
	}

	public void setID(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getCreationPlace() {
		return creationPlace;
	}

	public void setCreationPlace(String creationPlace) {
		this.creationPlace = creationPlace;
	}

	public int getPrice() {
		return price;
	}

	public void setPrice(int price) {
		this.price = price;
	}

	public String getCuratorName() {
		return curatorName;
	}

	public void setCuratorName(String curatorName) {
		this.curatorName = curatorName;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public int getRecommendedAge() {
		return recommendedAge;
	}

	public void setRecommendedAge(int recommendedAge) {
		this.recommendedAge = recommendedAge;
	}

	public String getRecommendedGender() {
		return recommendedGender;
	}

	public void setRecommendedGender(String recommendedGender) {
		this.recommendedGender = recommendedGender;
	}
	
	public String toString() {
        return "short-item-description," + id + "," + genre + "," + 
                recommendedAge + "," + recommendedGender;
    }
}
