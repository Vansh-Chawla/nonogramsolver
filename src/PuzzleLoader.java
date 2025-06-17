package src;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.awt.Color;
import java.util.*;

public class PuzzleLoader {
    /**
     * Gets the row and column constraints for a nonogram from JSON input.
     * 
     * @param filePath The path to the JSON file
     * @return A nonogram object based on the given file
     * @throws Exception
     */
    public Nonogram loadPuzzle(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONObject json = new JSONObject(content);

        String name = json.getString("name");
        JSONArray rowsJSON = json.getJSONArray("rows");
        JSONArray colJSON = json.getJSONArray("columns");

        BlockConstraint[][] rowContraints = parseConstraints(rowsJSON);
        BlockConstraint[][] colConstraints = parseConstraints(colJSON);

        int rows = rowContraints.length;
        int cols = colConstraints.length;

        // Check if the JSON has defined states
        // If so, parse the colours and return a nonogram with these colours stored,
        // otherwise return a default nonogram with black and white colours
        if (!(json.has("states"))) {
            return new Nonogram(name, rowContraints, colConstraints, rows, cols);
        } else {
            return new Nonogram(name, this.parseStates(json.getJSONObject("states")), rowContraints, colConstraints,
                    rows, cols);
        }
    }

    /**
     * Parse the defined states in the JSON, if present.
     * 
     * @param customStates A JSON object containing the custom state colours defined
     *                     in the JSON input
     * @return A map containing the custom states mapped to the colours they
     *         represent
     */
    private Map<CellState, Color> parseStates(JSONObject customStates) {
        Map<CellState, Color> colourMap = new TreeMap<>();

        for (String state : customStates.keySet()) {
            try {
                // Get custom state and colour from the JSON, convert them from String to
                // CellState and Color types respectively, and add them to colour map
                colourMap.put(Enum.valueOf(CellState.class, state), Color.decode(customStates.getString(state)));
            } catch (IllegalArgumentException e) {
                System.out.println(
                        "Argument Error: Some of the custom colours could not be recognised, so the image may display incorrectly. Check the JSON input.");
            }
        }
        return colourMap;
    }

    /**
     * Parse the constraints of a puzzle from JSON input.
     * 
     * @param jsonArray The JSON array of constraints to be parsed
     * @return a 2D array of BlockConstraint objects, containing length and state of
     *         the constraint
     */
    private BlockConstraint[][] parseConstraints(JSONArray jsonArray) {
        BlockConstraint[][] constraints = new BlockConstraint[jsonArray.length()][];

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray innerArray = jsonArray.getJSONArray(i);
            List<BlockConstraint> counts = new ArrayList<>();
            for (int j = 0; j < innerArray.length(); j++) {
                JSONObject obj = innerArray.getJSONObject(j);

                // Get count and colour (if present) for each constraint from JSON
                int count = obj.getInt("count");

                CellState state;
                if (obj.has("color")) {
                    state = Enum.valueOf(CellState.class, obj.getString("color"));
                    // If no custom states are defined, set the colour for each constraint to the
                    // default COLOUR_1
                } else {
                    state = CellState.COLOUR_1;
                }

                counts.add(new BlockConstraint(count, state));
            }
            constraints[i] = new BlockConstraint[counts.size()];
            for (int j = 0; j < counts.size(); j++) {
                constraints[i][j] = counts.get(j);
            }
        }
        return constraints;
    }
}