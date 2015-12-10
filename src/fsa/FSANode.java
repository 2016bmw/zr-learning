package fsa;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FSANode {
    private final List<FSAEdge> incidentEdges = new ArrayList<>();
    private final List<FSAEdge> outgoingEdges = new ArrayList<>();
    private final List<FSAEdge> incomingEdges = new ArrayList<>();
    private final String id;
    private boolean accepting = false;
    
    public FSANode(int id) {
        this.id = Integer.toString(id);
    }
    
    public FSANode(String id) {
        this.id = id;
    }
    
    public FSANode(Collection<String> mergingIds) {
        this.id = mergingIds.stream().reduce((i1, i2) -> i1+","+i2).get();
    }
    
    public String getID() {
        return id;
    }
    
    public boolean isAccepting() {
        return accepting;
    }
    
    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
    }
    
    public List<FSAEdge> getEdges() {
        return Collections.unmodifiableList(incidentEdges);
    }
    
    public List<FSAEdge> getOutgoingEdges() {
        return Collections.unmodifiableList(outgoingEdges);
    }    
    
    public List<FSAEdge> getIncomingEdges() {
        return Collections.unmodifiableList(incomingEdges);
    }
    
    public void removeEdge(FSAEdge e) {
        incidentEdges.remove(e);
        outgoingEdges.remove(e);
        incomingEdges.remove(e);
    }
    
    public void removeEdges(List<FSAEdge> edges) {
        edges.stream().forEach(e -> removeEdge(e));
    }
    
    public List<FSAEdge> edgesFrom(FSANode node) {
        return incomingEdges.stream().filter(e -> e.getSource().equals(node))
                .collect(Collectors.toList());
    }
    
    public boolean hasEdgeFrom(FSANode node) {
        return incomingEdges.stream().anyMatch(e -> e.getSource().equals(node));
    }
    
    public List<FSAEdge> edgesTo(FSANode node) {
        return outgoingEdges.stream().filter(e -> e.getTarget().equals(node))
                .collect(Collectors.toList());
    }
    
    public boolean hasEdgeTo(FSANode node) {
        return outgoingEdges.stream().anyMatch(e -> e.getTarget().equals(node));
    }
    
    public Map<String, Set<FSANode>> getLabelToTargets() {
        Map<String, Set<FSANode>> labelToTargets = new HashMap<>();
        for (FSAEdge e : outgoingEdges) {
            String label = e.toString();
            Set<FSANode> targets = labelToTargets.get(label);
            if (targets == null) {
                targets = new HashSet<>();
                labelToTargets.put(label, targets);
            }
            targets.add(e.getTarget());
        }
        return labelToTargets;
    }
    
    public boolean needsToMergeOutgoingEdges() {
        Set<String> outgoingLabels = new HashSet<>();
        for (FSAEdge e : outgoingEdges) {
            String label = e.getLabel();
            if (outgoingLabels.contains(label)) {
                return true;
            }
            outgoingLabels.add(label);
        }
        return false;
    }
    
    public void addEdge(FSAEdge edge) {
        assert edge.incidentTo(this);
        if (edge.hasSource(this)) {
            outgoingEdges.add(edge);
        } else {
            incomingEdges.add(edge);
        }

        incidentEdges.add(edge);
    }
    
    public FSANode nextNode(String s) {
        Optional<FSAEdge> edge = outgoingEdges.stream().filter(e -> e.getLabel().equals(s))
                                             .findFirst();
        if (!edge.isPresent()) {
            return null;
        }
        return edge.get().getTarget();
    }
    
    public Set<FSANode> prevNodes(String s) {
        return incomingEdges.stream().filter(e -> e.getLabel().equals(s))
                                     .map(e -> e.getSource())
                                     .collect(Collectors.toSet());
    }
      
    @Override
    public String toString() {
        return id+"";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FSANode other = (FSANode) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
