package agents;

import data.ExhibitionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ArtistManagerAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class ArtistManagerAgent extends Agent {

	private ArrayList<ExhibitionItem> items_for_sale = new ArrayList<ExhibitionItem>();

	private final String ITEM_AUCTIONING_REGTYPE = "item-auctioning";
	private final String ITEM_FOR_SALE_ONTOLOGY = "item-for-sale";

	private void handleInput(Object[] args) {
		if (args != null) {
			String[] arguments = (String[]) args;
		}
	}

	private String createMsgString(ExhibitionItem item) {
		return item.getName() + "," + item.getGenre() + "," + item.getPrice();
	}

	private ArrayList<ExhibitionItem> loadDataFromFile(String filename) {
		ArrayList<ExhibitionItem> list = new ArrayList<ExhibitionItem>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				try {
					String[] lineItems = line.split(",");
					String itemName = lineItems[0];
					int itemPrice = Integer.parseInt(lineItems[1]);
					String genre = lineItems[2];
					String recGender = lineItems[3];
					int recAge = Integer.parseInt(lineItems[4]);
					list.add(new ExhibitionItem(itemName, itemPrice, genre,
							recGender, recAge));
				} catch (NumberFormatException e) {
					System.out.println(e);
				}

			}
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
		return list;
	}

	private ExhibitionItem createDummyItem() {
		ExhibitionItem item = new ExhibitionItem("Female", "Art", 17);
		item.setName("Mona Lisa");
		item.setPrice(200);
		return item;
	}

	@Override
	protected void setup() {
		super.setup();
		handleInput(getArguments());
		items_for_sale = loadDataFromFile("items.txt");

		addBehaviour(new TickerBehaviour(this, 6000) {
			@Override
			protected void onTick() {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology(ITEM_FOR_SALE_ONTOLOGY);
				if (items_for_sale.size() > 0) {
					msg.setContent(createMsgString(items_for_sale.get(0)));
					items_for_sale.remove(0);

					AID[] curators = lookForService("item-sending");
					for (AID curator : curators) {
						msg.addReceiver(curator);
					}
					send(msg);
				}
			}
		});

	}

	@Override
	protected void takeDown() {
		super.takeDown();
	}

	private AID[] lookForService(String serviceType) {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(serviceType);
		template.addServices(sd);
		AID[] agents = new AID[] {};
		try {
			DFAgentDescription[] result = DFService.search(this, template);
			agents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				agents[i] = result[i].getName();
			}
		} catch (FIPAException fe) {
			System.err.println(fe);
		}

		return agents;
	}
}
