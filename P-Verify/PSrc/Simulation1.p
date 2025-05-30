machine Simulation1 {
    var philosophers: seq[machine];
    var forks: seq[machine];
    var numPhilosophers: int;
    var leftFork: machine;
    var rightFork: machine;

    start state Init {
        entry {
            var i: int;
            var j: int;
            numPhilosophers = 5;


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
                philosophers += (0, new Philosopher((id=i, left=leftFork, right=rightFork, left_first=true)));
                i = i + 1;
            }
        }
    }
}

test tcDeadlock [main=Simulation1]: assert DeadlockDetection in (union Simulation1, Philosopher, Fork);