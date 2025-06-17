package src;

/**
 * The possible states for a nonogram.
 * Using an enum as it defines all the possible states a cell can have.
 * For a black and white nonogram, black is COLOUR_1.
 */
public enum CellState {
    UNKNOWN, //default
    EMPTY, //white
    COLOUR_1,
    COLOUR_2,
    COLOUR_3,
    COLOUR_4;
}