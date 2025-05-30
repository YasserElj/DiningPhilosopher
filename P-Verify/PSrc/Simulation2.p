machine Simulation2 {
    var philosophers: seq[machine];
    var forks: seq[machine];
    var numPhilosophers: int;
    var leftFork: machine;
    var rightFork: machine;
    var left_first: bool;
    
    start state Init {
        entry {
            var i: int;
            var j: int;
            numPhilosophers = 5;
            left_first = true;

            // Create 5 forks
            j = 0;
            while (j < numPhilosophers) {
                forks += (0, new Fork());
                j = j + 1;
            }
            
            // Create 5 philosophers
            i = 0;
            while (i < numPhilosophers) {
                leftFork = forks[i];
                rightFork = forks[(i + 1) % numPhilosophers];
                if (i == numPhilosophers - 1) {
                    left_first = false;
                }
                philosophers += (0, new Philosopher((id=i, left=leftFork, right=rightFork, left_first=left_first)));
                i = i + 1;
            }
    
        }
    }
}

test tcNoDeadlock [main=Simulation2]: assert DeadlockDetection in (union Simulation2, Philosopher, Fork);