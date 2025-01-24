### README

**Project Title:** CS4348 Project 1 - Logger, Encryption, and Driver System

**Author:** Solomon Gheevarghese

**Date:** [October 11th, 2024]

---

### Files Included:
1. Logger.java - Responsible for logging activity to the log file. It will accept log messages, adds a timestamp and action, and puts them in the log file.
2. encrypt.java - This program handles encryption and decryption using a Vigenère cipher. It accepts commands like "PASSKEY", "ENCRYPT", "DECRYPT", and "QUIT".
3. driver.java - The main program that has user interaction and does the communication between the Logger and encryption code using processes and pipes.

---

### How to Compile:

- javac Logger.java encrypt.java driver.java
- java driver.java log.txt

When this commands are ran it will create class files
In the command line, navigate to the folder containing the source files and use the following commands:
- javac Logger.java encrypt.java driver.java
Then in this line run the interface
-java driver.java log.txt
