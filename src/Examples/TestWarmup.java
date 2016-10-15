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

public class TestWarmup {
/*********************************************************************************
 * 
 *  Example: Multiplication of two positive integers
 * 
 *  The test method testSingleRun() implements a IO test which executes the project once.
 *  Upon completion of the single run, the test outputs are compared with the 
 *  expected outputs.
 *
 *  The test method testMultiRun() implements a IO test which executes the project 
 *  multiple times.
 *  Upon completion of the each run, the test outputs are compared with the 
 *  expected outputs.
 * 
 *  A sample project is provided for demonstration. 
 *********************************************************************************/
    
    static class PositiveMultiplication {
        
        // default sample program for this project, if exists.
        static final String DefaultMasfilepath = "testbed/warmup.mas"; 
        
        // Convert a pair of integer values to a string as input to the simulator
        public String input(int X, int Y) {
            return "" + X + " " + Y;
        }

        
        // Convert a pair of arrays of integer values to an array of strings.
        // Each string should contain two integers. 
        // Each string is the input to a test run executed by the simulator
        public String[] input(int X[], int Y[]) {
            String data[] = new String[X.length];
            for (int index=0; index<X.length; index++) {
                data[index]= "" + X[index] + " " + Y[index];
            }
            return data;
        }
        
        
        // Compute the expected output of the test run for a given input.
        // When Y<=0, the program is expected to run infinitely, expecting no output.
        public String output(int X, int Y) {
            if (Y>0)
                return "" + (X * Y);
            else
                return "";
        }

        
        // Compute the expected output of multiple test runs on various inputs.
        // When Y<=0, the program is expected to run infinitely, expecting no output.
        public String[] output(int X[], int Y[]) {
            String data[] = new String[X.length];
            for (int index=0; index<X.length; index++) {
                if (Y[index]>0) {
                    data[index]= "" + (X[index]*Y[index]);
                }
                else {
                    data[index]=null; // Not defined when Y<=0
                }
            }
            return data;
        }
    }

    public static void testSingleRun(String masfilepath) {
        PositiveMultiplication fn = new PositiveMultiplication();
        final int X=1;
        final int Y=2;
        final String input = fn.input(X, Y);
        final String expectedOutput = fn.output(X, Y);
        final int maxPollCount = 3;

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).singleRunDec2Dec(masfilepath, input, expectedOutput, maxPollCount);
    }
        

    public static void testMultiRun(String masfilepath) {
        PositiveMultiplication fn = new PositiveMultiplication();

        int X[] = {1, 0, -4, 6, 20};
        int Y[] = {3, 9, 1, -1, 0};
        String inputs[] = fn.input(X, Y);
        String expectedOutputs[] = fn.output(X, Y);
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
            Usage.print(TestWarmup.class, PositiveMultiplication.DefaultMasfilepath);
            testMultiRun(null);
        }
    }    
}
