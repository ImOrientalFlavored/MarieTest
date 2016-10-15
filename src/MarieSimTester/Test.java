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

package MarieSimTester;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/*
 *
 * This is an IO test module for programs written to be run on the Marie Simulator (MarieSim).
 *
 * In order to take advantage of direct access to the fields in MarieSim without using reflections,
 * this test module must be placed in the same package as MarieSim.
 *
 * This Test module performs the following tasks:
 * 1. Assemble a MARIE source file.
 * 2. Test the object file in MarieSim with pre-determined input.
 *
 * Description:
 * 1. Assemble a given .mas file to obtain the .mex file.
 * 2. Upon successful assembly, load the .mex file into the MarieSim for execution.
 * 3. Periodically poll the MarieSim to check if the input register is editable,
 *    (MarieSim sets the input register to editable if and only if it is waiting for input).
 *    Use a robot to enter pre-determined input upon the request of MarieSim.
 * 4. Terminate a test run if either the allocated time is up, or MarieSim is halted, 
 *    whichever comes first.
 * 5. Upon completion of a test run, compare MarieSim's output with any expected output
 *    provided by the tester.
 *
 * Additional features:
 * 1. Tests may be conducted in a single run or in multiple runs. 
 * 2. Save all test results to a log file.
 * 3. For testing projects in batch mode, run this test module from command line and provide
 *    the directory of the *.mas files as argument.
 *
 * Limitations:
 * On some occasions, the system may need help from the tester to obtain focus of the mouse
 * at the beginning of the test. (use alt-tab or alt-shift-tab on keyboard)
 *
 */

public class Test {
    static final long PollInterval = 2000; // Poll the simulator's input register after each interval (msec)
    static final boolean CloseOnCompletion = true;
    
    // Objects that may be changed by the polling thread.
    MarieSim instance;
    Robot robot;
    Savelog log;
    int pollCount = 0;
    int inputIndex = 0;

    int runCount = 0;
    int fileCount = 0;
    
    // Define custom enum type to encapsule MarieSim's input IOMode
    // in case the tester does not know the implementation within MarieSim.
    private enum IOMode {
        hex(MarieSim.HEX),
        ascii(MarieSim.ASCII),
        dec(MarieSim.DEC);

        private int value;    
        private IOMode(int value) { this.value = value; }
        public int getValue() { return value; }
    };

    // Initiate the important variables 
    public Test() {
        instance = new MarieSim();
        instance.setVisible(true);
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            System.exit(0);
        }
        log = new Savelog();
    }

    
    // A log file to save the test results.
    static class Savelog {
        File logfile;
        public void setfile(String filepath) {
            if (filepath==null) return;
            logfile = new File(filepath);
            try {
                if (logfile.exists()) logfile.delete();
                logfile.createNewFile();
            } catch (IOException ex) {
                logfile = null;
            }
        }
        public void a(String text) {
            if (logfile!=null) {
                try {
                    //BufferedWriter for performance, true to set append to file flag
                    BufferedWriter buf = new BufferedWriter(new FileWriter(logfile, true));
                    buf.append(text);
                    buf.newLine();
                    buf.close();
                } catch (Exception ex) {}
            }
            // Always show result on console.
            System.out.println(text);
        }
    }
    
    
    public void setlog(String filepath) {
        if (log!=null) log.setfile(filepath);
    }

    
    
    // Input is a directory containing all the *.mas files
    public File[] getFiles(String relativePath) {
        File files[] = null;
        File dir = new File(relativePath);
        if (dir.isDirectory()) {
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.getName().endsWith(".mas")) return true;
                    return false;
                }
            };
            files = dir.listFiles(filter);
        }
        return files;
    }
    
    public File assemble(File masfile) {
        if (masfile==null || !masfile.exists()) return null;
        String path = masfile.getPath();
        String args[] = {masfile.getPath()};
        Assembler.main(args);
        
        File mexfile = new File( path.replace("mas", "mex"));
        // If program successfully assembled, remove the unnecessary files.
        if (mexfile.exists()) {
            File lstfile = new File( path.replace("mas", "lst"));
            File mapfile = new File( path.replace("mas", "map"));
            if (lstfile.exists()) lstfile.delete();
            if (mapfile.exists()) mapfile.delete();
            log.a("Assembly successful. Mexfile=" + mexfile.getPath());
            return mexfile;
        }
        else {
            log.a("Assembly FAILED. Mexfile=" + mexfile.getPath() + " not generated.");
            // default: not successful.
            return null;
        }
    }
    
    private void startRun() {
        // Reset the counters that are used for every run.
        pollCount = 0;
        inputIndex = 0;
        // Load the program and start running it.
        instance.loadProgram();
        instance.restart();
        instance.runProgram();
    }
    
    private boolean isMarieSimHalted() {
        // If the simulator does not exist, then it cannot be halted.
        if (instance==null) return false; 
        // Check if the simulator is in any of the halted states
        if (instance.machineState==MarieSim.MARIE_HALTED_ABNORMAL) return true;
        if (instance.machineState==MarieSim.MARIE_HALTED_NORMAL) return true;
        if (instance.machineState==MarieSim.MARIE_HALTED_BY_USER) return true;
        return false;
    }
    private boolean isMarieSimRequestingInput() {
        // The simulator makes the input register editable if and only if it needs input.
        return instance.regINPUT.isEditable();
    }
    private void postMarieSimInput(String input) {
        if (isMarieSimRequestingInput()) {
            log.a("Testing input " + input);
            // In case multiple windows are active, 
            // make sure the value is entered to this window 
            instance.regINPUT.requestFocusInWindow();
            instance.regINPUT.setValue(input);
            // In case multiple windows are active, 
            // make sure the robot gets access to this window.
            instance.regINPUT.requestFocusInWindow();
            robot.keyPress(KeyEvent.VK_ENTER);
        }
    }
    private String getMarieSimOutput() {
        // Instance not available. Nothing to verify.
        if (instance==null) return null; 
        // Obtain any output displayed at the output area.
        return instance.outputArea.getText();
    }

    
    // Test one file for a single run.
    private void singleRun(File mexfile, String input, String expectedOutput, IOMode inputMode, IOMode outputMode, int maxPolls) {
        if (mexfile==null || !mexfile.exists()) return;
        if (instance==null || robot==null) return;

        // If multiple inputs are provided within a single runs, the inputs are expected
        // to be delimited by a single whitespace.
        String inputs[];
        if (input==null)
            inputs = null;
        else
            inputs = input.split(" ");
        
        // Setup IO modes.
        instance.regINPUT.setMode(inputMode.getValue());
        instance.regOUTPUT.setMode(outputMode.getValue());
        // Load mexfile. 
        instance.mexFile = mexfile.getPath().replace(".mex", "");

        startRun();        
        
        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            public void run() {
                pollCount++;
                if (pollCount<=maxPolls && !isMarieSimHalted()) {
                    // If tester provided input, then supply it to the simulator upon request.
                    if (inputs!=null && isMarieSimRequestingInput() && inputIndex<inputs.length) {
                        postMarieSimInput(inputs[inputIndex]);
                        inputIndex++;
                    }
                }
                else {
                    // Post-process output of test.
                    verifyOutputs(expectedOutput);
                    log.a("Test completed on file " + mexfile.getName());
                    // Terminate polling.
                    timer.cancel();
                    timer.purge();
                    if (CloseOnCompletion) {
                        System.exit(0);
                    }
                }
            }
        }, 
        0,              // runs first occurrence immediately
        PollInterval);  // run after every specific interval
    }
    
    // Sequentially test one file for multiple runs.
    private void multiRuns(File mexfile, String inputs[], String expectedOutputs[], IOMode inputMode, IOMode outputMode, int maxPollsPerRun) {
        if (mexfile==null || !mexfile.exists()) return;
        if (instance==null || robot==null) return;
        // The length of inputs determines the number of runs.
        if (inputs==null || expectedOutputs==null) return;
        if (inputs.length != expectedOutputs.length) return;

        // Setup IO modes.
        instance.regINPUT.setMode(inputMode.getValue());
        instance.regOUTPUT.setMode(outputMode.getValue());
        // Setup mexfile
        instance.mexFile = mexfile.getPath().replace(".mex", "");

        // initialize run counter
        runCount = 0; 
        
        log.a("Start run " + (runCount+1));
        // Start a new run
        startRun();

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            public void run() {
                if (runCount<inputs.length) {
                    if (isMarieSimHalted()) {
                        // Post-process output of test.
                        verifyOutputs(expectedOutputs[runCount]);
                        log.a("Run completed.");                            

                        // A run has just finished. Increment the counter.
                        runCount++;

                        // If still within limit, start a new run.
                        if (runCount<inputs.length) {
                            log.a("Start run " + (runCount+1));
                            // Start a new run
                            startRun();
                        }
                    }
                    else { // instance still running
                        pollCount++;
                        if (pollCount<=maxPollsPerRun) {
                            // If tester provided input, then supply it to the simulator upon request.
                            if (inputs[runCount]!=null) {
                                String input[] = inputs[runCount].split(" ");
                                if (isMarieSimRequestingInput() && inputIndex<input.length) {
                                    postMarieSimInput(input[inputIndex]);
                                    inputIndex++;
                                }
                            }
                        }
                        else {
                            // Time's up for one run.
                            instance.halt();
                        }
                    }
                }
                else {
                    // Done all runs.
                    log.a("Tests completed on file " + mexfile.getName());
                    
                    // Terminate polling.
                    timer.cancel();
                    timer.purge();
                    if (CloseOnCompletion) {
                        System.exit(0);
                    }
                }
            }
        }, 
        PollInterval*1, // runs first occurrence after some delay (allow time for tester to get focus)
        PollInterval);  // run after every specific interval
    }
    
    
    // Sequentially test multiple files for multiple runs.
    private void multiRuns(File mexfile[], String inputs[], String expectedOutputs[], IOMode inputMode, IOMode outputMode, int maxPollsPerRun) {
        if (mexfile==null || mexfile.length==0) return;
        if (instance==null || robot==null) return;
        // The length of inputs determines the number of runs.
        if (inputs==null || expectedOutputs==null) return;
        if (inputs.length != expectedOutputs.length) return;

        
        // Setup IO modes.
        instance.regINPUT.setMode(inputMode.getValue());
        instance.regOUTPUT.setMode(outputMode.getValue());


        // Initialize the first file to be run.
        fileCount=0;
        // Load mexfile. 
        instance.mexFile = mexfile[fileCount].getPath().replace(".mex", "");
        // Redirect log
        setlog(mexfile[fileCount].getPath().replace(".mex", ".log"));
        log.a("Ready to test " + mexfile[fileCount]);
        runCount=0;
        log.a("Starting run " + (runCount+1) + " with input: " + inputs[runCount]);
        startRun();

        final Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            public void run() {
                if (fileCount<mexfile.length) {
                    if (runCount<inputs.length) {
                        if (pollCount<maxPollsPerRun && !isMarieSimHalted()) {
                            // If tester provided input, then supply it to the simulator upon request.
                            if (inputs[runCount]!=null) {
                                String input[] = inputs[runCount].split(" ");
                                if (isMarieSimRequestingInput() && inputIndex<input.length) {
                                    postMarieSimInput(input[inputIndex]);
                                    inputIndex++;
                                }
                            }
                            pollCount++;
                        }
                        else { // Finished all polls on one file
                            // Time's up for one run.
                            instance.halt();
                            
                            // Post-process output of test.
                            verifyOutputs(expectedOutputs[runCount]);
                            log.a("Run completed.");                            
                                    
                            runCount++;
                            if (runCount<inputs.length) {
                                log.a("Starting run " + (runCount+1) + " with input: " + inputs[runCount]);
                                startRun();
                            }
                        }
                    }
                    else { // Finished all runs on one file.
                        fileCount++;
                        // Load mexfile. 
                        
                        if (fileCount<mexfile.length) {
                            instance.mexFile = mexfile[fileCount].getPath().replace(".mex", "");
                            // Redirect log
                            setlog(mexfile[fileCount].getPath().replace(".mex", ".log"));
                            log.a("Ready to test " + mexfile[fileCount]);

                            // Reset run counter 
                            runCount=0;
                            log.a("Starting run " + (runCount+1) + " with input: " + inputs[runCount]);
                            startRun();
                        }
                    }
                }
                else {
                    // Done testing all files
                    // Terminate polling.
                    timer.cancel();
                    timer.purge();
                    if (CloseOnCompletion) {
                        System.exit(0);
                    }
                }
            }
        }, 
        PollInterval*1, // runs first occurrence after some delay (allow time for tester to get focus)
        PollInterval);  // run after every specific interval
    }
    
    
    
    private void verifyOutputs(String expectedOutput) {
        
        // Copy out any existing output from the simulator.
        String testOutput = getMarieSimOutput();
        
        // Tester should determine how to check output here.
        if (expectedOutput==null) expectedOutput = "";  // default

        String testOutputCleaned = ""; // default
        if (testOutput!=null) {
            // For example, if whitespaces don't matter, then replace all multiple spaces by a single space
            testOutputCleaned = testOutput.replaceAll("\\s+", " ").trim();
        }

        log.a("Cleaned test output: " + testOutputCleaned);
        expectedOutput = expectedOutput.trim();
        log.a("Expected output:     " + expectedOutput);
        if (testOutputCleaned.equals(expectedOutput)) {
            log.a("Test output seems correct");
        }
        else {
            log.a("Test output seems INCORRECT");
        }
    }
    
    
    
    private void singleRun(String masfilepath, String input, String expectedOutput, int maxPollCount, IOMode inputMode, IOMode outputMode) {
        if (masfilepath==null) return;
        File masfile = new File(masfilepath);
        setlog(masfile.getPath().replace(".mas", ".log"));

        File mexfile = assemble(masfile);
        if (mexfile!=null) {
            singleRun(mexfile, input, expectedOutput, inputMode, outputMode, maxPollCount);
        }
        else {
            System.exit(0);
        }
    }
    
    private void multiRuns(File masfile, String inputs[], String expectedOutputs[], int maxPollCount, IOMode inputMode, IOMode outputMode) {
        setlog(masfile.getPath().replace(".mas", ".log"));
        File mexfile = assemble(masfile);
        if (mexfile!=null) {
            multiRuns(mexfile, inputs, expectedOutputs, inputMode, outputMode, maxPollCount);
        }
        else {
            System.exit(0);
        }
    }
    
    private void multiRuns(File masfiles[], String inputs[], String expectedOutputs[], int maxPollCount, IOMode inputMode, IOMode outputMode) {
        // Assemble all in batch.
        ArrayList<File> mexlist = new ArrayList();
        for (File masfile : masfiles) {
            File mexfile = assemble(masfile);
            if (mexfile!=null) {
                mexlist.add(mexfile);
            }
        }
        // Consolidate mex files.
        File[] mexfiles = new File[mexlist.size()];
        for (int index=0; index<mexlist.size(); index++)
            mexfiles[index] = mexlist.get(index);

        // Run tests for all
        multiRuns(mexfiles, inputs, expectedOutputs, inputMode, outputMode, maxPollCount);
    }

    private void multiRuns(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount, IOMode inputMode, IOMode outputMode) {
        if (masfilepath==null) return;
        File masfile = new File(masfilepath);
        if (!masfile.exists()) {
            log.a("File " + masfilepath + " not found.");
            return;
        }
        
        // Determine whether to run in batch or not.
        if (masfile.isDirectory()) {
            log.a("Preparing to test all mas files in directory " + masfile.getName() + ":");
            // If a directory is provided, test all mas files inside it.
            multiRuns(getFiles(masfilepath), inputs, expectedOutputs, maxPollCount, inputMode, outputMode);
        }
        else { // one test file
            multiRuns(masfile, inputs, expectedOutputs, maxPollCount, inputMode, outputMode);
        }
    }

    //////////////////////////////////////////////////////
    // Public Methods 
    //////////////////////////////////////////////////////
    public void singleRunDec2Dec(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.dec, IOMode.dec);
    }
    public void singleRunDec2Hex(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.dec, IOMode.hex);
    }
    public void singleRunDec2Ascii(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.dec, IOMode.ascii);
    }
    public void singleRunHex2Dec(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.hex, IOMode.dec);
    }
    public void singleRunHex2Hex(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.hex, IOMode.hex);
    }
    public void singleRunHex2Ascii(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.hex, IOMode.ascii);
    }
    public void singleRunAscii2Dec(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.ascii, IOMode.dec);
    }
    public void singleRunAscii2Hex(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.ascii, IOMode.hex);
    }
    public void singleRunAscii2Ascii(String masfilepath, String input, String expectedOutput, int maxPollCount) {
        singleRun(masfilepath, input, expectedOutput, maxPollCount, IOMode.ascii, IOMode.ascii);
    }

    
    
    // Provide a single file to test one file with multiple runs.
    // If a directory is provided, test all mas files in the directory with multiple runs.
    public void multiRunsDec2Dec(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.dec, IOMode.dec);
    }
    public void multiRunsDec2Hex(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.dec, IOMode.hex);
    }
    public void multiRunsDec2Ascii(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.dec, IOMode.ascii);
    }
    public void multiRunsHex2Dec(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.hex, IOMode.dec);
    }
    public void multiRunsHex2Hex(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.hex, IOMode.hex);
    }
    public void multiRunsHex2Ascii(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.hex, IOMode.ascii);
    }
    public void multiRunsAscii2Dec(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.ascii, IOMode.dec);
    }
    public void multiRunsAscii2Hex(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.ascii, IOMode.hex);
    }
    public void multiRunsAscii2Ascii(String masfilepath, String inputs[], String expectedOutputs[], int maxPollCount) {
        multiRuns(masfilepath, inputs, expectedOutputs, maxPollCount, IOMode.ascii, IOMode.ascii);
    }
}
