/*
NEU INFO 5100, Final Project - Number Recognizer
Author:
    Name: Xinzhe Yuan, NUID:
    Name: Jia Xu, NUID:
Date: 13 Nov 2023
Version: 0.1

 */


package com.neu.info5100.numberrecognizer;

public class Prediction {
    private int mostPossibleNum;
    private float mostPossibleNumPossibility;
    private int secondPossibleNum;
    private float secondPossibleNumPossibility;

    public Prediction() {

    }

    public int getMostPossibleNum() {
        return mostPossibleNum;
    }

    public float getMostPossibleNumPossibility() {
        return mostPossibleNumPossibility;
    }

    public int getSecondPossibleNum() {
        return secondPossibleNum;
    }

    public float getSecondPossibleNumPossibility() {
        return secondPossibleNumPossibility;
    }

    public Prediction(int mostPossibleNum, float mostPossibleNumPossibility, int secondPossibleNum, float secondPossibleNumPossibility) {
        this.mostPossibleNum = mostPossibleNum;
        this.mostPossibleNumPossibility = mostPossibleNumPossibility;
        this.secondPossibleNum = secondPossibleNum;
        this.secondPossibleNumPossibility = secondPossibleNumPossibility;
    }
}
