package src;

import java.util.*;

/**
 * The Solver class is responsible for solving the nonogram puzzle.
 * It processes the rows and columns, making deductions based on the
 * constraints,
 * and updates the grid until the puzzle is solved.
 */
public class Solver {
    private Nonogram puzzle;

    /**
     * Constructor for the Solver class, which initializes the puzzle instance.
     * 
     * @param puzzle The nonogram puzzle to solve.
     */
    public Solver(Nonogram puzzle) {
        this.puzzle = puzzle;
    }

    /**
     * Attempts to solve the nonogram puzzle by applying deductive reasoning.
     * It processes both rows and columns iteratively, updating the grid based on
     * possible valid fills for each line.
     * 
     * @return true if the puzzle is solved, false otherwise.
     */
    public boolean solve() {
        return solve(false); // Default to not using guesser
    }

    public boolean solve(boolean allowGuessing) {
        boolean changed;
        int iterations = 0;
        final int MAX_ITERATIONS = 100;

        do {
            changed = false;
            iterations++;

            // Process rows
            for (int i = 0; i < puzzle.getRowConstraints().length; i++) {
                if (processRows(i)) {
                    changed = true;
                }
            }

            // Process columns
            for (int j = 0; j < puzzle.getColumnConstraints().length; j++) {
                if (processColumns(j)) {
                    changed = true;
                }
            }

            // Early exit if solved
            if (puzzle.isSolved()) {
                return true;
            }

        } while (changed && iterations < MAX_ITERATIONS);

        // If deductive solving fails and guessing is allowed, return false
        // so Guesser can take over
        return !allowGuessing && puzzle.isSolved();
    }

    private boolean processColumns(int index) {
        CellState[] line = getColumn(index);
        BlockConstraint[] constraints = puzzle.getColumnConstraints()[index];

        // Skip if already solved
        if (puzzle.isLineSolved(line, constraints)) {
            int colCounter = 0;
            for (CellState item : line) {
                if (item.equals(CellState.UNKNOWN)) {
                    puzzle.setCellState(colCounter, index, CellState.EMPTY);
                }
                colCounter++;
            }
            return false;
        }

        // Generate all possible fills for this line
        List<CellState[]> fills = generateLineFills(constraints, line);

        // If no valid fills, puzzle is unsolvable
        if (fills.isEmpty()) {
            return false;
        }

        // Find cells that are consistent across all possible fills
        CellState[] merged = mergeLineFills(fills);

        // Update the line if we found new information
        if (!Arrays.equals(line, merged)) {

            setColumn(index, merged);

            return true;
        }

        return false;
    }

    private boolean processRows(int index) {
        CellState[] line = getRow(index);
        BlockConstraint[] constraints = puzzle.getRowConstraints()[index];

        // Skip if already solved
        if (puzzle.isLineSolved(line, constraints)) {
            int rowCounter = 0;
            for (CellState item : line) {
                if (item.equals(CellState.UNKNOWN)) {
                    puzzle.setCellState(index, rowCounter, CellState.EMPTY);
                }
                rowCounter++;
            }
            return false;
        }

        // Generate all possible fills for this line
        List<CellState[]> fills = generateLineFills(constraints, line);

        // If no valid fills, puzzle is unsolvable
        if (fills.isEmpty()) {
            return false;
        }

        // Find cells that are consistent across all possible fills
        CellState[] merged = mergeLineFills(fills);

        // Update the line if we found new information
        if (!Arrays.equals(line, merged)) {

            setRow(index, merged);

            return true;
        }

        return false;
    }

    private void TEH6_DEBUG_LINE(CellState[] line, boolean isCol) {
        System.out.print('[');
        for (CellState cell : line) {
            switch (cell) {
                case UNKNOWN:
                    System.out.print("?");
                    break;
                case EMPTY:
                    System.out.print(".");
                    break;
                case COLOUR_1:
                    System.out.print("1");
                    break;
                case COLOUR_2:
                    System.out.print("2");
                    break;
                case COLOUR_3:
                    System.out.print("3");
                    break;
                case COLOUR_4:
                    System.out.print("4");
                    break;
            }
            if (isCol)
                System.out.println(); // Newline for columns
        }
        System.out.println(']');
    }

    private void TEH6_DEBUG_PRINT(CellState[][] grid) {
        System.out.println("----- PUZZLE -----");
        for (CellState[] row : grid) {
            System.out.print('[');
            for (CellState cell : row) {
                switch (cell) {
                    case UNKNOWN:
                        System.out.print('?');
                        break;
                    case EMPTY:
                        System.out.print('.');
                        break;
                    case COLOUR_1:
                        System.out.print('1');
                        break;
                    case COLOUR_2:
                        System.out.print('2');
                        break;
                    case COLOUR_3:
                        System.out.print('3');
                        break;
                    case COLOUR_4:
                        System.out.print('4');
                        break;
                }
            }
            System.out.println(']');
        }
        System.out.println("----- /PUZZLE -----");
    }

    /**
     * Debug method to display the current state of the puzzle.
     */
    private void printPuzzle() {
        CellState[][] grid = puzzle.getGrid();

        for (CellState[] row : grid) {
            System.out.println(Arrays.toString(row));
        }
    }

    /**
     * Gets the current state of the specified row.
     * 
     * @param rowIndex The index of the row to retrieve.
     * @return An array representing the state of the row.
     */
    private CellState[] getRow(int rowIndex) {
        int cols = puzzle.getColumnConstraints().length;
        CellState[] row = new CellState[cols];
        for (int j = 0; j < cols; j++) {
            row[j] = puzzle.getCellState(rowIndex, j);
        }
        return row;
    }

    /**
     * Sets the state of the specified row in the puzzle.
     * 
     * @param rowIndex The index of the row to update.
     * @param newRow   The new state of the row.
     */
    private void setRow(int rowIndex, CellState[] newRow) {
        for (int j = 0; j < newRow.length; j++) {
            puzzle.setCellState(rowIndex, j, newRow[j]);
        }
    }

    /**
     * Gets the current state of the specified column.
     * 
     * @param colIndex The index of the column to retrieve.
     * @return An array representing the state of the column.
     */
    private CellState[] getColumn(int colIndex) {
        int rows = puzzle.getRowConstraints().length;
        CellState[] col = new CellState[rows];
        for (int i = 0; i < rows; i++) {
            col[i] = puzzle.getCellState(i, colIndex);
        }
        return col;
    }

    /**
     * Sets the state of the specified column in the puzzle.
     * 
     * @param colIndex The index of the column to update.
     * @param newCol   The new state of the column.
     */
    private void setColumn(int colIndex, CellState[] newCol) {
        for (int i = 0; i < newCol.length; i++) {
            puzzle.setCellState(i, colIndex, newCol[i]);
        }
    }

    /**
     * Merges a list of possible line fills by taking the common values at each
     * index.
     * If there are any discrepancies, the cell at that index will be set to
     * UNKNOWN.
     * 
     * @param fills A list of possible valid fills for a line.
     * @return A merged line with the most deducible values.
     */
    private CellState[] mergeLineFills(List<CellState[]> fills) {
        if (fills.isEmpty()) {
            return new CellState[0];
        }

        int length = fills.get(0).length;
        CellState[] result = new CellState[length];
        Arrays.fill(result, CellState.UNKNOWN);

        for (int i = 0; i < length; i++) {
            // Check if all fills agree on this cell
            CellState first = fills.get(0)[i];
            boolean allSame = true;

            for (CellState[] fill : fills) {
                if (fill[i] != first) {
                    allSame = false;
                    break;
                }
            }

            if (allSame) {
                result[i] = first;
            } else {
                // For colored puzzles, we need more sophisticated analysis
                Set<CellState> possibleStates = new HashSet<>();
                for (CellState[] fill : fills) {
                    possibleStates.add(fill[i]);
                }

                // If only one non-UNKNOWN state appears in all fills, use that
                possibleStates.remove(CellState.UNKNOWN);
                if (possibleStates.size() == 1) {
                    result[i] = possibleStates.iterator().next();
                }
                // Otherwise leave as UNKNOWN
            }
        }
        return result;
    }

    /**
     * Generates all possible valid fills for a line (row or column) based on the
     * constraints.
     * It uses backtracking to explore all possibilities and checks against the
     * current line state.
     * 
     * @param constraints The constraints for the line (row or column).
     * @param currentLine The current state of the line.
     * @return A list of possible valid fills for the line.
     */
    private List<CellState[]> generateLineFills(BlockConstraint[] constraints, CellState[] currentLine) {
        List<CellState[]> result = new ArrayList<>();
        if (constraints == null || constraints.length == 0) {
            // Handle empty constraints case
            CellState[] fill = new CellState[currentLine.length];
            Arrays.fill(fill, CellState.EMPTY);
            result.add(fill);
            return result;
        }
        backtrackFill(result, new ArrayList<>(), constraints, 0, 0, currentLine);
        return result;
    }

    /**
     * A recursive method to generate all possible valid fills for a line using
     * backtracking.
     * 
     * @param result          The list to store the valid fills.
     * @param soFar           The current state of the line as we're filling it.
     * @param constraints     The constraints for the line (row or column).
     * @param index           The current position in the line we're trying to fill.
     * @param constraintIndex The index of the current constraint we're trying to
     *                        place.
     * @param currentLine     The current state of the line.
     */
    public void backtrackFill(List<CellState[]> result, List<CellState> soFar,
            BlockConstraint[] constraints, int index, int constraintIndex,
            CellState[] currentLine) {
        int length = currentLine.length;

        // Base case: placed all constraints
        if (constraintIndex == constraints.length) {
            // Fill remaining with EMPTY if possible
            for (int i = index; i < length; i++) {
                if (currentLine[i] != CellState.UNKNOWN && currentLine[i] != CellState.EMPTY) {
                    return; // Conflict with existing cells
                }
                soFar.add(CellState.EMPTY);
            }
            result.add(soFar.toArray(new CellState[length]));
            return;
        }

        BlockConstraint block = constraints[constraintIndex];
        int blockLength = block.getLength();
        CellState blockState = block.getState();
        int maxStart = length - remainingLength(constraints, constraintIndex);

        // Try placing the current block at every valid position
        for (int start = index; start <= maxStart; start++) {
            List<CellState> temp = new ArrayList<>(soFar);

            // Fill EMPTY before the block if needed
            for (int i = index; i < start; i++) {
                if (currentLine[i] != CellState.UNKNOWN && currentLine[i] != CellState.EMPTY) {
                    return; // Conflict
                }
                temp.add(CellState.EMPTY);
            }

            // Place the block - must match both color and state
            boolean canPlace = true;
            for (int i = 0; i < blockLength; i++) {
                int pos = start + i;
                if (pos >= length) {
                    canPlace = false;
                    break;
                }
                // Must match both the color and that it's not EMPTY
                if (currentLine[pos] != CellState.UNKNOWN && currentLine[pos] != blockState) {
                    canPlace = false;
                    break;
                }
                temp.add(blockState);
            }
            if (!canPlace)
                continue;

            // Determine if we need to add an EMPTY cell after this block
            boolean needsSeparator = false;
            if (constraintIndex < constraints.length - 1) {
                BlockConstraint nextBlock = constraints[constraintIndex + 1];
                // Need separator if next block is same color or if current position is at line
                // end
                needsSeparator = (nextBlock.getState() == blockState) ||
                        (start + blockLength >= length);
            }

            if (needsSeparator && start + blockLength < length) {
                int afterPos = start + blockLength;
                if (currentLine[afterPos] != CellState.UNKNOWN &&
                        currentLine[afterPos] != CellState.EMPTY) {
                    continue; // Can't place separator here
                }
                temp.add(CellState.EMPTY);
            }

            // Recurse to place next block
            int nextPosition = start + blockLength;
            if (needsSeparator && nextPosition < length) {
                nextPosition += 1; // Skip the separator
            }
            backtrackFill(result, temp, constraints, nextPosition, constraintIndex + 1, currentLine);
        }
    }

    /**
     * Calculates the total remaining length for the constraints from a given start
     * index.
     * This includes the lengths of the blocks and the required spaces between them.
     * 
     * @param constraints The list of constraints (block sizes and states).
     * @param startIdx    The starting index for calculating the remaining length.
     * @return The total remaining length.
     */
    private int remainingLength(BlockConstraint[] constraints, int startIdx) {
        int total = 0;
        for (int i = startIdx; i < constraints.length; i++) {
            total += constraints[i].getLength();
            // Add 1 for separator if needed
            if (i > startIdx && constraints[i].getState() == constraints[i - 1].getState()) {
                total += 1;
            }
        }
        return total;
    }
}
