package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREInitiator;
import jade.util.leap.Iterator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * ArtistManagerAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class CuratorAuctionBehaviour extends AchieveREInitiator {
	private ArrayList<AID> auctioneers = new ArrayList<AID>();
	private int responses = 0;
	private int itemRealPrice;
	private Agent curatorAgent;
	private HashMap<AID, String> acceptedBids = new HashMap<AID, String>();

	public CuratorAuctionBehaviour(Agent a, ACLMessage msg) {
		super(a, msg);
		curatorAgent = a;
	}

	public CuratorAuctionBehaviour(Agent a, ACLMessage msg, int itemRealPrice) {
		super(a, msg);
		this.curatorAgent = a;
		this.itemRealPrice = itemRealPrice;
	}

	@Override
	protected Vector prepareRequests(ACLMessage request) {
		Iterator receivers = request.getAllReceiver();
		while (receivers.hasNext()) {
			auctioneers.add((AID) receivers.next());
		}
		return super.prepareRequests(request);
	}

	@Override
	protected void handleAgree(ACLMessage agree) {
		// Agrees to the bid.
		responses += 1;
		if (responses == auctioneers.size() && acceptedBids.size() == 0)
			System.out.print("Winnner!!");
		else if (responses == auctioneers.size() && acceptedBids.size() > 0) {
			System.out.println("Multiple winners!! " + agree.getContent());
		} else {
			acceptedBids.put(agree.getSender(), agree.getContent());
		}
		super.handleAgree(agree);
	}

	@Override
	protected void handleRefuse(ACLMessage refuse) {
		System.out.println("Profiler refused the auction! "
				+ refuse.getSender());
		auctioneers.remove(refuse.getSender());
		// Suppose the profiler do not want to be in the auction for some
		// reason.
		super.handleRefuse(refuse);
	}

	@Override
	protected void handleInform(ACLMessage inform) {
		// Rejects the bid.

		String[] content = inform.getContent().split(",");
		responses += 1;
		if (responses == auctioneers.size()) {
			if (acceptedBids.keySet().size() > 0) {
				// handle multiple winners somehow??
				System.out.println("Winners ");
			}
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setContent(inform.getContent());

			msg.removeReceiver(inform.getSender());
			for (AID agent : auctioneers)
				msg.addReceiver(agent);
			ACLMessage newMsg = decreasePrice(itemRealPrice, msg);
			if (newMsg.getPerformative() != ACLMessage.CANCEL) {
				newMsg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);
				curatorAgent.addBehaviour(new CuratorAuctionBehaviour(
						curatorAgent, newMsg));
			} else {
				System.out.println("Auction terminated at price " + content[3]);
			}

		}
		super.handleInform(inform);
	}

	private ACLMessage decreasePrice(int initialPrice, ACLMessage msg) {
		String[] content = msg.getContent().split(",");
		int price = Integer.parseInt(content[3]);
		int newPrice = price - 50;
		if (newPrice < initialPrice) {
			msg.setPerformative(ACLMessage.CANCEL);
			return msg;
		} else {
			msg.setPerformative(ACLMessage.INFORM);
			msg.setContent(content[1] + "," + content[2] + "," + newPrice);
			return msg;
		}
	}
}
