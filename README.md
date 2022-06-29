# CS205_operating_system_concepts_with_android

## Assignment 1
[cskang.2020.asgn1.c](https://github.com/cskang0121/cs205-operating-system-concepts-with-android/blob/main/assignment_1/cskang.2020.asgn1.c)

This project focuses on the concept of process management using C Programming Language, which is able to run on terminal. Some features and arguments of this application are as follows : 

- ```run [priority] [program] [arguments]```: Running an executable program with optional input arguments
- ```stop [PID]```: Put a process with the specified PID in the stopped state. If a running process is stopped, then dispatch highest priority ready process to run state
- ```kill [PID]```: Terminate a process with the specified PID. If a running process is killed, then dispatch highest priority ready process to run state
- ```resume [PID]```: Resume the stopped process with the specified PID to ready state. If the number of running processes is going to exceed three, then only the top three priority processes are allowed in run state
- ```list``` : List all the processes by: PID, state, priority. Use the following mapping to represent the states – 0: running, 1: ready, 2: stopped, 3: terminated
- ```exit``` : Terminate all child processes if they have not yet been terminated, and exit from parent process

## Assignment 2
[KANG CHIN SHEN_CS205_assignment2](https://github.com/cskang0121/cs205-operating-system-concepts-with-android/tree/main/assignment_2)

This project focuses on the concept of concurrency, multi-threaded programming and other OS concepts such as synchronization, mutual exclusion through Java concurrency programming.

The main thread should takes in parameters N, M, K, W, X, Y, Z where N is the number of hotdogs to make, M is the number of burgers to make, K is the number of slots in the common pool, W is the number of hotdog makers, X is the number of burger makers, Y is the number of hotdog packers, Z is the number of burger packers. Constraints: 1 < N, M < 1000, 1 < K < 100, 1 < W, X, Y, Z < 50. Also, N is an even number.


## Assignment 3
[KANG CHIN SHEN_CS205_assignment3](https://github.com/cskang0121/cs205-operating-system-concepts-with-android/tree/main/assignment_3)

This project focuses on Android development environment. Various Android components including Activities, Services,
Content Providers, Broadcast Receivers and Intents were used to develop the software.

> Problem statement : As a stock investor, you are interested to know how your stock portfolio would perform given different compositions, and would like to build an app for that. Specifically you can only hold up to 5 stocks but potentially different number of shares for each stock. On the main screen you can provide up to 5 tickers and the number of shares you want to own respectively. The app then downloads historical data from a data provider. Your app then calculates the performance of such a portfolio in terms of annualized return and volatility.

#### Resources used : 
- [finnhub](https://finnhub.io/)

#### Configuration for the virtual device :
- Device: Nexus 6P
- API: Level 30
- Resolution 1440 x 2560: 560 dpi
- Multi-Core CPU 4
- RAM 1536 MB
- SD card 512 MB
