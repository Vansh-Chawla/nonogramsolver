package src;
import java.util.*;
import java.awt.Color;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Nonogram {
    private String name;
    private BlockConstraint[][] rowConstraints;
    private BlockConstraint[][] columnConstraints;
    private CellState[][] grid;
    private Stack<CellState[][]> moveHistory; // For undo functionality
    private Map<CellState, Color> colourMap; // For storing the colours in the puzzle

    public Nonogram(String name, Map<CellState, Color> colourMap, BlockConstraint[][] rowConstraints, BlockConstraint[][] columnConstraints, int rows, int columns) {
        this.name = name;
        this.colourMap = colourMap;
        this.rowConstraints = rowConstraints;
        this.columnConstraints = columnConstraints;
        this.grid = new CellState[rows][columns];
        this.moveHistory = new Stack<>();

        initialiseGrid();

        // Input Validation
        if (rowConstraints == null || columnConstraints == null) {
            throw new IllegalArgumentException("Constraints cannot be null");
        }
    }

    /**
     * Create linked constructor to set default colours for the black and white nonograms.
     * @param name name of the puzzle
     * @param rowConstraints row constraints of the puzzle
     * @param columnConstraints column constraints of the puzzle
     * @param rows number of rows in the puzzle
     * @param columns number of columns in the puzzle
     */
    public Nonogram(String name, BlockConstraint[][] rowConstraints, BlockConstraint[][] columnConstraints, int rows, int columns) {
        this(name, new TreeMap<CellState, Color>(), rowConstraints, columnConstraints, rows, columns);

        this.colourMap.put(CellState.UNKNOWN, Color.decode("#ECECEC"));
        this.colourMap.put(CellState.COLOUR_1, Color.decode("#000000"));
        this.colourMap.put(CellState.EMPTY, Color.decode("#ffffff"));
    }

    /**
     * Resets the puzzle grid to the loaded state.
     */
    public void resetGrid(){
        initialiseGrid();
        moveHistory.clear(); // Clear undo history
    }

    /**
     * Gets the current grid. Only needed for debugging, in order to print the current puzzle.
     * @return The current state of the grid
     */
    public CellState[][] getGrid() {
        return this.grid;
    }

    /**
     * Gets a copy of the current grid in order to add to the stack of moves.
     * @return a copy of the current grid
     */
    public CellState[][] getGridCopy() {
        CellState[][] copy = new CellState[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, grid[i].length);
        }
        return copy;
    }

    /**
     * Saves the current state of the grid to the stack of moves. For undo functionality.
     */
    public void saveState() {
        moveHistory.push(getGridCopy());
    }

    /**
     * Undo the last move made. The move must have been made after loading the puzzle for it to be on the stack.
     * @return true if the last move has been successfully undone, false if there are no moves left to undo
     */
    public boolean undo() {
        if (!moveHistory.isEmpty()) {
            grid = moveHistory.pop();
            return true;
        }
        return false; // No moves left to undo
    }

    /**
     * Reset the grid to its state when the puzzle or moves were loaded.
     * @return True if the moves were successfully reset, false if there were no moves to reset
     */
    public boolean resetMoves() {
        if (moveHistory.isEmpty()) {
            return false;
        }
        while (!moveHistory.isEmpty()) {
            grid = moveHistory.pop();
        }
        return true;
    }

    /**
     * Creates a grid of the puzzle size, filled with UNKNOWN cell state.
     */
    public void initialiseGrid() {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                grid[i][j] = CellState.UNKNOWN;
            }
        }
    }

    /**
     * Gets the cell state of the cell in a given row and column.
     * @param row number of rows in the puzzle
     * @param column number of columns in the puzzle
     * @return the cell state of the cell in the given row or column
     */
    public CellState getCellState(int row, int column) {
        validateCoordinates(row, column);
        return grid[row][column];
    }

    /**
     * Set the cell state for a specific cell.
     * @param row number of rows in puzzle
     * @param column number of columns in the puzzle
     * @param cellState the state to be set
     */
    public void setCellState(int row, int column, CellState cellState) {
        validateCoordinates(row, column);
        if (grid[row][column] != cellState) { // Only save if state changes
            saveState(); // Save current state before changing
            grid[row][column] = cellState;
        }
    }

    /**
     * Get the map of custom states for the current puzzle.
     * @return the map of custom states
     */
    public Map<CellState, Color> getColourMap() {
        return this.colourMap;
    }

    /**
     * Get the colour corresponding to a specific state in the puzzle.
     * @param state the state of the current cell
     * @return the Java Color corresponding to the given state
     */
    public Color getStateColour(CellState state) {
        return this.colourMap.get(state);
    }

    /**
     * Checks that a given row and column is within the dimensions for a given puzzle.
     * @param row number of rows in the puzzle
     * @param column number of columns in the puzzle
     */
    public boolean validateCoordinates(int row, int column) {
        if (row < 0 || row >= grid.length || column < 0 || column >= grid[0].length) {
            throw new IllegalArgumentException("Invalid cell coordinates: (" + row + ", " + column + ")");
        }
        return true;
    }

    /**
     * Gets the row constraints for the current puzzle.
     * @return a 2D array of BlockConstraint objects containing the row constraints
     */
    public BlockConstraint[][] getRowConstraints() {
        return rowConstraints;
    }

    /**
     * Gets the column constraints for the current puzzle.
     * @return a 2D array of BlockConstraint objects containing the column constraints
     */
    public BlockConstraint[][] getColumnConstraints() {
        return columnConstraints;
    }

    /**
     * Gets the puzzle name.
     * @return The puzzle name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks whether the puzzle is solved.
     * @return true if the puzzle is solved, false if not
     */
    public boolean isSolved() {
        // Check all rows
        for (int i = 0; i < grid.length; i++) {
            if (!isLineSolved(grid[i], rowConstraints[i])) {
                return false;
            }
        }

        // Check all columns
        for (int j = 0; j < grid[0].length; j++) {
            CellState[] column = new CellState[grid.length];
            for (int i = 0; i < grid.length; i++) {
                column[i] = grid[i][j];
            }
            if (!isLineSolved(column, columnConstraints[j])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if an individual line is solved.
     * @param line holds a list of lines in the puzzle to be iterated through
     * @param constraints contains the contraints of the puzzle
     * @return true if the line is solved, false if not
     */
    public boolean isLineSolved(CellState[] line, BlockConstraint[] constraints) {
        List<BlockConstraint> actualBlocks = new ArrayList<>();
        int currentBlockLength = 0;
        CellState currentColor = null;
        
        for (CellState cell : line) {
            if (cell != CellState.EMPTY && cell != CellState.UNKNOWN) {
                if (currentColor == null) {
                    currentColor = cell;
                    currentBlockLength = 1;
                } else if (cell == currentColor) {
                    currentBlockLength++;
                } else {
                    actualBlocks.add(new BlockConstraint(currentBlockLength, currentColor));
                    currentColor = cell;
                    currentBlockLength = 1;
                }
            } else if (currentColor != null) {
                // End of a colored block
                actualBlocks.add(new BlockConstraint(currentBlockLength, currentColor));
                currentColor = null;
                currentBlockLength = 0;
            }
        }
        
        // Add the last block if it exists
        if (currentColor != null) {
            actualBlocks.add(new BlockConstraint(currentBlockLength, currentColor));
        }
        
        // Compare with constraints
        if (actualBlocks.size() != constraints.length) {
            return false;
        }
        
        for (int i = 0; i < constraints.length; i++) {
            if (actualBlocks.get(i).getLength() != constraints[i].getLength() || 
                actualBlocks.get(i).getState() != constraints[i].getState()) {
                return false;
            }
        }
        
        return true;
    }

    /**
     * Saves the moves made so far to a JSON file.
     * @param filePath json file name to save moves to
     * @throws IOException if the named file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened for any other reason
     */
    public void saveMoves(String filePath) throws IOException {
        try (FileWriter file = new FileWriter(filePath)) {
            JSONObject json = new JSONObject();
            json.put("name", this.name);
            
            // Save the grid state
            JSONArray gridArray = new JSONArray();
            for (CellState[] row : grid) {
                JSONArray rowArray = new JSONArray();
                for (CellState cell : row) {
                    JSONObject cellObj = new JSONObject();
                    cellObj.put("state", cell.name());
                    rowArray.put(cellObj);
                }
                gridArray.put(rowArray);
            }
            json.put("grid", gridArray);
            
            // Save color map if it's a color puzzle
            if (!colourMap.isEmpty()) {
                JSONObject statesObj = new JSONObject();
                for (Map.Entry<CellState, Color> entry : colourMap.entrySet()) {
                    statesObj.put(entry.getKey().name(), 
                        String.format("#%06X", 0xFFFFFF & entry.getValue().getRGB()));
                }
                json.put("states", statesObj);
            }
            file.write(json.toString(2)); // Pretty print with 2-space indent
        }
    }

    /**
     * Read a puzzle from a JSON file.
     * @param filePath json file name to read moves from
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public void loadMoves(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject json = new JSONObject(content);
    
        if (!json.getString("name").equals(this.name)) {
            throw new IllegalArgumentException("Saved moves don't match current puzzle");
        }
    
        // Clear current grid and history
        resetGrid();
        moveHistory.clear();
    
        // Load color map if present
        if (json.has("states")) {
            JSONObject statesJson = json.getJSONObject("states");
            for (String key : statesJson.keySet()) {
                try {
                    CellState state = CellState.valueOf(key);
                    Color color = Color.decode(statesJson.getString(key));
                    colourMap.put(state, color);
                } catch (IllegalArgumentException e) {
                    System.err.println("Warning: Unknown state " + key + " in saved file");
                }
            }
        }
    
        // Load grid
        if (json.has("grid")) {
            JSONArray gridArray = json.getJSONArray("grid");
            for (int i = 0; i < gridArray.length(); i++) {
                JSONArray rowArray = gridArray.getJSONArray(i);
                for (int j = 0; j < rowArray.length(); j++) {
                    JSONObject cell = rowArray.getJSONObject(j);
                    grid[i][j] = CellState.valueOf(cell.getString("state"));
                }
            }
        }
    }
}