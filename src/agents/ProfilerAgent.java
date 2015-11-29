package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;

import java.util.ArrayList;

import data.ExhibitionItem;

/**
 * ProfilerAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 2.0
 */
public class ProfilerAgent extends Agent {

    /*
     * The profile contains basic user information (age, occupation, gender,
     * interest, etc) and visited items (in our case museum artifacts).
     */
    private int age;
    private String occupation;
    //private enum gender {Male,Female};
    private String gender;
    private ArrayList<String> interest = new ArrayList<String>();
    private int maxprice;
    private String biddingStrategy;

    private ArrayList<ExhibitionItem> exhibitionItems = new ArrayList<>();;
    private ArrayList<AID> guideAgents = new ArrayList<>();

    private Agent thisAgent = this;

    private final String REQUEST_VIRTUAL_TOUR_STRING = "request-virtual-tour";
    private final String REQUEST_EXHIBITION_ITEM_DETAILS_STRING = "request-exhibition-item-details";
    private final String SHORT_ITEM_DESC_STRING = "short-item-description";
    private final String FULL_ITEM_DESC_STRING = "full-item-description";
    
    private final String BUYING_ITEMS_REGTYPE = "item-buying";
    private final String BUYING_ITEMS_REGNAME = "exhibition-item-buying";
    private final String ITEM_SENDING_REGTYPE = "item-sending";
    
    public ProfilerAgent() {}

    @Override
    protected void setup() {
    	
    	handleArguments(getArguments());
    	
        // Printout a welcome message
        System.out.println("Profiler-agent " + getAID().getName() + " is ready.");

        registerService(BUYING_ITEMS_REGTYPE, BUYING_ITEMS_REGNAME);
        //registerService(AUCTION_REGTYPE, AUCTION_REGNAME);

        // BEHAVIOUR
        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                AID[] newGuideAgents = lookForServiceByType("tour-guiding");
                bcastGuideAgents(newGuideAgents);

                //getVirtualTour.. interest = 1; age = 2; fkjskf = 3;
            }
        });

        // BEHAVIOUR
        addBehaviour(new AchieveREResponder(thisAgent, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_DUTCH_AUCTION)){
            @Override
            protected ACLMessage handleRequest(ACLMessage request) throws NotUnderstoodException, RefuseException {
                if (biddingStrategy.equalsIgnoreCase("shade"))
                    return performShadeBidStrategy(request);
                else if(biddingStrategy.equalsIgnoreCase("normal"))
                    return performNormalBidStrategy(request);
                else return performShadeBidStrategy(request);
            }
        });

        // BEHAVIOUR
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchSender(getDefaultDF()));
                if (msg != null){
                    //System.out.println("Subscription");
                }
            }
        });
        
        // BEHAVIOUR
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Wait for message
                ACLMessage msg = receive(MessageTemplate.MatchOntology(SHORT_ITEM_DESC_STRING));
                
                if (msg != null) {
                    String[] lines = msg.getContent().split("\n");

                    for (String line : lines) {
                    	String[] content = line.split(",");
                    //System.out.println("Profiler: received items (short description).");
                        try {
                            String genre = content[1];
                            int age = Integer.parseInt(content[2]);
                            String gender = content[3];
                            String curator = content[4];
                            ExhibitionItem item = new ExhibitionItem(gender, genre, age);
                            item.setCuratorName(curator);

                            item.setID(Integer.parseInt(content[0]));
                            exhibitionItems.add(item);

                            requestFullDescription(curator, item.getID());
                        } catch (Exception e) {}

                        //System.out.print("Profiler: "+ getName()+" Short-desc -> ");
                        for (String str : content) {
                            System.out.print(str + " ");
                        }
                        System.out.println();
                    }
                } else { block(); }
            }
        });

        // BEHAVIOUR
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchOntology(FULL_ITEM_DESC_STRING));

                if (msg != null) {
                    String[] content = msg.getContent().split(",");
                    ExhibitionItem tempItem = null;
                    try {
                        int id = Integer.parseInt(content[0]);
                        String name = content[1];
                        String creator = content[2];
                        String creationPlace = content[3];
                        for (ExhibitionItem item : exhibitionItems) {
                            if (item.getID() == id && item.getName() == null) {
                                item.setName(name);
                                item.setCreator(creator);
                                item.setCreationPlace(creationPlace);
                                tempItem = item;
                                System.out.println("Profiler: "+getName()+ " FullDesc - > " 
                                			+ tempItem.getFullItemDescription());
                            }
                        }
                    } catch (NumberFormatException e) {}
                }
            }
        });
    }
    
    @Override
    protected void takeDown() {
        try { DFService.deregister(this); }
        catch (FIPAException fe) {
            System.err.println(fe);
        }
        
        // Printout a dismissal message
        System.out.println("Profiler-agent " + getAID().getName() + 
                " terminating.");
    }
    
    private void handleArguments(Object[] args){

        try {
            String[] arguments = ((String) args[0]).split(" ");

            age = Integer.parseInt( arguments[0]);
            gender =  arguments[1];
            occupation =  arguments[2];
            maxprice = Integer.parseInt( arguments[3]);
            biddingStrategy = arguments[4];
            
            if (!biddingStrategy.equalsIgnoreCase("shade") || 
            		!biddingStrategy.equalsIgnoreCase("normal") ) {
                throw new Exception("Invalid bidding strategy");
            }
            
            for (int i = 5; i<arguments.length;i++) {
                interest.add( arguments[i]);
            }
        } catch (Exception e) {
            age = 17;
            occupation = "Student";
            //private enum gender {Male,Female};
            gender = "Female";
            biddingStrategy = "normal";
            interest.add("art");
            interest.add("music");
            maxprice = 250;
        }
    }
    
    private void bcastGuideAgents(AID[] agents) {
        for (int i = 0;i<agents.length;i++) {
            if (!guideAgents.contains(agents[i])) {
                final AID agent = agents[i];
                guideAgents.add(agents[i]);

                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(agent);
                msg.setSender(getAID());
                msg.setOntology(REQUEST_VIRTUAL_TOUR_STRING);
                
                String tempInterest = "";
                for (String str : interest) {
                    tempInterest += "," + str;
                }
                
                String contentString = age + "," + 
                        occupation + "," + gender + tempInterest;
                msg.setContent(contentString);
                
                //System.out.println("Profiler: requesting virtual tour with parameters: " 
                //		+ msg.getContent());
                send(msg);
            }
        }
    }
    
    private void registerService(String serviceType, String serviceName) {
        DFAgentDescription dfdesc = new DFAgentDescription();
        dfdesc.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        dfdesc.addServices(sd);
        
        try {
            DFService.register(this, dfdesc);
            DFService.createSubscriptionMessage(this, getDefaultDF(), 
            		dfdesc, new SearchConstraints());
        } catch (FIPAException fe) {
            System.err.println(fe);
        }
    }

    private AID[] lookForServiceByType(String serviceType) {
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
        } catch (FIPAException fe) { System.err.println(fe); }
        
        return agents;
    }
    
    private AID[] lookForServiceByName(String serviceType, String serviceName) {
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        //sd.setName(serviceName);
        template.addServices(sd);
        AID[] agents = new AID[]{};

        try {
            DFAgentDescription[] result = DFService.search(this, template);
            agents = new AID[result.length];

            for (int i = 0; i < result.length; ++i) {
                agents[i] = result[i].getName();
            }
        } catch (FIPAException fe) { System.err.println(fe); }

        return agents;
    }

    private ACLMessage performShadeBidStrategy(ACLMessage msg) {

        String[] content = msg.getContent().split(",");
        ACLMessage returnMsg = msg.createReply();
        try {
            String itemName = content[0];
            String itemGenre = content[1];
            int itemPrice = Integer.parseInt(content[2]);

            //System.out.println( returnMsg.toString() );
            if(checkInterest(itemGenre)) {
                if(itemPrice < maxprice) {
                    returnMsg.setPerformative(ACLMessage.AGREE);
                    returnMsg.setContent("accept_bid,"+msg.getContent());
                } else {
                    returnMsg.setPerformative(ACLMessage.INFORM);
                    returnMsg.setContent("reject_bid,"+msg.getContent());
                }
            } else {
                returnMsg.setPerformative(ACLMessage.REFUSE);
                returnMsg.setContent("reject_auction,"+msg.getContent());
            }

        } catch (Exception e) {
            returnMsg.setContent("reject_bid,"+msg.getContent());
        }
        return returnMsg;
    }
    
    private ACLMessage performNormalBidStrategy(ACLMessage msg) {

        String[] content = msg.getContent().split(",");
        ACLMessage returnMsg = msg.createReply();
        try {
            String itemName = content[0];
            String itemGenre = content[1];
            int itemPrice = Integer.parseInt(content[2]);

            //
            //System.out.println( returnMsg.toString() );
            if(checkInterest(itemGenre)) {
                if(itemPrice <= maxprice) {
                    returnMsg.setPerformative(ACLMessage.AGREE);
                    returnMsg.setContent("accept_bid,"+msg.getContent());
                } else {
                    returnMsg.setPerformative(ACLMessage.INFORM);
                    returnMsg.setContent("reject_bid,"+msg.getContent());
                }
            } else {
                returnMsg.setPerformative(ACLMessage.REFUSE);
                returnMsg.setContent("reject_auction,"+msg.getContent());
            }

        } catch (Exception e) {
            returnMsg.setContent("reject_bid,"+msg.getContent());
        }
        return returnMsg;
    }
    
    private boolean checkInterest(String genre) {
        for (String i : interest){
            if(i.equalsIgnoreCase(genre)) {
                return true;
            }
        }
        return false;
    }
    
    private void requestFullDescription(String sender, int id){
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);

        msg.setOntology(REQUEST_EXHIBITION_ITEM_DETAILS_STRING);
        msg.setContent(Integer.toString(id));
        AID[] curators = lookForServiceByName(ITEM_SENDING_REGTYPE,sender);
        for (AID curator : curators){
            msg.addReceiver(curator);
        }
        send(msg);

    }
    
    public String toString() {
    	return "Age: "+age + " gender: "+gender + " occupation: " + occupation + " maxprice: " 
    				+ maxprice+ " interest: " + interest;
    }
}
