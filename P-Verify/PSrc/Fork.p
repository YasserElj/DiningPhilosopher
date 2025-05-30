machine Fork {
    var holder: machine;
    var holder_id: int;

    start state available {
        entry {
            holder = null;
            holder_id = -1;
        }
        on eAcquireFork do (info: (philosopher: machine, philo_id: int)) {
            holder = info.philosopher;
            holder_id = info.philo_id;
            send info.philosopher, eForkAcquired, (philosopher = info.philosopher, philo_id = info.philo_id);
            goto taken;
        }

        on eReleaseFork do (info: (philosopher: machine, philo_id: int)) {
            // Fork is available, ignore the release request
            print format("Fork is already available, cannot release by {0}", info.philo_id);

        }
    }

    state taken {

        on eReleaseFork do (info: (philosopher: machine, philo_id: int)) {
            assert info.philo_id == holder_id, format("Only holder can release fork");
            holder = null;
            holder_id = -1;
            goto available;
        }

        on eAcquireFork do (info: (philosopher: machine, philo_id: int)) {
        
            // Send a delayed retry message to the philosopher
            send info.philosopher, eForkBusy, (fork = info.philosopher, philo_id = info.philo_id);
        }
    }
}