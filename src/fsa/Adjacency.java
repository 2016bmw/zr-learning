package fsa;

public class Adjacency {

    private final String edgeLabel;
    private final FSANode target;
    
    public Adjacency(FSANode target, String edgeLabel) {
        this.target = target;
        this.edgeLabel = edgeLabel;
    }
    
    public Adjacency(FSAEdge e) {
        this.target = e.getTarget();
        this.edgeLabel = e.getLabel();
    }
    public String getEdgeLabel() {
        return edgeLabel;
    }

    public FSANode getTarget() {
        return target;
    }
    
    @Override
    public String toString() {
        return "Adjacency [edgeLabel=" + edgeLabel + ", target=" + target + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((edgeLabel == null) ? 0 : edgeLabel.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        Adjacency other = (Adjacency) obj;
        if (edgeLabel == null) {
            if (other.edgeLabel != null)
                return false;
        } else if (!edgeLabel.equals(other.edgeLabel))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
}
