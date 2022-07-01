# Oerating System Concepts with Android - Assignment Description

## Assignment 1
[cskang.2020.asgn1.c](https://github.com/cskang0121/operating-system-concepts-with-android/blob/main/assignment_1/cskang.2020.asgn1.c)

This project focuses on the concept of process management using C Programming Language, which is able to run on terminal. Some features and arguments of this application are as follows : 

- ```run [priority] [program] [arguments]```: Running an executable program with optional input arguments
- ```stop [PID]```: Put a process with the specified PID in the stopped state. If a running process is stopped, then dispatch highest priority ready process to run state
- ```kill [PID]```: Terminate a process with the specified PID. If a running process is killed, then dispatch highest priority ready process to run state
- ```resume [PID]```: Resume the stopped process with the specified PID to ready state. If the number of running processes is going to exceed three, then only the top three priority processes are allowed in run state
- ```list``` : List all the processes by: PID, state, priority. Use the following mapping to represent the states – 0: running, 1: ready, 2: stopped, 3: terminated
- ```exit``` : Terminate all child processes if they have not yet been terminated, and exit from parent process

## Assignment 2
[assignment2](https://github.com/cskang0121/operating-system-concepts-with-android/tree/main/assignment_2)

This project focuses on the concept of concurrency, multi-threaded programming and other OS concepts such as synchronization, mutual exclusion through Java concurrency programming.

## Assignment 3
[assignment3](https://github.com/cskang0121/operating-system-concepts-with-android/tree/main/assignment_3)

This project focuses on Android development environment. Various Android components including Activities, Services,
Content Providers, Broadcast Receivers and Intents were used to develop the software.
