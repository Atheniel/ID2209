package agents;

import data.Exhibition;
import data.ExhibitionItem;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPANames.InteractionProtocol;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREInitiator;
import jade.proto.AchieveREResponder;
import jade.proto.SimpleAchieveREResponder;


public class CuratorAgent extends Agent {

    private Exhibition exhibition = new Exhibition("DefaultName");

    private final String REQUEST_EXHIBITION_ITEMS_STRING = "request-exhibition-items";
    private final String REQUEST_EXHIBITION_ITEM_DETAILS_STRING = "request-exhibition-item-details";

    private final String ITEM_SENDING_LOOKUPSTRING = "item-sending";
    
    private final String SHORT_ITEM_DESC_STRING = "short-item-description";
    private final String FULL_ITEM_DESC_STRING = "full-item-description";


    private final String BUYING_ITEMS_REGTYPE = "item-buying";
    private final String BUYING_ITEMS_REGNAME = "exhibition-item-buying";

    private final Agent thisAgent = this;
    private DataStore dataStore = new DataStore();

    @Override
    protected void setup() {

        System.out.println("Curator-agent: "+getAID().getName()+" is ready.");
        initiateAuction(createDummyItem());

        registerService("item-sending", "send-exhibition-content");
        ParallelBehaviour parallelBehaviour = new ParallelBehaviour();

        parallelBehaviour.addSubBehaviour(new AchieveREResponder(thisAgent, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION)) {

            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                System.out.println("Häääärligt  " + request.getContent());
                if (request.getContent().equals("reject_bid")) {

                }
                return super.handleRequest(request);

            }
        });
            
        
        // BEHAVIOUR
        parallelBehaviour.addSubBehaviour(new SimpleAchieveREResponder(thisAgent,
                MessageTemplate.MatchOntology(REQUEST_EXHIBITION_ITEMS_STRING)){

            @Override
            protected ACLMessage prepareResponse(ACLMessage request) throws 
                    NotUnderstoodException, RefuseException {
            	System.out.println("Curator: received item list request (short description).");
                return getItemDescription("short", request);
            }

        });

        // BEHAVIOUR
        parallelBehaviour.addSubBehaviour(new SimpleAchieveREResponder(thisAgent,MessageTemplate.MatchOntology(REQUEST_EXHIBITION_ITEM_DETAILS_STRING)) {

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
    private void initiateAuction(ExhibitionItem item){

        AID[] profilers = lookForService(BUYING_ITEMS_REGTYPE);

        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setSender(getAID());


        int startingPrice = item.getPrice() * 10;

        String msgString = item.getName()+","+item.getGenre()+","+startingPrice;
        msg.setContent(msgString);

        System.out.println(profilers.length);
        for (AID profiler : profilers){
            msg.addReceiver(profiler);

        }
        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION);

        addBehaviour(new AchieveREInitiator(thisAgent,msg));
    }

    private ACLMessage getItemDescription(String type, ACLMessage message) {
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(message.getSender());
        msg.setSender(getAID());
        
        String messageContent = "";
        if (type.equals("short")){
            msg.setOntology(SHORT_ITEM_DESC_STRING);

            for (ExhibitionItem item : exhibition.getExhibitionItems()) {
                messageContent += item.getShortDescription() + "," +
                        getAID() + "\n";
            }
        }
        else if (type.equals("full")){
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




    // Ta bort..

    private ExhibitionItem createDummyItem(){
        ExhibitionItem item = new ExhibitionItem("Female","Art",17);
        item.setName("Mona Lisa");
        item.setPrice(100);
        return item;
    }

}
