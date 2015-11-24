package data;

import java.util.ArrayList;

/**
 * Exhibition.java
 * Curator Agent monitors the gallery/museum. A gallery/museum contains 
 * detailed information of artifacts such as: id, name, creator, date of
 * creation, place ofcreation, genre etc.
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class Exhibition {

    private ArrayList<ExhibitionItem> items;
    private String exhibitionName;

    public Exhibition(String name){
        items = new ArrayList<>();
        exhibitionName = name;
        createSampleItems();
    }
    public void addExhibitionItem(ExhibitionItem item){
        items.add(item);
    }

    public String getExhibitionName(){
        return exhibitionName;
    }

    public ArrayList<ExhibitionItem> getExhibitionItems(){
        return items;
    }

    private void createSampleItems(){
        addExhibitionItem(new ExhibitionItem("Male","music",30));
        addExhibitionItem(new ExhibitionItem("Female","art",22));
    }
}
