#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <stdlib.h>
#include <stdbool.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <sys/resource.h>
#include <readline/history.h>
#include <readline/readline.h>


#define MAX_SIZE 16
#define MAX_ARGS 5
#define SUPPORTED_PRG "./prog"


struct Process {
    pid_t pid;
    int state;
};


// Keep all the 16 processes, with each process's index defined by it's priority (0 - 15)
struct Process processes[MAX_SIZE];
int n_terminated = 0;
int status;


// ------------------------------------------------------------------------
// HELPER METHODS
// ------------------------------------------------------------------------


// Initialise all processes' pid to -1 and processes' state to -1
void init(struct Process processes[]) {
    
    // All valid Process ID's are guaranteed to be positive
    // Reference: https://en.wikipedia.org/wiki/Process_identifier
    for(int i = 0; i < MAX_SIZE; i++) {
        processes[i].pid = -1;
        processes[i].state = -1;
    }
}


// Check if a given pid is valid
// Return -1 if the given pid does not exist 
// Return the index of the process in processes array if the given pid is valid
int is_pid_valid(struct Process processes[], pid_t pid) {
    
    for(int i = 0; i < MAX_SIZE; i++) {
        if(processes[i].pid == pid) {
            return i;
        }
    }

    return -1;
}


// Return the total number of running processes  
int get_num_running(struct Process processes[]) {
    int count = 0;

    for(int i = 0; i < MAX_SIZE; i++) {
        if(processes[i].state == 0) {
            count++;
        }
    }

    return count; 
}


// Return the index of the process with highest priority if there exists >= 1 processes in ready state
// Return -1 if no process is in ready state
int get_highest_prio_ready(struct Process processes[]) {

    for(int i = 0; i < MAX_SIZE; i++) {
        if(processes[i].state == 1) {
            return i;
        }
    }
    
    return -1;
}


// Return the index of the process with lowest priority if there exists >= 1 processes in running state
// Return -1 if no process is in running state
int get_lowest_prio_running(struct Process processes[]) {

    for(int i = MAX_SIZE - 1; i >= 0 ; i--) {
        if(processes[i].state == 0) {
            return i;
        }
    }
    
    return -1;
}


// Receive a signal if a child process is terminated and update the child processes accordingly
void signal_handler() {

    for (int i = 0; i < MAX_SIZE; i++){
        if (processes[i].state == 0)  {
            if (waitpid(processes[i].pid, &status, WNOHANG) == processes[i].pid){
                // Updates the state of the child process - running to terminated
                processes[i].state = 3;

                // Dispatch highest priority ready process to running state
                int pos = get_highest_prio_ready(processes);

                if(pos != -1) {
                    processes[pos].state = 0;
                    kill(processes[pos].pid, SIGCONT);
                }

                n_terminated++;
            }
        }
    }
}


// ------------------------------------------------------------------------
// SHELL
// ------------------------------------------------------------------------
int main() {

    init(processes);

    char *args[MAX_ARGS];

    int n_process = 0;
    bool active = true;

    // Override default behavior and listen to SIGCHLD signal to call signal_handler
    signal(SIGCHLD, signal_handler);


    while(active) {

        // Input from user
        char *input = NULL;
        input = readline("cs205> ");
        char *p = strtok(input, " ");
        char *cmd = p;
        int arg_cnt = 0;

        if(cmd == NULL) {
            printf("cs205> Error: No command is entered\n");
            continue;
        }


        //args : [priority | ./prog | <filename> | <arg1> | <arg2>]
        //            0         1          2         3        4
        while (p = strtok(NULL, " ")) {             
            args[arg_cnt] = p;                      
            arg_cnt++;
        }

        
        // RUN
        if (strcmp(cmd, "run") == 0) {
            
            // Handle possible error conditions
            bool is_valid_input = true;

            if(n_process >= MAX_SIZE) {
                printf("cs205> Error: Exceeded maximum number of processes (16 processes)\n");
                is_valid_input = false;
            }

            if(atoi(args[0]) < 0 || atoi(args[0]) > MAX_SIZE - 1) {
                printf("cs205> Error: The priority's value must be between 0 and 15 (both inclusive)\n");
                is_valid_input = false;
            }
            
            if(!(atoi(args[0]) < 0 || atoi(args[0]) > MAX_SIZE - 1)
                && processes[atoi(args[0])].pid != -1) {
                printf("cs205> Error: No two processes can have same priority\n");
                is_valid_input = false;
            }

            if(arg_cnt != 5) {
                printf("cs205> Error: Incorrect number of arguments\n");
                is_valid_input = false;
            }

            if(strcmp(args[0],args[arg_cnt-1]) != 0) {
                printf("cs205> Error: Two different priority values entered\n");
                is_valid_input = false;
            }

            if(strcmp(args[1], SUPPORTED_PRG) != 0) {
                printf("cs205> Error: The program entered is not supported\n");
                is_valid_input = false;
            }
            
            if(!is_valid_input) continue;

            n_process++;
            pid_t pid = fork();

            // Child process
            if (pid == 0) {
                char *arg_array[] = {NULL, NULL, NULL, NULL, NULL};

                for (int i = 1; i < arg_cnt; i++) {  //arg_array: [./prog | <filename>.txt | <arg1> | <arg2>]
                    arg_array[i - 1] = args[i];
                }

                execvp(arg_array[0], arg_array);
            } 
            
            // Parent process
            else if (pid > 0) {
                processes[atoi(args[0])].pid = pid;
                processes[atoi(args[0])].state = 0;

                // Dispatch the process into ready state if there exist more than 3 processes in running state
                if(get_num_running(processes) > 3) {
                    
                    int ready_pos = get_lowest_prio_running(processes);
                    
                    kill(processes[ready_pos].pid, SIGSTOP);
                    processes[ready_pos].state = 1;
                }
            }
        } 



        // STOP
        else if (strcmp(cmd, "stop") == 0) {

            // Handle possible error conditions
            int stop_pos = is_pid_valid(processes, atoi(args[0]));

            if(arg_cnt == 0 || stop_pos == -1) {
                printf("cs205> Error: Please provide a valid process identifier\n");
                continue;
            }

            // Stop a process in running state
            if (processes[stop_pos].state == 0) {

                kill(processes[stop_pos].pid, SIGSTOP);
                processes[stop_pos].state = 2;

                int ready_pos = get_highest_prio_ready(processes);

                if(ready_pos != -1) {
                    kill(processes[ready_pos].pid, SIGCONT);
                    processes[ready_pos].state = 0;
                    printf("cs205> stopping %d\n", processes[stop_pos].pid);
                }
            }

            // Stop a process in ready state
            else if(processes[stop_pos].state == 1) {
                kill(processes[stop_pos].pid, SIGSTOP);
                processes[stop_pos].state = 2;
                printf("cs205> stopping %d\n", processes[stop_pos].pid);
            }

            // Stop a process in stop state
            else if(processes[stop_pos].state == 2) {
                printf("cs205> %d is already stopped \n", processes[stop_pos].pid);
            }

            // Stop a process in terminated state
            else if(processes[stop_pos].state == 3) {
                printf("cs205> %d is already terminated \n", processes[stop_pos].pid);
            }
        }



        // KILL
        else if (strcmp(cmd, "kill") == 0) {

            // Handle possible error conditions
            int kill_pos = is_pid_valid(processes, atoi(args[0]));

            if(arg_cnt == 0 || kill_pos == -1 ) {
                printf("cs205> Error: Please provide a valid process identifier\n");
                continue;
            }

            // Kill a process in running state
            if (processes[kill_pos].state == 0) {

                kill(processes[kill_pos].pid, SIGTERM);
                processes[kill_pos].state = 3;

                int ready_pos = get_highest_prio_ready(processes);

                if(ready_pos != -1) {
                    kill(processes[ready_pos].pid, SIGCONT);
                    processes[ready_pos].state = 0;
                }

                printf("cs205> killing %d\n", processes[kill_pos].pid);
            }

            // Kill a process in ready or stop state
            else if(processes[kill_pos].state == 1 || processes[kill_pos].state == 2) {
                kill(processes[kill_pos].pid, SIGTERM);
                processes[kill_pos].state = 3;

                printf("cs205> killing %d\n", processes[kill_pos].pid);
            }

            else if(processes[kill_pos].state == 3) {
                printf("cs205> %d is already terminated \n", processes[kill_pos].pid);
            }
        }



        // RESUME
        else if (strcmp(cmd, "resume") == 0) {

            // Handle possible error conditions
            int res_pos = is_pid_valid(processes, atoi(args[0]));

            bool is_valid_input = true;

            if(arg_cnt == 0 || res_pos == -1) {
                printf("cs205> Error: Please provide a valid process identifier\n");
                is_valid_input = false;
            }

            if(processes[res_pos].state == 3) { 
                printf("cs205> Error: Process is already terminated and cannot be resumed\n");
                is_valid_input = false;
            }

            if(!is_valid_input) continue;

            // Resume a process in stop state
            if (processes[res_pos].state == 2) {
                processes[res_pos].state = 1;

                // Dispatch the process (from ready) into running state if number of processes in running state < 3
                if(get_num_running(processes) < 3) {
                    int res_pos = get_highest_prio_ready(processes);

                    if(res_pos != -1) {
                        processes[res_pos].state = 0;
                        kill(processes[res_pos].pid, SIGCONT);
                    }

                } else {
                    int ready_pos = get_highest_prio_ready(processes);
                    int running_pos = get_lowest_prio_running(processes);

                    // Dispatch the process (from ready) into running state and 
                    // Dispatch a process (lowest priority in running state) into ready state
                    if(ready_pos < running_pos) {
                        processes[running_pos].state = 1;
                        kill(processes[running_pos].pid, SIGSTOP);

                        processes[ready_pos].state = 0;
                        kill(processes[ready_pos].pid, SIGCONT);
                    }
                    
                }

                printf("cs205> resuming %d\n", processes[res_pos].pid);
            }
            
            // Resume a process in running state
            else if(processes[res_pos].state == 0) {
                printf("cs205> %d is already in running state \n", processes[res_pos].pid);
            }

            // Resume a process in ready state
            else if(processes[res_pos].state == 1) {
                printf("cs205> %d is already in ready state \n", processes[res_pos].pid);
            }
        }



        // LIST
		else if (strcmp(cmd, "list") == 0) {
            
            if(n_process == 0) {
                printf("cs205> No process currently\n");
            }

            // 0: running, 1: ready, 2: stopped, 3: terminated
			for (int i = 0; i < MAX_SIZE; i++){
                if(processes[i].pid != -1)
				printf("%d, %d, %d\n", processes[i].pid,  processes[i].state, i);
			}
        }



        // EXIT
		else if (strcmp(cmd, "exit")==0){

            // Terminate all processes before exit
			for(int i = 0; i < MAX_SIZE; i++) {
				if(processes[i].state != -1 && processes[i].state != 3) {
					kill(processes[i].pid, SIGTERM);
					processes[i].state = 3;
				}
			}

            printf("Program terminated!\n");
			break;
		}



        // INVALID COMMANDs
        else {
            printf("Error: Invalid command\n");
        }
    }
}