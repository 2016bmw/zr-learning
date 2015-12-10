package fsa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class FSA {
    
    private final Set<FSANode> nodes;
    private FSANode start;
    private final Set<FSANode> finalNodes;
    private int nodeCount;
    private boolean mergedFinalStates;
    private Set<FSANode> curMergeLevel;

    public FSA() {
        start = new FSANode(0);
        nodeCount = 1;
        nodes = new HashSet<>();
        nodes.add(start);
        finalNodes = new HashSet<>();
        mergedFinalStates = false;
        curMergeLevel = new HashSet<>();
    }
    
    private void removeNode(FSANode node) {
        nodes.remove(node);
        if (finalNodes.contains(node)) {
            finalNodes.remove(node);
        }
    }
        
    public void ingestSentence(String example) {
        String[] words = example.split(" ");
        FSANode curNode = start;
        FSANode nextNode;
        for (String word: words) {
            nextNode = curNode.nextNode(word);
            if (nextNode == null) {
                nextNode = new FSANode(nodeCount++);
                nodes.add(nextNode);
                FSAEdge edge = new FSAEdge(curNode, nextNode, word);
                curNode.addEdge(edge);
                nextNode.addEdge(edge);
            }
            curNode = nextNode;
        }
        finalNodes.add(curNode);
        curNode.setAccepting(true);
    }
    
    public void merge() {
        if (!mergedFinalStates) {
            mergeFinalStates();
            mergedFinalStates = true;
        } else {
            mergeIntermediateStates();
        }
    }
    
    private void updateBasedOnMerges(Set<FSANode> nodes, Map<FSANode, FSANode> formerNodeToMergedNode) {
        Iterator<FSANode> it = nodes.iterator();
        Set<FSANode> nodesToRemove = new HashSet<>();
        Set<FSANode> nodesToAdd = new HashSet<>();
        while (it.hasNext()) {
            FSANode n = it.next();
            while (formerNodeToMergedNode.containsKey(n)) {
                nodesToRemove.add(n);
                nodesToAdd.remove(n);
                n = formerNodeToMergedNode.get(n);
                nodesToAdd.add(n);
            }
        }
        nodes.addAll(nodesToAdd);
        nodes.removeAll(nodesToRemove);
    }
    
    private void mergeIntermediateStates() {
        Map<Adjacency, Set<FSANode>> adjToNodes = new HashMap<>();
        Set<FSANode> newCurMergeLevel = new HashSet<>();
        Map<FSANode, FSANode> formerNodeToMergedNode = new HashMap<>();
        for (FSANode node : curMergeLevel) {
            boolean needToBreak = false;
            for (Set<FSANode> targets : node.getLabelToTargets().values()) {
                if (targets.size() > 1) {
                    updateBasedOnMerges(targets, formerNodeToMergedNode);
                    FSANode newNode = mergeNodes(targets);
                    targets.stream().forEach(n -> formerNodeToMergedNode.put(n, newNode));                    
                    if (newNode.needsToMergeOutgoingEdges()) {
                        newCurMergeLevel.add(newNode);
                    }
                    needToBreak = true;
                }
            }
            
            if (needToBreak) {
                break;
            }
            
            for (FSAEdge edge : node.getOutgoingEdges()) {
                Adjacency adj = new Adjacency(edge);
                Set<FSANode> nodesWithSameLabelToSameTarget = adjToNodes.get(adj);
                if (nodesWithSameLabelToSameTarget == null) {
                    nodesWithSameLabelToSameTarget = new HashSet<>();
                    adjToNodes.put(adj, nodesWithSameLabelToSameTarget);
                }
                nodesWithSameLabelToSameTarget.add(node);
            }
        }
        
        for (FSANode node : curMergeLevel) {
            newCurMergeLevel.addAll(node.getIncomingEdges().stream()
                    .map(e -> e.getSource()).collect(Collectors.toSet()));
        }
        
       for (Set<FSANode> nodes : adjToNodes.values()) {
           updateBasedOnMerges(nodes, formerNodeToMergedNode);
            if (nodes.size() > 1) {
                FSANode newNode = mergeNodes(nodes);
                if (!Collections.disjoint(newCurMergeLevel, nodes)) {
                    newCurMergeLevel.removeAll(nodes);
                    newCurMergeLevel.add(newNode);
                }
                for (FSANode node : nodes) {
                    formerNodeToMergedNode.put(node, newNode);
                }
            }
        }
        // remove any nodes that are now contained by other nodes
        curMergeLevel = newCurMergeLevel.stream()
                .filter(n -> this.nodes.contains(n))
                .collect(Collectors.toSet());
    }
    
    private FSANode mergeNodes(Set<FSANode> nodes) {
        List<FSAEdge> incomingEdgesToAdjust = new ArrayList<>();
        List<FSAEdge> outgoingEdgesToAdjust = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        boolean replacedStart = false;
        boolean replacedFinal = false;
        boolean accepting = false;
        for (FSANode node: nodes) {
            incomingEdgesToAdjust.addAll(node.getIncomingEdges());
            outgoingEdgesToAdjust.addAll(node.getOutgoingEdges());
            ids.add(node.getID());
            if (node == start) {
                replacedStart = true;
                start = null;
            }
            if (finalNodes.contains(node)) {
                replacedFinal = true;
            }
            if (node.isAccepting()) {
                accepting = true;
            }
            removeNode(node);
        }
        FSANode newNode = new FSANode(ids);
        newNode.setAccepting(accepting);
        Set<String> selfLoopLabels = new HashSet<>();
        for (FSAEdge edge: outgoingEdgesToAdjust) {
            FSANode formerSource = edge.getSource();
            edge.setSource(newNode);
            if (nodes.contains(edge.getTarget())) {
                // self loop
                if (!selfLoopLabels.contains(edge.getLabel())) {
                    edge.setTarget(newNode);
                    newNode.addEdge(edge);
                    selfLoopLabels.add(edge.getLabel());
                } else {
                    edge.getTarget().removeEdge(edge);
                }
                continue;
            }
            if (!newNode.getOutgoingEdges().contains(edge)) {
                newNode.addEdge(edge);
                edge.getTarget().removeEdges(edge.getTarget().edgesFrom(formerSource));
                if (!edge.getTarget().edgesFrom(newNode).contains(edge)) {
                    edge.getTarget().addEdge(edge);
                }
                List<FSAEdge> commonEdges = edge.getTarget().edgesFrom(newNode);
                commonEdges.retainAll(newNode.edgesTo(edge.getTarget()));
                assert commonEdges.size() > 0;
                assert !edge.getTarget().hasEdgeFrom(formerSource);
            } else {
                FSANode target = edge.getTarget();
                if (target.edgesFrom(newNode).stream().filter(e -> e.getLabel().equals(edge.getLabel()))
                        .count() > 1) {
                    target.removeEdge(edge);
                }
            }
        }        
        for (FSAEdge edge: incomingEdgesToAdjust) {
            FSANode formerTarget = edge.getTarget();
            edge.setTarget(newNode);
            if (edge.getSource().equals(newNode) || nodes.contains(edge.getSource())) {
                // self loop, handled as outgoing above
                assert selfLoopLabels.contains(edge.getLabel());
                continue;
            }

            if (!newNode.getIncomingEdges().contains(edge)) {
                newNode.addEdge(edge);
                edge.getSource().removeEdges(edge.getSource().edgesTo(formerTarget));
                if (!edge.getSource().edgesTo(newNode).contains(edge)) {
                    edge.getSource().addEdge(edge);
                }
                List<FSAEdge> commonEdges = edge.getSource().edgesTo(newNode);
                commonEdges.retainAll(newNode.edgesFrom(edge.getSource()));
                assert commonEdges.size() > 0;
                assert !edge.getSource().hasEdgeTo(formerTarget);
            } else {
                FSANode source = edge.getSource();
                source.removeEdge(edge);
            }
        }
        assert !this.nodes.contains(newNode);
        this.nodes.add(newNode);
        if (replacedStart) {
            start = newNode;
        }
        if (replacedFinal) {
            finalNodes.add(newNode);
        }
        return newNode;
    }
    
    private void mergeFinalStates() {
        Set<FSANode> formerFinals = new HashSet<>(finalNodes);
        FSANode newNode = mergeNodes(formerFinals);
        finalNodes.add(newNode);
        newNode.getIncomingEdges().stream().forEach(e -> curMergeLevel.add(e.getSource()));
    }
    
    public DirectedSparseMultigraph<FSANode, FSAEdge> getVisualGraph() {
        DirectedSparseMultigraph<FSANode, FSAEdge> g = new DirectedSparseMultigraph<FSANode, FSAEdge>();
        Queue<FSANode> q = new LinkedList<>();
        q.add(start);
        g.addVertex(start);
        FSANode curNode;
        FSANode nextNode;
        Set<FSANode> seenNodes = new HashSet<>();
        seenNodes.add(start);
        while (!q.isEmpty()) {
            curNode = q.remove();
            for (FSAEdge edge : curNode.getOutgoingEdges()) {
                nextNode = edge.getTarget();
                if (!seenNodes.contains(nextNode) && !nextNode.equals(curNode)) {
                    g.addVertex(nextNode);
                    q.add(nextNode);
                    seenNodes.add(nextNode);
                }
                
                FSAEdge existingEdge = g.findEdge(curNode, nextNode);
                if (g.findEdge(curNode, nextNode) != null) {
                    g.removeEdge(existingEdge);
                    edge = edge.mergeLabel(existingEdge);
                }
                
                g.addEdge(edge, curNode, nextNode);
            }
        }
        return g;
    }
}
