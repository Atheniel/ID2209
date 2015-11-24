package agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;

import data.ExhibitionItem;

/**
 * ProfilerAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 1.0
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
    private String[] interest;

    private ArrayList<ExhibitionItem> exhibitionItems;
    private ArrayList<AID> guideAgents = new ArrayList<>();

    private Agent thisAgent = this;

    private final String REQUEST_VIRTUAL_TOUR_STRING = "request-virtual-tour";
    private final String REQUEST_EXHIBITION_ITEM_DETAILS_STRING = "request-exhibition-item-details";
    private final String SHORT_ITEM_DESC_STRING = "short-item-description";
    private final String FULL_ITEM_DESC_STRING = "full-item-description";
    
    private final String TOUR_GUIDING_REGTYPE = "tour-visiting";
    private final String TOUR_GUIDING_REGNAME = "interested-in-guided-tours";
    private final String AUCTION_REGTYPE = "auction";
    private final String AUCTION_REGNAME = "interested-in-canvas-auctions";

    public ProfilerAgent() {
        age = 17;
        occupation = "Student";
        //private enum gender {Male,Female};
        gender = "Female";
        interest = new String[]{"art","music"};
        exhibitionItems = new ArrayList<>();
    }

    @Override
    protected void setup() {
        // Printout a welcome message
        System.out.println("Profiler-agent " + getAID().getName() + " is ready.");

        registerService(TOUR_GUIDING_REGTYPE, TOUR_GUIDING_REGNAME);
        //registerService(AUCTION_REGTYPE, AUCTION_REGNAME);

        addBehaviour(new TickerBehaviour(this, 2000) {
            @Override
            protected void onTick() {
                AID[] newGuideAgents = lookForService("tour-guiding");
                bcastGuideAgents(newGuideAgents);

                //getVirtualTour.. interest = 1; age = 2; fkjskf = 3;
            }
        });
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Wait for message
                ACLMessage msg = receive();
                
                if (msg != null) {
                    // IF-satsen här nedan gör inget?
                    if (msg.getSender().equals(getDefaultDF())) {
                        System.out.println("Subscription");
                    } else {
                        String[] lines = msg.getContent().split("\n");
                        String[] content = lines[0].split(",");
                        
                        // Parse message depending on ontology
                        switch (msg.getOntology()) {
                            case SHORT_ITEM_DESC_STRING:
                            	System.out.println("Profiler: received items (short description).");
                                try {
                                    String genre = content[1];
                                    int age = Integer.parseInt(content[2]);
                                    String gender = content[3];
                                    ExhibitionItem item = new ExhibitionItem(
                                            gender, genre, age);
                                    
                                    item.setID(Integer.parseInt(content[0]));
                                    exhibitionItems.add(item);

                                    //addBehaviour(new SimpleAchieveREInitiator(
                                    //      thisAgent, ));
                                } catch (Exception e) {}

                                System.out.print("Profiler: ");
                                for (String str : content) {
                                    System.out.print(str + " ");
                                }

                                break;
                            case FULL_ITEM_DESC_STRING:
                            	System.out.println("Profiler: received items (detailed description).");
                            	
                                break;
                            case REQUEST_EXHIBITION_ITEM_DETAILS_STRING: 
                                System.out.print("full ");
                                for (String str : content) {
                                    System.out.print(str + " ");
                                }

                                break;
                            case "BEGINNING": 
                                System.out.println("BEGINNING " + msg.toString());

                                break;
                        }
                    }
                }
                
                else{ block(); }
            }
        });
        
        /*  PARALLELBEHAVIOUR - USEFUL CODE - DO NOT REMOVE
        ParallelBehaviour parallel = new ParallelBehaviour(thisAgent, 4);
        
        parallel.addSubBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive(MessageTemplate.MatchOntology(""));
                if (msg != null) {
                    
                }
            }
        });*/
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
                
                System.out.println("Profiler: requesting virtual tour with parameters: " 
                		+ msg.getContent());
                send(msg);
            }
        }
    }
    
    /*
    private void setGuideAgentReciever(AID guide) {
        //addBehaviour(new ReceiverBehaviour());
    }*/
    
    private void registerService(String serviceType, String serviceName) {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(serviceType);
        sd.setName(serviceName);
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            if(serviceType.equals(TOUR_GUIDING_REGTYPE)) { // Precaution
                DFService.createSubscriptionMessage(this, getDefaultDF(), dfd, 
                        new SearchConstraints());
            }
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
