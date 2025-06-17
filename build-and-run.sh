#!/bin/bash

javac -cp "lib/*" -d out src/**.java
java -cp "lib/*":out src/GUI