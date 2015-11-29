package data;

import java.util.ArrayList;

/**
 * Exhibition.java Curator Agent monitors the gallery/museum. A gallery/museum
 * contains detailed information of artifacts such as: id, name, creator, date
 * of creation, place ofcreation, genre etc.
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 2.0
 */
public class Exhibition {

	private ArrayList<ExhibitionItem> items;
	private String exhibitionName;

	public Exhibition(String name) {
		items = new ArrayList<>();
		exhibitionName = name;
		createSampleItems();
	}

	public void addExhibitionItem(ExhibitionItem item) {
		item.setID(items.size());
		items.add(item);
	}

	public String getExhibitionName() {
		return exhibitionName;
	}

	public ArrayList<ExhibitionItem> getExhibitionItems() {
		return items;
	}

	private void createSampleItems() {
		ExhibitionItem item1 = new ExhibitionItem("Eiffeltower", 1000,
				"Architecture", "Male", 30);
		item1.setCreator("XXX");
		item1.setCreationPlace("Paris");
		ExhibitionItem item2 = new ExhibitionItem("Mona Lisa", 2000, "Art",
				"Female", 22);
		item2.setCreator("Leonaro da vinci");
		item2.setCreationPlace("Italy");
		ExhibitionItem item3 = new ExhibitionItem("The doors", 20, "Music",
				"Female", 22);
		item3.setCreator("Jim Morrison, etc");
		item3.setCreationPlace("USA");
		addExhibitionItem(item1);
		addExhibitionItem(item2);
		addExhibitionItem(item3);
	}
}
