package src;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestSolverAndGuesser {
    private TestNonogram nonogramTester;

    private Solver testSolvableSolver;
    private Solver testUnsolvableSolver;

    private Guesser testGuesser;

    private Nonogram testSolvablePuzzle;
    private Nonogram testUnsolvablePuzzle;

    private CellState[][] emptyUnsolvableGrid;
    private CellState[][] solvedUnsolvableGrid;

    @Before
    public void setupTestCases() {
        this.nonogramTester = new TestNonogram();

        // Create a solvable test puzzle with constraints (same puzzle as when testing the nonogram) and a related solver
        BlockConstraint[][] solvableRowConstraints = new BlockConstraint[2][];
        BlockConstraint[][] solvableColumnConstraints = new BlockConstraint[2][];

        this.nonogramTester.setupTestGrids();
        this.nonogramTester.setupTestConstraints(solvableRowConstraints, solvableColumnConstraints);

        this.testSolvablePuzzle = new Nonogram("Test Puzzle", solvableRowConstraints, solvableColumnConstraints, 2, 2);
        this.testSolvableSolver = new Solver(testSolvablePuzzle);

        // Create an unsolvable (without guessing) test puzzle with constraints and a related solver
        BlockConstraint[][] unsolvableRowConstraints = new BlockConstraint[2][];
        BlockConstraint[][] unsolvableColumnConstraints = new BlockConstraint[2][];

        this.setupTestGrids();
        this.setupTestConstraints(unsolvableRowConstraints, unsolvableColumnConstraints);

        this.testUnsolvablePuzzle = new Nonogram("Test Puzzle", unsolvableRowConstraints, unsolvableColumnConstraints, 2, 2);
        this.testUnsolvableSolver = new Solver(testUnsolvablePuzzle);

        // Create a guesser for the unsolvable puzzle, which will solve it by guessing
        this.testGuesser = new Guesser(testUnsolvablePuzzle);
    }

    // Test backtrackFill(), the method to generate all possible valid fills for a line using backtracking.

    /**
     * Test case 1: Test constraints 2,3,2 with length 10, given an empty line
     * Should give 4 solutions.
     */
    @Test
    public void testBacktrackFillCase1() {
        // Define test constraints
        BlockConstraint[] testConstraints = {new BlockConstraint(2, CellState.COLOUR_1),
            new BlockConstraint(3, CellState.COLOUR_1),new BlockConstraint(2, CellState.COLOUR_1)};

        // Define state of the line before finding possibilities
        CellState[] currentLine = {CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN,
                                    CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN};

        // Run the backtrackFill() method 
        List<CellState[]> result = new ArrayList<>();
        testSolvableSolver.backtrackFill(result, new ArrayList<>(), testConstraints, 0, 0, currentLine);

        // Define expected output
        List<CellState[]> expectedOutput = Arrays.asList(
            new CellState[] {CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY},
            new CellState[] {CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1},
            new CellState[] {CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1},
            new CellState[] {CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1}
        );
        
        // Compare to expected output
        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), expectedOutput.get(i));
        }
    }

    /**
     * Test case 2: Test constraint 5 with length 9, given a partly filled line
     * Should give 3 solutions.
     */
    @Test
    public void testBacktrackFillCase2() {
        BlockConstraint[] testConstraints = {new BlockConstraint(5, CellState.COLOUR_1)};

        CellState[] currentLine = {CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.COLOUR_1, CellState.COLOUR_1,
                                                        CellState.COLOUR_1, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN};  

        List<CellState[]> result = new ArrayList<>();
        testSolvableSolver.backtrackFill(result, new ArrayList<>(), testConstraints, 0, 0, currentLine);

        List<CellState[]> expectedOutput = Arrays.asList(
            new CellState[] {CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.EMPTY, CellState.EMPTY},
            new CellState[] {CellState.EMPTY, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY, CellState.EMPTY},
            new CellState[] {CellState.EMPTY, CellState.EMPTY, CellState.EMPTY, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.EMPTY}
        );

        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), expectedOutput.get(i));
        }
    }

    /**
     * Test case 3: test constraint 5 with length 5, given an empty line
     * Should give 1 solution
     */
    @Test
    public void testBacktrackFillCase3() {
        BlockConstraint[] testConstraints = {new BlockConstraint(5, CellState.COLOUR_1)};

        CellState[] currentLine = {CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN, CellState.UNKNOWN};

        List<CellState[]> result = new ArrayList<>();
        testSolvableSolver.backtrackFill(result, new ArrayList<>(), testConstraints, 0, 0, currentLine);

        CellState[] expectedOutputArr = new CellState[] {CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1};
        List<CellState[]> expectedOutput = new ArrayList<>();
        expectedOutput.add(expectedOutputArr);

        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), expectedOutput.get(i));
        }
    }

    /**
     * Test case 4: test constraint 5 with length 5, given an already solved line
     * Should give 1 solution
     */
    @Test
    public void testBacktrackFillCase4() {
        BlockConstraint[] testConstraints = {new BlockConstraint(5, CellState.COLOUR_1)};

        CellState[] currentLine = {CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1};

        List<CellState[]> result = new ArrayList<>();
        testSolvableSolver.backtrackFill(result, new ArrayList<>(), testConstraints, 0, 0, currentLine);

        CellState[] expectedOutputArr = new CellState[] {CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1, CellState.COLOUR_1};
        List<CellState[]> expectedOutput = new ArrayList<>();
        expectedOutput.add(expectedOutputArr);

        for (int i = 0; i < result.size(); i++) {
            assertArrayEquals(result.get(i), expectedOutput.get(i));
        }
    }

    /**
     * Test case 5: test constraint 1 with length 5, given an incorrectly filled line of 2 colours
     * Should give 0 solutions (empty output)
     */
    @Test
    public void testBacktrackFillCase5() {
        BlockConstraint[] testConstraints = {new BlockConstraint(1, CellState.COLOUR_1)};

        CellState[] currentLine = {CellState.UNKNOWN, CellState.COLOUR_1, CellState.COLOUR_1, CellState.UNKNOWN, CellState.UNKNOWN};

        List<CellState[]> result = new ArrayList<>();
        testSolvableSolver.backtrackFill(result, new ArrayList<>(), testConstraints, 0, 0, currentLine);

        List<CellState[]> expectedOutput = new ArrayList<>();

        assertEquals(result, expectedOutput);
    }

    /**
     * Test that the solver accurately solves a puzzle which is solvable without guessing.
     */
    @Test
    public void testSolveSolvable() {
        testSolvablePuzzle.initialiseGrid();
        assertArrayEquals(testSolvablePuzzle.getGrid(), nonogramTester.emptyGrid);

        testSolvableSolver.solve();

        assertArrayEquals(testSolvablePuzzle.getGrid(), nonogramTester.solvedAndEmptyGrid);
    }

    /**
     * Ensure that the solver does not accurately solve a puzzle which is unsolvable without guessing.
     */
    @Test
    public void testSolveUnsolvable() {
        testUnsolvablePuzzle.initialiseGrid();
        assertArrayEquals(testUnsolvablePuzzle.getGrid(), this.emptyUnsolvableGrid);

        testUnsolvableSolver.solve();

        assertArrayEquals(testUnsolvablePuzzle.getGrid(), this.emptyUnsolvableGrid);
    }

    /**
     * Show that the guesser accurately solves a puzzle with multiple solutions that the solver could not.
     * Uses the same puzzle as testSolveUnsolvable().
     */
    @Test
    public void testGuessUnsolvable() {
        testUnsolvablePuzzle.initialiseGrid();
        assertArrayEquals(testUnsolvablePuzzle.getGrid(), this.emptyUnsolvableGrid);

        testGuesser.solve();

        assertArrayEquals(testUnsolvablePuzzle.getGrid(), this.solvedUnsolvableGrid);
    }

    /**
     * Setup some test grids to compare solver outputs with.
     * The puzzle these moves create is unsolvable without guessing, since it has multiple solutions.
     * One empty grid, and one solved grid.
     * The grids are assigned to instance variables, since they are used for comparison when solving the grid.
     * In a separate method to save space.
     */
    public void setupTestGrids() {
        // Setup 2 moves for the test grid, leading to a solved example puzzle
        this.emptyUnsolvableGrid = new CellState[][] {
            {CellState.UNKNOWN, CellState.UNKNOWN},
            {CellState.UNKNOWN, CellState.UNKNOWN}
        };
        
        this.solvedUnsolvableGrid = new CellState[][] {
            {CellState.COLOUR_1, CellState.EMPTY},
            {CellState.EMPTY, CellState.COLOUR_1},
        };
    }
    
    /**
     * Setup test constraints for the grids.
     */
    public void setupTestConstraints(BlockConstraint[][] rowConstraints, BlockConstraint[][] columnConstraints) {
        rowConstraints[0] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_1) };
        rowConstraints[1] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_1) };
        
        columnConstraints[0] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_1) };
        columnConstraints[1] = new BlockConstraint[] { new BlockConstraint(1, CellState.COLOUR_1) };
    }

    /**
     * Initialise the test puzzle grid, set it to a solved state and push the moves made to the move history stack.
     */
    public void initialiseAndSolveGrid() {
        testUnsolvablePuzzle.initialiseGrid();
        this.testUnsolvablePuzzle.saveState();

        // Set the cell states to solve the puzzle, and in doing so adds them to the stack
        // In the real puzzle, this would be done when pressing the grid buttons
        testUnsolvablePuzzle.setCellState(0, 0, CellState.COLOUR_1);
        testUnsolvablePuzzle.setCellState(1, 1, CellState.COLOUR_1);
    }
}
