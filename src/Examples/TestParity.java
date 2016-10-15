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


public class TestParity {
/*********************************************************************************
 * Example: Parity checker
 * 
 * In this example, the Marie Project is to implement a parity checker in Marie.
 * The program's expected behavior is to accept a character as input
 * and report the following 3 pieces of information:
 * 1. the decimal value of the ASCII code
 * 2. the number of 1 in binary form of the ASCII code
 * 3. Whether to add a 1 to make for even parity. 
 
 * The Parity class generates all the input characters to be tested and 
 * the corresponding expected outputs for each ASCII code.
 
 * The test method testSingleRun() implements a IO test which executes the project once.
 * Upon completion of the single run, the test outputs are compared with the 
 * expected outputs.

 * The test method testMultiRun() implements a IO test which executes the project 
 * multiple times.
 * Upon completion of the each run, the test outputs are compared with the 
 * expected outputs.
 
 * A sample project (parity-brute.mas) which incompletely implements the parity 
 * checker is provided for demonstration. 
 **********************************************************************************/
    

    static class Parity {
        static final byte start = 33;  // Start at 33 instead of 32 (because 32 is whitespace, which is not directly accepted by MarieSim)
        static final byte end = 126;   // End at the last ASCII code. 
        
        // default sample program for this project
        static final String DefaultMasfilepath = "testbed/parity-brute.mas"; 

        // Generate the sequence of ascii characters within the given range and including the boundary.
        public String input(int start, int end) {
            // If the range is invalid, fix it with the correct range.
            if (start<Parity.start) start=Parity.start;
            if (end>Parity.end) end=Parity.end;

            String ascii = "";
            for (byte i=(byte)start; i<=(byte)end; i++) {
                // Convert the byte to character (note: Java uses unicode)
                char c = (char) i;
                if (ascii.length()!=0)
                    ascii += " ";
                ascii += c;
            }
            return ascii;
        }
        
        public String toBinary(char c) {
            // Convert the byte to binary string
            // Reference: http://stackoverflow.com/questions/12310017/how-to-convert-a-byte-to-its-binary-string-representation
            return String.format("%8s", Integer.toBinaryString(c & 0xFF)).replace(' ', '0');
        }
        
        

        // Provide result for each ascii character
        // Output for each character includes: 
        // (1) the decimal value of the ASCII code
        // (2) the number bits with value 1 in the code
        // (3) whether an additional 1 bit is needed to make the parity even.
        public String output(char c) {
            // Convert the char to decimal
            String binary = toBinary(c);
            int dec = (int) (c & 0xFF);
            // Count the number of 1s in the binary string.
            // Reference: http://stackoverflow.com/questions/275944/java-how-do-i-count-the-number-of-occurrences-of-a-char-in-a-string
            int count = binary.length() - binary.replace("1", "").length();
            int parity = count%2==0 ? 0 : 1;
            return dec + " " + count + " " + parity;
        }
        
        public String output(String input) {
            if (input==null) return null;
            // split input by whitespaces.
            String[] chars = input.trim().split("\\s");
            String result = "";
            for (String token : chars) {
                if (token.length()>=1) {
                    if (result.length()>0) result += " ";
                    result += output(token.charAt(0));
                }
            }
            return result;
        }
        
        // Provide result for each ascii character in the specific range.
        public String output(int start, int end) {
            if (start<Parity.start) start=Parity.start;
            if (end>Parity.end) end=Parity.end;

            String result = "";
            for (byte i=(byte)start; i<=(byte)end; i++) {
                result += output((char) i) + " ";
            }
            return result;
        }
        
        // Support printing of ascii for visual verification if needed.
        public void print() {
            for (byte i=start; i<=end; i++) {
                char c = (char) i;
                // Add a space every 4 bits for ease of reading.
                String binary = toBinary(c);
                binary = binary.substring(1, 4) + " " + binary.substring(4, 8);
                System.out.println(c + " " + binary + " " + output(c));
            }
        }
        
    }
    
    
    public static void testSingleRun(String masfilepath) {
        Parity fn = new Parity();
        
        // Test just the first few ASCII characters.
        int start=33;
        int end=35;
        final String input = fn.input(start, end);
        final String expectedOutput = fn.output(start, end);
        final int maxPollCount = 10;

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).singleRunAscii2Dec(masfilepath, input, expectedOutput, maxPollCount);
    }
    
    
    public static void testMultiRunAllChars(String masfilepath) {
        Parity fn = new Parity();
        final int inputsPerRun = 10;
        final int maxPollCount = (int)((double)inputsPerRun*2.5);  // tune accordingly to allow enough time.

        // Test all ASCII characters
        int total = (int)(Parity.end-Parity.start)+1;

        // Count the total number of runs.
        int runs = (int) Math.ceil((double)total/(double)inputsPerRun);

        String inputs[] = new String[runs];
        String expectedOutputs[] = new String[runs];

        for (int run=0; run<runs; run++) {
            int start = run*inputsPerRun + Parity.start;
            int end = start + inputsPerRun - 1;

            // If start or end is out of range, the fn method will automatically fix it.
            inputs[run] = fn.input(start, end);
            expectedOutputs[run] = fn.output(start, end);
        }

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).multiRunsAscii2Dec(masfilepath, inputs, expectedOutputs, maxPollCount);
        
    }

    public static void testMultiRun(String masfilepath) {
        Parity fn = new Parity();
        final int inputsPerRun = 6;
        final int maxPollCount = (int)((double)inputsPerRun*2.5);  // tune accordingly to allow enough time.

        String inputs[] = { "H i !",
                            "{ W }",
                            "z Z",
                            "2 3 4",
                            "A b C d",
                            "@ A d e g o"};

        String expectedOutputs[] = new String[inputs.length];
        for (int run=0; run<inputs.length; run++) {
            expectedOutputs[run] = fn.output(inputs[run]);
        }

        if (masfilepath==null) masfilepath = fn.DefaultMasfilepath;
        (new Test()).multiRunsAscii2Dec(masfilepath, inputs, expectedOutputs, maxPollCount);
    }

    

    public static void main(String args[]) {
        if (args.length>=1) {
            testMultiRunAllChars(args[0]);
        }
        else {
            // default
            Usage.print(TestParity.class, Parity.DefaultMasfilepath);
            testMultiRunAllChars(null);
        }
    }    
}
