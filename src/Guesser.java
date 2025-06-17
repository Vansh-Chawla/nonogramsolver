package src;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Guesser {
    private Nonogram puzzle;
    private int maxGuesses;
    private int guessCount;
    
    /**
     * Constructor for the Guesser class.
     * 
     * @param puzzle The Nonogram puzzle to be solved.
     */
    public Guesser(Nonogram puzzle) {
        this.puzzle = puzzle;
        this.maxGuesses = 1000; // Prevent infinite guessing in for loop later on
        this.guessCount = 0; // number of guesses 
    }
    
    /**
     * @return boolean value determining if puzzle has been solved by solver
     * 
     * First try deductive solving before guessing to ensure gusser is required
     * If deductive solving fails guessing method is implemented
    */ 
    public boolean solve() {
        Solver solver = new Solver(puzzle);
        if (solver.solve(false)) {
            return true;
        }
        return guessAndCheck();
    }
    
    /**
     * 
     * @return boolean if puzzle is solvable
     * 
     * Main method in the class that attempts to solve the a puzzle by guessing possible solutions
     */
    private boolean guessAndCheck() {
        if (guessCount++ > maxGuesses) {
            return false;
        }
        
        // Find the line with the fewest possible solutions
        int lineIndex = findMostConstrainedLine();
        boolean isRow = true; // default to row
        
        // Generate all possible fills for this line
        /**
         * Determine whether we're working with a row or column based on isRow flag
         * and fetch the corresponding constraints and current state of the line:
         * Get the constraints for the selected line:
         * - If isRow=true: Get row constraints at lineIndex
         * - If isRow=false: Get column constraints at lineIndex
         * Get the current state of the line (what's already filled in):
         * - Uses getRow() for rows or getColumn() for columns
         * - Returns an array of CellStates representing current cell values basically if it is (FILLED/EMPTY/UNKNOWN)
         * Generate all valid possible ways to fill this line that:
         * 1. Match the block constraints (lengths and colors)
         * 2. Are compatible with currently filled cells
         * Returns a list of possible configurations, where each configuration is represented as an array of CellStates
         */

        BlockConstraint[] constraints = isRow ? puzzle.getRowConstraints()[lineIndex] : puzzle.getColumnConstraints()[lineIndex];
        CellState[] currentLine = isRow ? getRow(lineIndex) : getColumn(lineIndex);
            
        List<CellState[]> possibleFills = generateLineFills(constraints, currentLine);
        
        if (possibleFills.isEmpty()) {
            return false; // No valid fills, puzzle is unsolvable
        }
        
        // Sort possible fills by length (shortest first)
        possibleFills.sort(Comparator.comparingInt(fill -> {
            int count = 0;
            for (CellState state : fill) {
                if (state != CellState.UNKNOWN) count++; // Count how many cells in this fill are already determined i.e. not UNKNOWN
            }
            return count; // Use this count as the sorting count 
        }));
        
        // Iterate through all possible line configurations we generated,
        // trying each one as a potential solution path
        for (CellState[] fill : possibleFills) {
            
            // 1. PRESERVE CURRENT STATE 
            // Create a copy of the entire puzzle's current state before making changes
            // which should let us to backtrack if this path doesn't lead to a solution
            CellState[][] savedState = puzzle.getGridCopy();
            
            // 2. APPLY THE CURRENT GUESS
            // Update either a row or column with our potential fill configuration
            if (isRow) {
                setRow(lineIndex, fill);  // Apply to row if working with rows
            } else {
                setColumn(lineIndex, fill);  // Apply to column if working with columns
            }
            
            // 3. TEST THE GUESS
            // Try to solve the puzzle with this configuration in place:
            // a) First try pure logical deduction (no guessing)
            Solver solver = new Solver(puzzle);
            boolean solvedByDeduction = solver.solve(false);  // noob
            
            // b) If deduction fails, recursively try more guessing (depth-first search)
            boolean solvedByGuessing = guessAndCheck(); 
            
            // If either approach solved it, success message
            if (solvedByDeduction || solvedByGuessing) {
                return true;
            }
            
            // 4. BACKTRACK IF UNSUCCESSFUL
            // Restore the puzzle state to before we tried this fill configuration:
            // a) First reset to initial empty state
            puzzle.resetGrid();
            puzzle.saveState();
            
            // b) Then copy back our saved state cell-by-cell
            // https://www.tutorialspoint.com/java/lang/system_arraycopy.htm
            for (int i = 0; i < savedState.length; i++) {
                System.arraycopy(
                    savedState[i],          // Source array (saved row)
                    0,                     // Source starting position
                    puzzle.getGrid()[i],    // Destination array (current grid row)
                    0,                      // Destination starting position
                    savedState[i].length    // Number of cells to copy
                );
            }
        }
        
        return false;
    }

    /**
     * @return int representing the index of the line with the fewest possibilities
     * 
     * This method finds the line (row or column) with the fewest possible configurations
     * based on the current state of the puzzle and its constraints.
     * It returns the index of that line.
     */
    private int findMostConstrainedLine() {
        int minPossibilities = Integer.MAX_VALUE; // acts as an infinity placeholder for initial comparison, so we can find the minimum
        // We will return the index of the line and whether it is a row or column
        int constrainedLine = 0;
        
        // Check rows
        for (int i = 0; i < puzzle.getRowConstraints().length; i++) {
            CellState[] line = getRow(i);
            if (hasUnknowns(line)) { //check for UNKNOWNS
                List<CellState[]> fills = generateLineFills(puzzle.getRowConstraints()[i], line);
                if (fills.size() < minPossibilities) { // this will never fail because we are checking for unknowns against basically infinty
                    minPossibilities = fills.size();
                    constrainedLine = i;
                }
            }
        }
        
        // Check columns
        for (int j = 0; j < puzzle.getColumnConstraints().length; j++) {
            CellState[] line = getColumn(j);
            if (hasUnknowns(line)) {
                List<CellState[]> fills = generateLineFills(puzzle.getColumnConstraints()[j], line);
                if (fills.size() < minPossibilities) { // again this will never fail because we are checking for unknowns against basically infinty
                    minPossibilities = fills.size();
                    constrainedLine = j;
                }
            }
        }
        
        return constrainedLine;
    }
    
    private boolean hasUnknowns(CellState[] line) {
        for (CellState state : line) {
            if (state == CellState.UNKNOWN) return true;
        }
        return false;
    }
    
    /**
     * @return boolean value determining if puzzle has been solved by solver
     * 
     * First try deductive solving before guessing to ensure gusser is required
     * If deductive solving fails guessing method is implemented
    */
    // Helper methods to get and set rows and columns taken from the Solver class
    private CellState[] getRow(int rowIndex) {
        int cols = puzzle.getColumnConstraints().length;
        CellState[] row = new CellState[cols];
        for (int j = 0; j < cols; j++) {
            row[j] = puzzle.getCellState(rowIndex, j);
        }
        return row;
    }

    /**
     * @param rowIndex Index of the row to set
     * @param newRow New row values to set
     * 
     * This method sets the values of a specific row in the puzzle grid.
     * It iterates through each cell in the specified row and updates its state.
     */
    
    private void setRow(int rowIndex, CellState[] newRow) {
        for (int j = 0; j < newRow.length; j++) {
            puzzle.setCellState(rowIndex, j, newRow[j]);
        }
    }
    
    /**
     * @param colIndex Index of the column to set
     * @param newCol New column values to set
     * 
     * This method sets the values of a specific column in the puzzle grid.
     * It iterates through each cell in the specified column and updates its state.
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
     * @param colIndex Index of the column to set
     * @param newCol New column values to set
     * 
     * This method sets the values of a specific column in the puzzle grid.
     * It iterates through each cell in the specified column and updates its state.
     */
    private void setColumn(int colIndex, CellState[] newCol) {
        for (int i = 0; i < newCol.length; i++) {
            puzzle.setCellState(i, colIndex, newCol[i]);
        }
    }
    /**
     * @return List of possible fills for a line based on constraints and current state
     * 
     * This method generates all possible fills for a given line based on the provided constraints and the current state of the line.
     * It uses backtracking to explore all combinations of filling the line while respecting the constraints.
     */
    private List<CellState[]> generateLineFills(BlockConstraint[] constraints, CellState[] currentLine) {
        List<CellState[]> result = new ArrayList<>();
        if (constraints == null || constraints.length == 0) {
            CellState[] fill = new CellState[currentLine.length];
            Arrays.fill(fill, CellState.EMPTY);
            result.add(fill);
            return result;
        }
        backtrackFill(result, new ArrayList<>(), constraints, 0, 0, currentLine);
        return result;
    }

    /**
     * @param result List to store valid fills
     * @param soFar Current fill being constructed
     * @param constraints Block constraints for the line
     * @param index Current index in the line
     * @param constraintIndex Current index in the constraints
     * @param currentLine Current state of the line
     * 
     * This method uses backtracking to fill the line with valid configurations based on the constraints.
     * It recursively explores all possible placements of blocks and separators, ensuring that the current fill is valid.
     * It checks for conflicts with existing cells and ensures that the blocks are placed according to the constraints.
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
            if (!canPlace) continue;

            // Determine if we need to add an EMPTY cell after this block
            boolean needsSeparator = false;
            if (constraintIndex < constraints.length - 1) {
                BlockConstraint nextBlock = constraints[constraintIndex + 1];
                // Need separator if next block is same color or if current position is at line end
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
     * @return int representing the remaining length of the line after the current index
     * 
     * This method calculates the total length of the remaining blocks and separators
     * in the constraints starting from a given index.
     * It sums up the lengths of the blocks and adds 1 for each separator between blocks
     * that are of the same color.
     */

    private int remainingLength(BlockConstraint[] constraints, int startIdx) {
        int total = 0;
        for (int i = startIdx; i < constraints.length; i++) {
            total += constraints[i].getLength();
            // Add 1 for separator if needed
            if (i > startIdx && constraints[i].getState() == constraints[i-1].getState()) {
                total += 1;
            }
        }
        return total;
    }

}