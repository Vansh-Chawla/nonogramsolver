package src;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI extends JFrame {
    private Nonogram puzzle;
    private JButton[][] gridButtons;
    private JLabel statusLabel;
    private JPanel gridPanel;
    private JPanel keyPanel;
    private Boolean isInitalised;
    private ArrayList<JButton> keyPanelButtons = new ArrayList<JButton>();
    private Color currentColour;
    private JPanel loadingPanel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI gui = new GUI();
            gui.setVisible(true);
        });
    }

    /**
     * The constructor to create the GUI for the program, including menu bar, file
     * explorer and Check Solution button.
     */
    public GUI() {
        isInitalised = false;
        setTitle("Nonogram Puzzle Solver");

        // Set window to full screen and non-resizable
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.setResizable(false);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Main layout with NORTH (status), CENTER (content), and SOUTH (buttons)
        setLayout(new BorderLayout());

        // Create and add the status label (empty initially)
        statusLabel = new JLabel("");
        add(statusLabel, BorderLayout.NORTH);

        // Show loading panel initially
        createLoadingPanel();
        add(loadingPanel, BorderLayout.CENTER);
    }

    /**
     * Method to check whether a puzzle has been loaded yet and returns an error
     * message if not;
     * used for buttons such as load, save and check
     */
    private void noPuzzleLoadedError() {
        if (puzzle == null) {
            JOptionPane.showMessageDialog(GUI.this,
                    "No puzzle loaded!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    /**
     * Creates a Panel of all buttons at the bottom of the screen
     * 
     * @return The panel of buttons
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        // Check Solution Button
        JButton checkButton = new JButton("Check Solution");
        checkButton.addActionListener(e -> checkSolution());
        buttonPanel.add(checkButton);

        // Load Moves Button
        JButton loadMovesButton = new JButton("Load Moves");
        Action loadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noPuzzleLoadedError();

                // set file directory
                JFileChooser fileChooser = new JFileChooser("savedpuzzles");
                fileChooser.setFileFilter(
                        new FileNameExtensionFilter("JSON Files", "json"));
                int returnVal = fileChooser.showOpenDialog(GUI.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        // if valid json file, load moves
                        File file = fileChooser.getSelectedFile();
                        puzzle.loadMoves(file.getAbsolutePath());
                        updateGridColors();
                        JOptionPane.showMessageDialog(GUI.this,
                                "Moves loaded successfully!", "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        // if invalid json file, display error
                        JOptionPane.showMessageDialog(GUI.this,
                                "Error loading moves: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        // add action listener and keyboard shortcute for load moves button
        loadMovesButton.addActionListener(loadAction);
        loadMovesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control O"), "load");
        loadMovesButton.getActionMap().put("load", loadAction);
        buttonPanel.add(loadMovesButton);

        // Save Moves Button
        JButton saveMovesButton = new JButton("Save Moves");
        Action saveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noPuzzleLoadedError();

                // set file directory

                new File("savedpuzzles").mkdirs();
                JFileChooser fileChooser = new JFileChooser("savedpuzzles");
                fileChooser.setFileFilter(
                        new FileNameExtensionFilter("JSON Files", "json"));
                fileChooser.setSelectedFile(new File(puzzle.getName() + "_moves.json"));
                int userSelection = fileChooser.showSaveDialog(GUI.this);
                if (userSelection == JFileChooser.APPROVE_OPTION) {

                    // get absolute file path
                    File fileToSave = fileChooser.getSelectedFile();
                    String filePath = fileToSave.getAbsolutePath();

                    // add .json to end of file path
                    if (!filePath.toLowerCase().endsWith(".json")) {
                        fileToSave = new File(filePath + ".json");
                    }
                    try {
                        // if valid save moves
                        puzzle.saveMoves(fileToSave.getAbsolutePath());
                        JOptionPane.showMessageDialog(GUI.this,
                                "Moves saved successfully to:\n" + fileToSave.getAbsolutePath(),
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(GUI.this,
                                "Error saving moves: " + ex.getMessage(), "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        // add action listener and keyboard shortcut for save moves button
        saveMovesButton.addActionListener(saveAction);
        saveMovesButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control S"), "save");
        saveMovesButton.getActionMap().put("save", saveAction);
        buttonPanel.add(saveMovesButton);

        // Undo Button
        JButton undoButton = new JButton("Undo");
        Action undoAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noPuzzleLoadedError();

                // if moves can be undone, update cell state accordingly
                if (puzzle.undo()) {
                    updateGridColors();
                } else {

                    // if no moves can be undone, display error message
                    JOptionPane.showMessageDialog(GUI.this, "No moves to undo!", "Info",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        // add action listener and keyboard shortcut for undo button
        undoButton.addActionListener(undoAction);
        undoButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control Z"), "undo");
        undoButton.getActionMap().put("undo", undoAction);
        buttonPanel.add(undoButton);

        // Reset Button
        JButton resetButton = new JButton("Reset");
        Action resetAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noPuzzleLoadedError();

                // if moves have been made reset grid
                if (puzzle.resetMoves()) {
                    updateGridColors();
                } else {

                    // if there are no moves to reset, display error message
                    JOptionPane.showMessageDialog(GUI.this,
                            "No moves have been made yet!", "Info",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };

        // add action listener and keyboard shortcut for reset button
        resetButton.addActionListener(resetAction);
        resetButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control R"), "reset");
        resetButton.getActionMap().put("reset", resetAction);
        buttonPanel.add(resetButton);

        // Solve Button
        JButton solveButton = new JButton("Solve");
        Action solveAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                noPuzzleLoadedError();

                // create guesser object
                Guesser guesser = new Guesser(puzzle);
                boolean solved = guesser.solve();

                updateGridColors();

                // display messages depending on whether the puzzle could be solved or not
                if (solved) {
                    JOptionPane.showMessageDialog(GUI.this, "Puzzle Solved!", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(GUI.this, "Could not completely solve the puzzle.", "Info",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        };

        // add action listener and keyboard shortcut for solve button
        solveButton.addActionListener(solveAction);
        solveButton.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("control L"), "solve");
        solveButton.getActionMap().put("solve", solveAction);
        buttonPanel.add(solveButton);

        return buttonPanel;
    }

    /**
     * Creates and displays loading screen with nonogram of platypus
     */
    private void createLoadingPanel() {
        loadingPanel = new JPanel(new BorderLayout());

        // Panel for the image
        JPanel imagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                try {
                    // Set background color
                    // Load and draw the image
                    BufferedImage myPicture = ImageIO.read(new File("logo.png"));
                    Image scaledImage = myPicture.getScaledInstance(
                            getWidth() / 2, -1, Image.SCALE_SMOOTH);
                    int x = ((getWidth() - scaledImage.getWidth(null)) / 2);
                    int y = 100 + (getHeight() - scaledImage.getHeight(null)) / 3;
                    g.drawImage(scaledImage, x, y, null);

                    // Add loading text
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.BOLD, 36));
                    String text = "Load a puzzle to begin";
                    FontMetrics fm = g.getFontMetrics();
                    int textX = (getWidth() - fm.stringWidth(text)) / 2;
                    int textY = (scaledImage.getHeight(null) + 50) + 150;
                    g.drawString(text, textX, textY);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // Panel for the buttons on loading screen
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));

        // Load Puzzle button
        JButton loadPuzzleButton = new JButton("Load Puzzle");
        loadPuzzleButton.addActionListener(e -> loadPuzzle());
        loadPuzzleButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(loadPuzzleButton);

        // Help button
        JButton helpButton = new JButton("Help");
        helpButton.addActionListener(e -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    // open pdf file
                    Desktop.getDesktop().open(new File("help.pdf"));
                } else {
                    // if error occurs display alert
                    JOptionPane.showMessageDialog(this,
                            "Cannot open help file automatically. Please open help.pdf "
                                    + "manually.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        "Help file not found: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        helpButton.setPreferredSize(new Dimension(150, 40));
        buttonPanel.add(helpButton);
        // Add components to main panel
        loadingPanel.add(imagePanel, BorderLayout.CENTER);
        loadingPanel.add(buttonPanel, BorderLayout.SOUTH);
        loadingPanel.setPreferredSize(new Dimension(800, 600));
    }

    /**
     * Updates the cell state of the puzzle's buttons
     */
    private void updateGridColors() {
        for (int i = 1; i < gridButtons.length; i++) {
            for (int j = 1; j < gridButtons[0].length; j++) {
                CellState state = puzzle.getCellState(i - 1, j - 1);
                gridButtons[i][j].setBackground(puzzle.getStateColour(state));
            }
        }
    }

    /**
     * Clear the current puzzle, if one has been initialised (is currently being
     * displayed).
     */
    private void clearGrid() {
        if (isInitalised) {
            this.remove(gridPanel);
            this.remove(keyPanel);
            this.statusLabel.setText("");
        } else {
            this.remove(loadingPanel);
        }
    }

    /**
     * Load a puzzle from file using the file explorer.
     */
    private void loadPuzzle() {
        JFileChooser fileChooser = new JFileChooser("puzzles/");
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                puzzle = new PuzzleLoader().loadPuzzle(file.getPath());
                clearGrid(); // This will now remove the loading panel
                initializeGridGUI();
                // Create the button panel (always present)
                JPanel buttonPanel = createButtonPanel();
                add(buttonPanel, BorderLayout.SOUTH);

                statusLabel = new JLabel("");
                add(statusLabel, BorderLayout.NORTH);
                String text = " Running " + puzzle.getName() + " Puzzle";
                statusLabel.setText(text);
                statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
                statusLabel.setFont(new Font("Tahoma", Font.BOLD, 24));
                isInitalised = true; // Set this after initialization
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading puzzle: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Checks whether the solution entered on the grid is the correct one and
     * display messages accordindly
     */
    public void checkSolution() {

        noPuzzleLoadedError();

        boolean isCorrect = puzzle.isSolved();
        if (isCorrect) {
            JOptionPane.showMessageDialog(this, "Correct Solution!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    this, "Incorrect Solution!", "Success", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Creates the panel of buttons on the right hand side of the screen giving the
     * user the ability to change colours/cell state of grid cells
     */

    private void colourKeyPanel() {

        // Get a map of the colours and cell states in the current puzzle
        Map<CellState, Color> colourMap = puzzle.getColourMap();

        // create a new panel
        keyPanel = new JPanel(new GridBagLayout());
        JLabel instructionLabel = new JLabel("     Click to change colour");
        GridBagConstraints gbc = new GridBagConstraints();
        // gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // padding
        gbc.insets = new Insets(10, 10, 10, 10);
        keyPanel.add(instructionLabel, gbc);
        int row = 1;

        // loop for each entry in the cell state map
        for (Map.Entry<CellState, Color> entry : colourMap.entrySet()) {

            // create a new buttona nd set dimensions/properties
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(30, 30));
            button.setBackground(entry.getValue());
            button.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
            button.addActionListener(e -> changeColour(entry.getKey()));
            button.addActionListener(e -> updateButtonBorder(button));
            keyPanelButtons.add(button);

            JLabel label = new JLabel(entry.getKey().toString());
            // button in 1st column
            gbc.gridx = 0;
            // row increments
            gbc.gridy = row;
            // padding
            gbc.insets = new Insets(5, 5, 5, 5);
            if (row == 3) {
                button.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.red));
            }
            keyPanel.add(button, gbc);

            // label in 2nd column
            gbc.gridx = 1;
            // row increments
            gbc.gridy = row;
            // padding
            gbc.insets = new Insets(5, 5, 5, 5);
            keyPanel.add(label, gbc);
            row += 1;
        }
        this.add(keyPanel, BorderLayout.EAST);
    }

    /**
     * Sets the currently selected button's border in colour panel to red and all
     * other buttons to black
     * 
     * @param button Currently selected button - border of which will be updated to
     *               red
     */
    private void updateButtonBorder(JButton button) {
        for (JButton btn : keyPanelButtons) {
            btn.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        }
        button.setBorder(BorderFactory.createMatteBorder(3, 3, 3, 3, Color.red));
    }

    /**
     * Changes the current selection of the colour the user wishes to use to update
     * the grid
     * 
     * @param cellState The cellstate of that button
     */
    private void changeColour(CellState cellState) {
        this.currentColour = puzzle.getStateColour(cellState);
    }

    /**
     * Initialise the GUI for the nonogram grid.
     */
    private void initializeGridGUI() {
        isInitalised = true;
        colourKeyPanel();

        // Adds menu bar for loading new files
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("file");
        JMenuItem loadItem = new JMenuItem("Load Puzzle");
        loadItem.addActionListener(e -> loadPuzzle());
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        gridPanel = new JPanel(new GridBagLayout());
        // Set background color
        gridButtons = new JButton[puzzle.getRowConstraints().length + 1][puzzle.getColumnConstraints().length + 1];
        GridBagConstraints gbc = new GridBagConstraints();
        gridPanel.setBackground(Color.decode("#D3D3D3"));

        // Set the current colour
        this.currentColour = puzzle.getStateColour(CellState.COLOUR_1);

        // creates blank corner in top left of grid
        gbc.gridx = 0;
        gbc.gridy = 0;
        gridPanel.add(new JLabel(), gbc);

        // Add column labels
        for (int j = 1; j < gridButtons[0].length; j++) {
            gbc.gridx = j;
            gbc.gridy = 0;
            JPanel columnPanel = columnConstraintsLabel(puzzle.getColumnConstraints()[j - 1]);
            gridPanel.add(columnPanel, gbc);
        }
        // Add row labels
        for (int i = 1; i < gridButtons.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            JPanel rowPanel = rowConstraintsLabel(puzzle.getRowConstraints()[i - 1]);
            gridPanel.add(rowPanel, gbc);
        }
        // Add the grid buttons
        for (int i = 1; i < gridButtons.length; i++) {
            for (int j = 1; j < gridButtons[0].length; j++) {
                final int row = i;
                final int col = j;
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(42, 42));
                button.setBackground(Color.decode("0xECECEC"));
                button.addActionListener(e -> {
                    // Use the final copies instead of i and j
                    CellState currentState = puzzle.getCellState(row - 1, col - 1);
                    // Determine new state based on current color
                    CellState newState = CellState.UNKNOWN;
                    for (Map.Entry<CellState, Color> entry : puzzle.getColourMap().entrySet()) {
                        if (entry.getValue().equals(this.currentColour)) {
                            newState = entry.getKey();
                            break;
                        }
                    }
                    if (currentState != newState) {
                        puzzle.setCellState(row - 1, col - 1, newState);
                        button.setBackground(this.currentColour);
                    }
                });
                // grid button
                gbc.gridx = j;
                gbc.gridy = i;
                gridButtons[i][j] = button;
                gridPanel.add(button, gbc);
            }
        }
        this.add(gridPanel, BorderLayout.CENTER);

        // Undo the last move when the undo button is pressed
        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> {
            puzzle.undo();
        });

        // Reset the grid to the loaded state when the reset button is pressed
        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            puzzle.resetGrid();
        });

        getContentPane().add(gridPanel, BorderLayout.CENTER);
        revalidate();
    }

    /**
     * Turns constraints for one row into a string label that can be displayed to
     * the left of that row. Consists of the constraints separated by commas.
     * 
     * @param constraints The row constraints to be processed
     * @return The string label to be displayed to the left of the row
     */
    private JPanel rowConstraintsLabel(BlockConstraint[] constraints) {
        // create a new panel
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        String outString = "";
        rowPanel.setOpaque(true);
        rowPanel.setBackground(Color.decode("#D3D3D3"));

        // loop for every constraint in that patricular row
        for (int index = 0; index < constraints.length; index++) {
            outString = "";
            outString += constraints[index].getLength();

            // create a label for each particular number and assign its colour
            JLabel label = new JLabel(outString);
            label.setOpaque(true);
            label.setForeground(puzzle.getStateColour(constraints[index].getState()));
            label.setFont(new Font("Tahoma", Font.BOLD, 14));
            label.setBackground(Color.decode("#D3D3D3"));
            rowPanel.add(label);

            // if not the last constraint number, create a label to display a comma in
            // the colour black
            if (index < constraints.length - 1) {
                outString = ",";
                JLabel labelComma = new JLabel(outString);
                labelComma.setOpaque(true);
                labelComma.setForeground(Color.black);
                labelComma.setFont(new Font("Tahoma", Font.BOLD, 14));
                labelComma.setBackground(Color.decode("#D3D3D3"));
                rowPanel.add(labelComma);
            }
        }
        return rowPanel;
    }

    /**
     * Turns constraints for one column into a string label that can be displayed
     * above that column. Consists of the constraints separated by newlines.
     * 
     * @param constraints The column constraints to be processed
     * @return The string label to be displayed above the column
     */
    private JPanel columnConstraintsLabel(BlockConstraint[] constraints) {
        // new panel with specific grid layout so column labels are displayed
        // vertically on top of each other
        JPanel columnPanel = new JPanel(new GridLayout(0, 1));
        columnPanel.setBackground(Color.decode("#D3D3D3"));
        String outString = "";
        for (int index = 0; index < constraints.length; index++) {
            outString = "";
            outString += constraints[index].getLength();

            // create a label for each particular number and assign its colour
            JLabel label = new JLabel(outString);
            label.setOpaque(true);
            label.setForeground(puzzle.getStateColour(constraints[index].getState()));
            label.setFont(new Font("Tahoma", Font.BOLD, 14));
            label.setBackground(Color.decode("#D3D3D3"));
            columnPanel.add(label);
        }
        return columnPanel;
    }
}
