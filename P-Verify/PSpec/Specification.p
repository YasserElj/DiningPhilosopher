spec DeadlockDetection observes eForkAcquired, eReleaseFork {
    var philosophers: set[machine];
    var releasecounter: int;
    
    start state Monitoring {
        entry { 
            philosophers = default(set[machine]);
        }
        
        on eForkAcquired do (payload: (philosopher: machine, philo_id: int)){
            releasecounter = releasecounter + 1;
            philosophers += (payload.philosopher);
            print format("philosophers: {0}", philosophers);
            print format("Philosopher {0} acquired fork", payload.philo_id);
            print format("size: {0}", sizeof(philosophers));
            assert sizeof(philosophers) < 5, "Hard deadlock: All philosophers holding forks";

        }
        
        on eReleaseFork do (payload: (philosopher: machine, philo_id: int)) {
            philosophers -= (payload.philosopher);
        }
    }
}