package agents;

import data.Exhibition;
import data.ExhibitionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SimpleAchieveREResponder;

/**
 * CuratorAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
 */
public class CuratorAgent extends Agent {

    private Exhibition exhibition = new Exhibition("DefaultName");

    private final String REQUEST_EXHIBITION_ITEMS_STRING = "request-exhibition-items";
    private final String REQUEST_EXHIBITION_ITEM_DETAILS_STRING = "request-exhibition-item-details";

    private final String ITEM_SENDING_LOOKUPSTRING = "item-sending";
    
    private final String SHORT_ITEM_DESC_STRING = "short-item-description";
    private final String FULL_ITEM_DESC_STRING = "full-item-description";

    private final Agent thisAgent = this;
    private DataStore dataStore = new DataStore();

    @Override
    protected void setup() {

        System.out.println("Curator-agent: "+getAID().getName()+" is ready.");
        initiateAuction();

        registerService("item-sending", "send-exhibition-content");
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();

        // BEHAVIOUR
        parallelBehaviour.addSubBehaviour(new SimpleAchieveREResponder(thisAgent,
                MessageTemplate.MatchOntology(REQUEST_EXHIBITION_ITEMS_STRING)){

            @Override
            protected ACLMessage prepareResponse(ACLMessage request) throws 
                    NotUnderstoodException, RefuseException {
            	System.out.println("Curator: received item list request (short description).");
                return getItemDescription("short", request);
            }

            @Override
            protected ACLMessage prepareResultNotification(ACLMessage request, 
                    ACLMessage response) throws FailureException {
                return super.prepareResultNotification(request, response);
            }
        });

        // BEHAVIOUR
        parallelBehaviour.addSubBehaviour(new SimpleAchieveREResponder(thisAgent,
                MessageTemplate.MatchOntology(REQUEST_EXHIBITION_ITEM_DETAILS_STRING)) {

            @Override
            protected ACLMessage prepareResponse(ACLMessage request) throws 
                    NotUnderstoodException, RefuseException {
            	System.out.println("Curator: received item list request (detailed description).");
                return getItemDescription("full", request);
            }
        });

        addBehaviour(parallelBehaviour);
    }
    
    @Override
    protected void takeDown() {
        try { DFService.deregister(this); }
        catch (FIPAException fe) {
            System.err.println(fe);
        }
        
        // Printout a dismissal message
        System.out.println("Curator-agent " + getAID().getName() + 
                " terminating.");
    }
    
    /*
     * TODO
     */
    private void initiateAuction() {
    	System.out.println("Curator: beginning a new auction.");
        AID[] profilers = lookForService("tour-visiting");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.setSender(getAID());
        msg.setContent("BEGINNING");
        
        for (AID profiler : profilers) {
            msg.addReceiver(profiler);
        }
        
        msg.setProtocol(InteractionProtocol.FIPA_DUTCH_AUCTION);
        send(msg);
        //addBehaviour(new AchieveREInitiator(this, msg));
    }

    private ACLMessage getItemDescription(String type, ACLMessage message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(message.getSender());
        msg.setSender(getAID());
        
        String messageContent = "";
        switch (type) {
            case "short":
                msg.setOntology(SHORT_ITEM_DESC_STRING);
                
                for (ExhibitionItem item : exhibition.getExhibitionItems()) {
                    messageContent += item.getShortItemDescription() + "," + 
                            getAID() + "\n";
                }
                
                break;
        
            case "full":
                msg.setOntology(FULL_ITEM_DESC_STRING);
                
                for (ExhibitionItem item : exhibition.getExhibitionItems()){
                    messageContent += item.getFullItemDescription() + "\n";
                }
        }
        
        msg.setContent(messageContent);
        System.out.println("Curator: sending list of items.");
        
        return msg;
    }

    private void registerService(String serviceType, String serviceName){
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            System.err.println(fe);
        }
    }
    
    private AID[] lookForService(String serviceType) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        template.addServices(sd);
        AID[] agents = new AID[]{};
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
