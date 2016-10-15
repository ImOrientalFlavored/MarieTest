# MarieTest
Automated IO tests for the MARIE simulator

### Purpose
The project provides an IO based test environment for conducting automated program tests on the Marie Simulator.

### Target users
CSC205 students at NVCC Manassas, and instructors of computer organization.

### Approach
The Marie Simulator is built on Java Swing. The main module MarieSim uses two Swing objects (the input textfield and the output textarea) for IO. This project automates the user interaction with MarieSim via a robot to facilitate IO testing on Marie assembly programs.

### Features
The test module [Test.java](src/MarieSimTester/Test.java) provides tests in the following modes:
* The *Single-run* mode tests a single (.mas) file with a single run of inputs
* The *Multi-run* mode tests a single (.mas) file for multiple runs, with distinct inputs for each run
* The *Batch Multi-run* mode tests all (.mas) files in a specified directory, each file for multiple runs with distinct inputs

### Usage
This project may be downloaded and compiled from command line. Instructions are available at [howto.txt](src/howto.txt).

### Examples
* [TestWarmup.java](src/Examples/TestWarmup.java): This test module verifies the results of a multiplication of two positive integers. A sample source file to be tested may be found at [warmup.mas](testbed/warmup.mas)
* [TestArray.java](src/Examples/TestArray.java): This test module verifies the results of a sequential access of an array to print both the array index and the array content. A sample source file to be tested may be found at [array.mas](testbed/array.mas)
* [TestParity.java](src/Examples/TestParity.java): This test module verifies the correctness of a parity check implemented in Marie. A default (incomplete) example of one such parity checker may be found at [parity-brute.mas](testbed/parity-brute.mas)
* More examples are available at the [Examples](src/Examples) folder.

### References
The Marie Simulator is an educational tool designed and built by Null and Lobur for the instruction of computer architecture. The terms of use of the Marie Simulator software may be found at the authors' web site. 

