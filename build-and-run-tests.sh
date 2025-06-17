#!/bin/bash

javac -cp "lib/*" -d out src/**.java

# Run Nonogram JUnit tests
echo "--- TESTING NONOGRAM ---"
java -cp lib/*:out org.junit.runner.JUnitCore src.TestNonogram

# Run Solver JUnit tests
echo "--- TESTING GUESSER AND SOLVER ---"
java -cp lib/*:out org.junit.runner.JUnitCore src.TestSolverAndGuesser