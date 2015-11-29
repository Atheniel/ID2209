package agents;

import data.ExhibitionItem;
import data.ProfilerData;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import java.util.ArrayList;

/**
 * GuideAgent.java
 * 
 * @author Peter Ledberg
 * @author David Kufa
 * @version 2.0
 */
public class GuideAgent extends Agent{

    private final String REQUEST_EXHIBITION_ITEMS_STRING = "request-exhibition-items";
    private final String REQUEST_VIRTUAL_TOUR_STRING = "request-virtual-tour";
    private final String SHORT_ITEM_DESC_STRING = "short-item-description";

    private final String ITEM_SENDING_LOOKUPSTRING = "item-sending";
    private final String TOUR_GUIDING_REGTYPE = "tour-guiding";
    private final String TOUR_GUIDING_REGNAME = "exhibition-tour-guiding";

    private final String ITEM_DESC_KEY = "item-desc-key";
    private final String CURATOR_REQUEST_KEY = "curator-request-key";
    private final String VIRTUAL_TOUR_REQUEST_KEY = "virtual-tour-request-key";

    private ArrayList<ProfilerData> requestedTours = new ArrayList<>();
    private ArrayList<ExhibitionItem> exhibitionItems = new ArrayList<>();

    private final Agent thisAgent = this;
    private DataStore dataStore = new DataStore();

    public GuideAgent() {}
    
    @Override
    protected void setup() {
        System.out.println("Guide-agent: " + getAID().getName() + " is ready.");
        registerService(TOUR_GUIDING_REGTYPE, TOUR_GUIDING_REGNAME);
        
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                // Wait for message
                ACLMessage msg = receive();
                
                if (msg != null) {
                    final String[] content = msg.getContent().split(",");
                    final ACLMessage message = msg;
                        
                    // Parse message depending on ontology
                    switch (msg.getOntology()) {
                        case REQUEST_VIRTUAL_TOUR_STRING:
                        	//System.out.println("Guide: received virtual tour request.");
                            addBehaviour(new OneShotBehaviour(thisAgent) {
                                @Override
                                public void action() {
                                    addGuideRequest(content, message.getSender());
                                }
                            });

                            AID[] curatorAgents = lookForService(ITEM_SENDING_LOOKUPSTRING);
                            ACLMessage curatorMsg = new ACLMessage(ACLMessage.REQUEST);
                            curatorMsg.setSender(getAID());
                            curatorMsg.setOntology(REQUEST_EXHIBITION_ITEMS_STRING);

                            for (AID agent : curatorAgents) {
                                curatorMsg.addReceiver(agent);
                            }
                            
                            //System.out.println("Guide: requesting museum items.");
                            send(curatorMsg);

                            break;
                        case SHORT_ITEM_DESC_STRING: 
                        	//System.out.println("Guide: received museum items.");
                            final String[] lines = msg.getContent().split("\n");
                            SequentialBehaviour sequentialBehaviour = new SequentialBehaviour(thisAgent);
                            sequentialBehaviour.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    addExhibitionItem(lines);
                                }
                            });

                            ParallelBehaviour parallelBehaviour = new ParallelBehaviour(thisAgent,requestedTours.size());
                            for (ProfilerData profilerData : requestedTours){
                                final ProfilerData data = profilerData;
                                parallelBehaviour.addSubBehaviour(new OneShotBehaviour() {
                                    @Override
                                    public void action() {
                                        createVirtualTours(data, exhibitionItems);
                                    }
                                });
                            }
                            sequentialBehaviour.addSubBehaviour(parallelBehaviour);
                            addBehaviour(sequentialBehaviour);

                            break;
                    }
                }
                
                else{ block(); }
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
        System.out.println("Guide-agent " + getAID().getName() + 
                " terminating.");
    }
    
    private void addGuideRequest(String[] content, AID sender) {            
        try {
            int age = Integer.parseInt(content[0]);
            String occupation = content[1];
            String gender = content[2];
            String[] interest = new String[content.length - 3];
            for (int j = 3; j < content.length-1; j++) {
                interest[j - 3] = content[j];
            }
            requestedTours.add(new ProfilerData(sender, age, occupation, gender, interest));
        } catch (NumberFormatException e) { System.err.print(e); }
    }
   
    private void addExhibitionItem(String[] content) {
        for (String line : content) {
            try {
                String[] lineContent = line.split(",");
                int id = Integer.parseInt(lineContent[0]);
                String genre = lineContent[1];
                int recommendedAge = Integer.parseInt(lineContent[2]);
                String recommendedGender = lineContent[3];
                String curator = lineContent[4];
                ExhibitionItem newItem = new ExhibitionItem(recommendedGender,genre,recommendedAge);
                newItem.setCuratorName(curator);
                newItem.setID(id);
                exhibitionItems.add(newItem);
            } catch (Exception e) { System.err.println(e); }
        }
    }
   
    private void createVirtualTours(ProfilerData agent, ArrayList<ExhibitionItem> items) {
        ProfilerData tempAgent = null;
        String messageString = "";
        for (ExhibitionItem item : items) {
            if ((agent.getAge() >= (item.getRecommendedAge() - 5) &&
                  agent.getAge() <= (item.getRecommendedAge() + 5)) &&
                  agent.getGender().equalsIgnoreCase(item.getRecommendedGender()) &&
                  checkInterest(agent.getInterest(),item.getGenre())) {

                messageString += item.getShortItemDescription() + "," + item.getCuratorName() +"\n";
            }
        }
        
        ArrayList<ProfilerData> tempArr = new ArrayList<>();
        if(!messageString.equals("")) {
            sendTours(agent.getAid(), messageString);
            for (ProfilerData data : requestedTours) {
                if (!data.getAid().equals(agent.getAid())) {
                    tempArr.add(data);
                }
            }
            requestedTours = tempArr;
        }

        /*
        if(tempAgent != null) {
            requestedTours.remove(tempAgent);
        }*/
    }

    private boolean checkInterest(String[] agentInterest, String itemInterest) {
        for (String interest : agentInterest) {
            if(interest != null && interest.equalsIgnoreCase(itemInterest)) {
                return true;
            }
        }
        
        return false;
    }

    private void sendTours(AID agentID, String message) {
    	//System.out.println("Guide: sending virtual tour guide.");
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(agentID);
        msg.setSender(getAID());
        msg.setContent(message);
        msg.setOntology(SHORT_ITEM_DESC_STRING);
        send(msg);
    }
    
    private void registerService(String serviceType, String serviceName) {
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

