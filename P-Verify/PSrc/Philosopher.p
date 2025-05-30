// Event declarations
event eAcquireFork: (philosopher: machine, philo_id: int);
event eReleaseFork: (philosopher: machine, philo_id: int); // philo id
event eForkAcquired: (philosopher: machine, philo_id: int);
event eBothForksAcquired;
event eForkBusy: (fork: machine, philo_id: int);


machine Philosopher {
    var left_fork: machine;
    var right_fork: machine;
    var philosopher_id: int;
    var forks_acquired: int;
    var left_first: bool;
    
    start state Init {
      
        entry (input: (id: int, left: machine, right: machine, left_first: bool)) {
	            philosopher_id = input.id;
            left_fork = input.left;
            right_fork = input.right;
            forks_acquired = 0;
            left_first = input.left_first;
            // Start thinking
        goto thinking;
        }

        
    }

    state thinking {
       
           entry  {
             print format("P{0} Thinking", philosopher_id);

            if (left_first) {
                send left_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
            } else {
                send right_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
            }
            // goto thinking;
           
        }

        on eForkAcquired do (info: (philosopher: machine, philo_id: int)) {
            forks_acquired = forks_acquired + 1;
            if (forks_acquired == 1) {
                if (left_first) {
                    print format("P{0} took left fork", philosopher_id);
                    // Request right fork
                    send right_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                } else {
                    print format("P{0} took right fork", philosopher_id);
                    // Request left fork
                    send left_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                }
                // goto thinking;
            } else if (forks_acquired == 2) {
                print format("P{0} took both forks", philosopher_id);
                goto eating;
            }
        }


        on eForkBusy do (info: (fork: machine, philo_id: int)) {       
            print format("P{0} fork busy, retrying...", philosopher_id);  
            // goto thinking;          
            // Retry acquiring fork based on left_first preference
            if (forks_acquired == 1) {
                if (left_first) {
                    print format("P{0} took left fork", philosopher_id);
                    // Request right fork
                    send right_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                } else {
                    print format("P{0} took right fork", philosopher_id);
                    // Request left fork
                    send left_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                }
                // goto thinking;
            } else if (forks_acquired == 0) {
                if (left_first) {
                    print format("P{0} took left fork", philosopher_id);
                    send left_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                } else {
                    print format("P{0} took right fork", philosopher_id);
                    send right_fork, eAcquireFork, (philosopher = this, philo_id = philosopher_id);
                }
            }
        }
    }

    state eating {
        entry {
            print format("P{0} Eating", philosopher_id);
            send left_fork, eReleaseFork, (philosopher = this, philo_id = philosopher_id);
            send right_fork, eReleaseFork, (philosopher = this, philo_id = philosopher_id);
            print format("P{0} Done Eating", philosopher_id);
            

            goto done;
        }
    }

    state done {
        entry {
            print format("P{0} killed", philosopher_id);
        }
    }
}