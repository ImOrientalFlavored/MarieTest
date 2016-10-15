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

public class TestArray {
/*********************************************************************************
 *  This module tests the sequential access of an array.
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
 **********************************************************************************/
    
    static class Array {
        
        // default sample program for this project, if one exists.
        static final String DefaultMasfilepath = "testbed/array.mas";         
        
        int array[] = {64, 32, 16, 8, 4, 2, 1};
    
        // No output is generated.
        public String output() {
            String result = "";
            for (int index=0; index<array.length; index++) {
                if (index>0) result += " ";
                result += index + " " + array[index];
            }
            return result;
        }
    }

    public static void testSingleRun(String masfilepath) {
        Array fn = new Array();
        final String input = "";
        final String expectedOutput = fn.output();
        final int maxPollCount = 3;

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).singleRunDec2Dec(masfilepath, input, expectedOutput, maxPollCount);        
    }

    
    // Primarily for batch mode to test multiple files.
    // Each file is tested only once.
    public static void testMultiRun(String maspath) {
        Array fn = new Array();
        String inputs[] = {""};  
        String expectedOutputs[] ={fn.output()};
        final int maxPollCount = 3;  // tune accordingly to allow enough time.

        if (maspath==null) maspath = fn.DefaultMasfilepath;
        (new Test()).multiRunsDec2Dec(maspath, inputs, expectedOutputs, maxPollCount);
    }

    

    public static void main(String args[]) {
        if (args.length>=1) {
            testMultiRun(args[0]);
        }
        else {
            // default
            Usage.print(TestArray.class, Array.DefaultMasfilepath);
            testSingleRun(null);
        }
    }    
}
