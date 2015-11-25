package data;

import java.util.ArrayList;



public class Exhibition {

    private ArrayList<ExhibitionItem> items;
    private String exhibitionName;

    public Exhibition(String name){
        items = new ArrayList<ExhibitionItem>();
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
