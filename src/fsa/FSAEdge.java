package fsa;

public class FSAEdge {
    
    private FSANode target;
    private FSANode source;
    private final String label;

    public FSAEdge(FSANode source, FSANode target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }
    
    public FSANode getTarget() {
        return target;
    }
    
    public FSANode getSource() {
        return source;
    }
    
    public String getLabel() {
        return label;
    }
    
    public void setTarget(FSANode node) {
        this.target = node;
    }
    
    public void setSource(FSANode node) {
        this.source = node;
    }
    
    public boolean incidentTo(FSANode node) {
        return target.equals(node) || source.equals(node);
    }
    
    public boolean hasSource(FSANode node) {
        return source.equals(node);
    }
    
    public boolean hasTarget(FSANode node) {
        return target.equals(node);
    }
    
    public FSAEdge mergeLabel(FSAEdge other) {
        assert this.target.equals(other.target) && this.source.equals(other.source);
        return new FSAEdge(source, target, label+", "+other.label);
    }

    @Override
    public String toString() {
        return label;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((label == null) ? 0 : label.hashCode());
        result = prime * result + ((source == null) ? 0 : source.hashCode());
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
        FSAEdge other = (FSAEdge) obj;
        if (label == null) {
            if (other.label != null)
                return false;
        } else if (!label.equals(other.label))
            return false;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }
}
