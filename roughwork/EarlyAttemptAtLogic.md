1. Guessing strategy
    The solver first tries deductive solving ( existing solver)
    If that fails, it finds the most constrained cell (with fewest possibilities)
    It makes educated guesses based on possible valid states for that cell

2. Backtracking

    When a guess leads to a contradiction (no valid solution), it backtracks
    The solver tries alternative guesses until it finds a solution or exhausts all possibilities

3. Finding Best Guess Cell

    Prioritizes cells with the fewest possible valid states
    Considers both row and column constraints to determine possible states

4. Possible States Calculation

    For each UNKNOWN cell, determines which states are valid given:
        The row's constraints and current state
        The column's constraints and current state

4. Handling Special Cases
For multi_checks.json (multiple solutions):

    The solver will find one valid solution
    The first valid path in the search tree will be returned

For player.json (requires deep backtracking):

    The solver may take longer as it explores multiple guess paths
    The depth of recursion is limited by Java's stack size (may need optimization for very large puzzles)

5. Optimization Tips

    Memoization: Cache line solutions to avoid recomputing
    Early Termination: Check for contradictions earlier in the guessing process
    Heuristics: Improve the guess selection strategy (e.g., prefer cells that appear in the most constraints)
    Parallelization: Explore multiple guess paths simultaneously (advanced)