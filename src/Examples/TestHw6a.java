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

public class TestHw6a {
/*********************************************************************************
 *  This module tests the program of homework set hw6.
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
 **********************************************************************************/
    
    static class CSC205HW6a {
        
        int Qn1(int input) {
            int x = 0; 
            int y = 1;
            int z = 2;

            x = input;
            if (x>y) {
              x = -y;
            }
            else {
              x = 1+z;
            }
            return x;
        }
        
        // default sample program for this project, if one exists.
       static final String DefaultMasfilepath = "testbed/CSC205-hw6aQ1.mas"; 
        
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
            return "" + Qn1(X);
        }

        // Compute the expected output of multiple test runs on various inputs.
        public String[] output(int X[]) {
            String data[] = new String[X.length];
            for (int index=0; index<X.length; index++) {
                data[index] = "" + Qn1(X[index]);
            }
            return data;
        }
    }

    public static void testSingleRun(String masfilepath) {
        CSC205HW6a fn = new CSC205HW6a();
        final int X=1;
        final String input = fn.input(X);
        final String expectedOutput = fn.output(X);
        final int maxPollCount = 3;

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).singleRunDec2Dec(masfilepath, input, expectedOutput, maxPollCount);
        
    }
        


    public static void testMultiRun(String masfilepath) {
        CSC205HW6a fn = new CSC205HW6a();

        int X[] = new int[10];
        for (int i=0; i<X.length; i++) {
            X[i] = i - 5;
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
            Usage.print(TestHw6a.class, CSC205HW6a.DefaultMasfilepath);
            testMultiRun(null);
        }
    }    
}
