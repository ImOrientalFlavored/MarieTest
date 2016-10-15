package Examples;

/*
 * Copyright (c) 2015-2016 Annie Hui @ NVCC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import MarieSimTester.Test;

public class TestSquare {
/*********************************************************************************
 *  This module tests the square of an integer.
 * 
 *  The test method testSingleRun() implements a IO test which executes the project once.
 *  Upon completion of the single run, the test outputs are compared with the 
 *  expected outputs.
 *
 *  The test method testMultiRun() implements a IO test which executes the project 
 *  multiple times.
 *  Upon completion of the each run, the test outputs are compared with the 
 *  expected outputs.
 **********************************************************************************/
    
    static class Square {
        
        // default sample program for this project, if one exists.
        static final String DefaultMasfilepath = "testbed/square.mas"; 
        
        // Convert an integer value to a string as input to the simulator
        public String input(int X) {
            return "" + X;
        }

        
        // Convert an array of integer values to an array of strings.
        // Each string is the input to a test run executed by the simulator
        public String[] input(int X[]) {
            String data[] = new String[X.length];
            for (int index=0; index<X.length; index++) {
                data[index]= "" + X[index];
            }
            return data;
        }
        
        // Compute the expected output of the test run for a given input.
        public String output(int X) {
            return "" + (X*X);
        }

        // Compute the expected output of multiple test runs on various inputs.
        public String[] output(int X[]) {
            String data[] = new String[X.length];
            for (int index=0; index<X.length; index++) {
                data[index] = "" + (X[index]*X[index]);
            }
            return data;
        }
    }

    public static void testSingleRun(String masfilepath) {
        Square fn = new Square();
        final int X=1;
        final String input = fn.input(X);
        final String expectedOutput = fn.output(X);
        final int maxPollCount = 3;

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).singleRunDec2Dec(masfilepath, input, expectedOutput, maxPollCount);        
    }

    public static void testMultiRun(String masfilepath) {
        Square fn = new Square();
        int total = 41;
        int X[] = new int[total];
        for (int i=0; i<total; i++) {
            X[i] = i-total/2;
        }
        
        String inputs[] = fn.input(X);
        String expectedOutputs[] = fn.output(X);
        final int maxPollCount = 3;  // tune accordingly to allow enough time.

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).multiRunsDec2Dec(masfilepath, inputs, expectedOutputs, maxPollCount);
    }

    

    public static void main(String args[]) {
        if (args.length>=1) {
            testMultiRun(args[0]);
        }
        else {
            // default
            Usage.print(TestSquare.class, Square.DefaultMasfilepath);
            testMultiRun(null);
        }
    }    
}
