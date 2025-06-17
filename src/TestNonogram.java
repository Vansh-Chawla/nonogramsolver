package src;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Before;
import org.junit.Test;

public class TestNonogram {
    private Nonogram testPuzzle;
    public CellState[][] emptyGrid;
    private CellState[][] gridWithMove1;
    private CellState[][] gridWithMove2;
    private CellState[][] solvedGrid;
    public CellState[][] solvedAndEmptyGrid;

    @Before
    public void setupTestPuzzle() {
        // Create a test puzzle with constraints
        BlockConstraint[][] rowConstraints = new BlockConstraint[2][];
        BlockConstraint[][] columnConstraints = new BlockConstraint[2][];

        setupTestGrids();
        setupTestConstraints(rowConstraints, columnConstraints);

        this.testPuzzle = new Nonogram("Test Puzzle", rowConstraints, columnConstraints, 2, 2);
    }

    // test initialise grid
    @Test
    public void testInitialiseGrid() {
        testPuzzle.initialiseGrid();

        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);
    }

    // test get cell state
    @Test
    public void testGetCellState() {
        testPuzzle.initialiseGrid();

        assertEquals(testPuzzle.getCellState(0, 0), CellState.UNKNOWN);
    }

    // test set cell state
    @Test
    public void testSetCellState() {
        testPuzzle.initialiseGrid();

        testPuzzle.setCellState(1, 1, CellState.COLOUR_1);

        assertArrayEquals(testPuzzle.getGrid(), this.gridWithMove1);
    }

    // test reset grid
    @Test
    public void testResetGrid() {
        initialiseAndSolveGrid();

        assertArrayEquals(testPuzzle.getGrid(), this.solvedGrid);
        testPuzzle.resetGrid();
        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);
    }
    
    // test getGridCopy, is copy same as grid
    @Test
    public void testGetGridCopy() {
        initialiseAndSolveGrid();

        assertArrayEquals(testPuzzle.getGrid(), this.solvedGrid);
        assertArrayEquals(testPuzzle.getGrid(), testPuzzle.getGridCopy());
    }
    
    // test undo
    @Test
    public void testUndo() {
        initialiseAndSolveGrid();

        assertArrayEquals(testPuzzle.getGrid(), this.solvedGrid);

        testPuzzle.undo();
        assertArrayEquals(testPuzzle.getGrid(), this.gridWithMove2);

        testPuzzle.undo();
        assertArrayEquals(testPuzzle.getGrid(), this.gridWithMove1);

        testPuzzle.undo();
        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);
    }

    // test that the puzzle does not change or throw an exception if undo is run on an empty puzzle
    @Test
    public void testEmptyUndo() {
        testPuzzle.initialiseGrid();

        testPuzzle.undo();
        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);
    }

    // test resetMoves (stack)
    @Test
    public void testResetMoves() {
        initialiseAndSolveGrid();

        testPuzzle.resetMoves();
        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);
    }

    // test validate coordinates with normal (valid) data
    @Test
    public void testValidateCoordinatesNormal() {
        assertTrue(testPuzzle.validateCoordinates(0, 1));
    }

    // test validate coordinates with exceptional (invalid) data
    @Test (expected = IllegalArgumentException.class)
    public void testValidateCoordinatesExceptional() {
        testPuzzle.validateCoordinates(1, 2);
    }

    // test isLineSolved with a normal (valid) line
    @Test
    public void testIsLineSolvedNormal() {
        CellState[] line = {CellState.COLOUR_2, CellState.COLOUR_1};
        BlockConstraint[] lineConstraint = { new BlockConstraint(1, CellState.COLOUR_2), new BlockConstraint(1, CellState.COLOUR_1) };

        assertTrue(testPuzzle.isLineSolved(line, lineConstraint));
    }

    // test another line in the puzzle
    @Test
    public void testIsLineSolvedNormal2() {
        CellState[] line = {CellState.UNKNOWN, CellState.COLOUR_1};
        BlockConstraint[] lineConstraint = { new BlockConstraint(1, CellState.COLOUR_1) };
        
        assertTrue(testPuzzle.isLineSolved(line, lineConstraint));
    }

    // test isLineSolved with a normal (valid) column
    @Test
    public void testIsColumnSolvedNormal() {
        CellState[] column = {CellState.COLOUR_2, CellState.UNKNOWN};
        BlockConstraint[] columnConstraint = { new BlockConstraint(1, CellState.COLOUR_2) };

        assertTrue(testPuzzle.isLineSolved(column, columnConstraint));
    }

    // test another column in the puzzle
    @Test
    public void testIsColumnSolvedNormal2() {
        CellState[] column = {CellState.COLOUR_1, CellState.COLOUR_1};
        BlockConstraint[] columnConstraint = { new BlockConstraint(2, CellState.COLOUR_1) };

        assertTrue(testPuzzle.isLineSolved(column, columnConstraint));
    }

    // test isLineSolved with an exceptional (invalid) line
    @Test
    public void testIsLineSolvedExceptional() {
        CellState[] line = {CellState.UNKNOWN, CellState.COLOUR_1};
        BlockConstraint[] lineConstraint = { new BlockConstraint(1, CellState.COLOUR_2), new BlockConstraint(1, CellState.COLOUR_1) };

        assertFalse(testPuzzle.isLineSolved(line, lineConstraint));
    }

    // test whether isLineSolved ignores unknown cells, ie does not require unfilled cells to be marked empty
    @Test
    public void testIsLineSolvedUnknown() {
        CellState[] line = {CellState.UNKNOWN, CellState.COLOUR_1};
        BlockConstraint[] lineConstraint = { new BlockConstraint(1, CellState.COLOUR_1) };

        assertTrue(testPuzzle.isLineSolved(line, lineConstraint));
    }

    // test isSolved with a normal (solved) puzzle
    @Test
    public void testIsSolvedNormal() {
        initialiseAndSolveGrid();

        assertArrayEquals(testPuzzle.getGrid(), this.solvedGrid);

        assertTrue(testPuzzle.isSolved());
    }

    // test isSolved with an exceptional (unsolved) puzzle
    @Test
    public void testIsSolvedExceptional() {
        testPuzzle.initialiseGrid();

        assertFalse(testPuzzle.isSolved());
    }

    // test saveMoves (compare file with expected)
    @Test
    public void testSaveMoves() throws IOException {
        initialiseAndSolveGrid();

        testPuzzle.saveMoves("testpuzzles/savedtestpuzzlemoves.json");

        // Check whether the files are identical
        long mismatch = Files.mismatch(Paths.get("testpuzzles/testpuzzlemoves.json"), Paths.get("testpuzzles/savedtestpuzzlemoves.json"));

        assertEquals(-1, mismatch);
    }

    // test loadMoves (compare grid with expected)
    @Test
    public void testLoadMoves() throws IOException {
        testPuzzle.initialiseGrid();
        assertArrayEquals(testPuzzle.getGrid(), this.emptyGrid);

        testPuzzle.loadMoves("testpuzzles/savedtestpuzzlemoves.json");
        assertArrayEquals(testPuzzle.getGrid(), this.solvedGrid);
    }
    
    /**
     * Setup some test grids to compare Nonogram method outputs with.
     * The puzzle these moves create is solvable without guessing.
     * One empty grid, then three with one move added each.
     * The grids are also assigned to instance variables, since they are used for comparison when resetting the grid or undoing moves.
     * In a separate method to save space.
     */
    public void setupTestGrids() {
        // Setup 3 moves for the test grid, leading to a solved example puzzle
        this.emptyGrid = new CellState[][] {
            {CellState.UNKNOWN, CellState.UNKNOWN},
            {CellState.UNKNOWN, CellState.UNKNOWN}
        };
        
        this.gridWithMove1 = new CellState[][] {
            {CellState.UNKNOWN, CellState.UNKNOWN},
            {CellState.UNKNOWN, CellState.COLOUR_1},
        };
        
        this.gridWithMove2 = new CellState[][] {
            {CellState.COLOUR_2, CellState.UNKNOWN},
            {CellState.UNKNOWN, CellState.COLOUR_1},
        };
        
        this.solvedGrid = new CellState[][] {
            {CellState.COLOUR_2, CellState.COLOUR_1},
            {CellState.UNKNOWN, CellState.COLOUR_1},
        };

        this.solvedAndEmptyGrid = new CellState[][] {
            {CellState.COLOUR_2, CellState.COLOUR_1},
            {CellState.EMPTY, CellState.COLOUR_1},
        };
    }
    
    /**
     * Setup test constraints for the grids.
     */
    public void setupTestConstraints(BlockConstraint[][] rowConstraints, BlockConstraint[][] columnConstraints) {
        rowConstraints[0] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_2), new BlockConstraint(1, CellState.COLOUR_1) };
        rowConstraints[1] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_1) };
        
        columnConstraints[0] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_2) };
        columnConstraints[1] = new BlockConstraint[] { new BlockConstraint(2, CellState.COLOUR_1) };
    }

    /**
     * Initialise the test puzzle grid, set it to a solved state and push the moves made to the move history stack.
     */
    public void initialiseAndSolveGrid() {
        testPuzzle.initialiseGrid();
        this.testPuzzle.saveState();

        // Set the cell states to solve the puzzle, and in doing so adds them to the stack
        // In the real puzzle, this would be done when pressing the grid buttons
        testPuzzle.setCellState(1, 1, CellState.COLOUR_1);
        testPuzzle.setCellState(0, 0, CellState.COLOUR_2);
        testPuzzle.setCellState(0, 1, CellState.COLOUR_1);
    }
}
