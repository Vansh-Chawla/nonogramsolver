```java
package src;

import java.util.*;
import java.util.stream.Stream;

public class PuzzleSolverAttempt {
    //TODO:
    // Make puzzlesolver constructor taking puzzle and isInitialised
    // Start stack logic

    public 
    public static void main(String[] args) {
        BlockConstraint[] constraints = {new BlockConstraint(2, CellState.COLOUR_1),
            new BlockConstraint(3, CellState.COLOUR_1),new BlockConstraint(2, CellState.COLOUR_1)};
        int size = 10;

        puzzle = 

        findPossibilities(constraints, size);
    }

    public static void findPossibilities(BlockConstraint[] constraints, int size) {
        int numberOfSets = constraints.length;
        int boxes = Stream.of(constraints).mapToInt(x -> x.getLength()).sum();
        // numberOfSets - 1 = number of guaranteed spaces required between the sets
        int spaces = size - boxes - (numberOfSets - 1);
        //int remainingSpaces = size - boxes - spaces;

        List<CellState[]> possibilities = new ArrayList<>();

        // row, current block in row, extra spaces left to add, current permutation, current number of bits to shift
        findPossibility(possibilities, 0, spaces, numberOfSets);

        for (CellState n : possibilities.get(0)) {
            System.out.println(n.toString() + "");
        }
    }

    public static void findPossibility(List<CellState[]> possibilities, int currentBlock, int spaces, int numberOfSets) {
        Stack<CellState> currentPoss = new Stack<>();

        if (currentPoss.size() == this.puzzle.rows....)

        // Loop through sets of coloured cells
        for (int i = 0; i < numberOfSets; i++) {
            // Colour the selected cells
            for (int j = 0; j < constraints[i].getLength(); j++) {
                line.add(CellState.COLOUR_1);
            }

            numberOfSets -= 1;
            size -= (constraints[i].getLength() + 1);

            findPossibility(possibilities, constraints, size, numberOfSets);
        }
    }
}
```