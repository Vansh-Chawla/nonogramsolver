package src;

public class BlockConstraint {
    int length;
    CellState state;
    
    public BlockConstraint(int length, CellState state) {
        this.length = length;
        this.state = state;
    }

    public int getLength() {
        return length;
    }

    public CellState getState() {
        return state;
    }
}
