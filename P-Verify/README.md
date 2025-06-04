# Dining Philosophers Problem in P Language - Assignment Implementation

This project implements the Dining Philosophers problem using P language to demonstrate formal verification and deadlock detection. The implementation includes two variants as required by the assignment and provides formal specifications to verify their behavior.

## Assignment Requirements Fulfilled

✅ **Core Requirements:**
- Two variants of Dining Philosophers implemented
  1. All philosophers take left fork first (deadlock-prone)
  2. One philosopher takes forks in reverse order (deadlock-free)
- Formal specification for deadlock detection
- Verification of both solutions

✅ **Bonus Features:**
- ✅ Implementation matches Akka solution structure
- ✅ Parametric version (create any )

## Project Structure

```
P-Verify/
├── PSrc/                           # Source P files
│   ├── Philosopher.p              # Philosopher behavior
│   ├── Fork.p                     # Fork behavior
│   ├── Simulation1.p             # Deadlock-prone implementation
│   ├── Simulation2.p             # Deadlock-free implementation
│   └── Modules.p                 # Module declarations
├── PSpec/
│   └── Specification.p           # Deadlock detection specification
├── PTst/                         # Test files
└── PGenerated/                   # Generated output files
```

## Implementation Details

### Variant 1: Deadlock-Prone Solution (`Simulation1.p`)
```p
// All philosophers follow same order: Left -> Right
machine Philosopher {
    var leftFirst = true;  // Always true in Simulation1
    // ... implementation details ...
}
```
- All philosophers attempt to acquire left fork first
- Without releasing left fork, they attempt to acquire right fork
- **Expected Result**: Deadlock when all philosophers hold their left forks

### Variant 2: Deadlock-Free Solution (`Simulation2.p`)
```p
// Last philosopher takes right fork first
machine Philosopher {
    var leftFirst = (id != NUM_PHILOSOPHERS - 1);
    // ... implementation details ...
}
```
- Philosophers 0-(N-1): Left fork first
- Philosopher N: Right fork first
- **Result**: Breaks circular wait condition, prevents deadlock

## 1. Running the P Checker

### Prerequisites
- P language compiler and runtime installed
- All source files in their respective directories

### Command Line Instructions

1. **Compile the Project:**
   ```bash
   p compile
   ```

2. **Verify Deadlock-Prone Solution (Simulation1):**
   ```bash
   p check -tc tcDeadlock
   ```
   This will:
   - Run the deadlock detection specification
   - Find and report the deadlock scenario
   - Generate a trace showing the path to deadlock

3. **Verify Deadlock-Free Solution (Simulation2):**
   ```bash
   p check -tc tcNoDeadlock
   ```
   This will:
   - Run the same specification
   - Verify absence of deadlock
   - Complete successfully

## 2. Expected Outcomes

### Deadlock Detection (Simulation1)

When running `p check -tc tcDeadlock -ms 1000 -s 5`:

```
.. Searching for a P compiled file locally in folder ./PGenerated/
.. Found a P compiled file: ./PGenerated/CSharp/net8.0/DiningPhilosophers.dll
.. Checking ./PGenerated/CSharp/net8.0/DiningPhilosophers.dll
.. Test case :: tcDeadlock
... Checker is using 'random' strategy (seed:2377540071).
..... Schedule #1
..... Schedule #2
Checker found a bug.
... Emitting traces:
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers_0_0.txt
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers_0_0.trace.json
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers_0_0.schedule
... Elapsed 0.31 sec and used 0 GB.
... Emitting coverage report:
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers.coverage.txt
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers.sci
... Checking statistics:
..... Found 1 bug.
... Scheduling statistics:
..... Explored 2 schedules
..... Explored 2 timelines
..... Found 50.00% buggy schedules.
..... Number of scheduling points in terminating schedules: 39 (min), 82 (avg), 126 (max).
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers_pchecker_summary.txt
... Elapsed 0.4043146 sec.
. Done
~~ [PTool]: Thanks for using P! ~~
```

### Deadlock-Free Verification (Simulation2)

When running `p check -tc tcNoDeadlock -ms 1000 -s 5`:

```
.. Searching for a P compiled file locally in folder ./PGenerated/
.. Found a P compiled file: ./PGenerated/CSharp/net8.0/DiningPhilosophers.dll
.. Checking ./PGenerated/CSharp/net8.0/DiningPhilosophers.dll
.. Test case :: tcNoDeadlock
... Checker is using 'random' strategy (seed:3884404200).
..... Schedule #1
... Emitting coverage report:
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers.coverage.txt
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers.sci
... Checking statistics:
..... Found 0 bugs.
... Scheduling statistics:
..... Explored 1 schedule
..... Explored 1 timeline
..... Number of scheduling points in terminating schedules: 146 (min), 146 (avg), 146 (max).
..... Writing PCheckerOutput/BugFinding/DiningPhilosophers_pchecker_summary.txt
... Elapsed 0.2996118 sec.
. Done
~~ [PTool]: Thanks for using P! ~~
```



## Specification Implementation

The deadlock detection specification (`PSpec/Specification.p`) monitors the system:

```p
spec DeadlockDetection observes eForkAcquired, eReleaseFork {
    var philosophers: set[machine];
    
    on eForkAcquired do (payload: (philosopher: machine)) {
        philosophers += (payload.philosopher);
        assert sizeof(philosophers) < N, "Hard deadlock: All philosophers holding forks";
    }
    
    on eReleaseFork do (payload: (philosopher: machine)) {
        philosophers -= (payload.philosopher);
    }
}
```


## Relationship to Akka Implementation

The P implementation mirrors the Akka solution's structure:

| Akka Component | P Equivalent |
|----------------|--------------|
| `Philosopher` Actor | `Philosopher` Machine |
| `Fork` Actor | `Fork` Machine |
| Message Protocol | Events (`eForkAcquired`, etc.) |
| State Management | P State Machines |
| Deadlock Prevention | Same asymmetric strategy |

## Requirements

- P language compiler and runtime
- Graphviz (optional, for visualization)
- Make sure all source files are in their respective directories

