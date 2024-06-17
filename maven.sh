#!/bin/bash
@echo off
clear

# Navigate to the Project Directory:
pushd /Users/percyrotteveel/workspace/WeeklyEvents 

# Clean the Project:
mvn clean

# Compile the Project:
mvn compile

# Package the Project:
mvn package

# Run the Project:
mvn javafx:run

# Go back from where we came
popd