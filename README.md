# Dining Philosophers Implementations

This repository contains two implementations of the Dining Philosophers problem, demonstrating different approaches to concurrent programming and deadlock prevention.

## Projects

### [Akka Implementation](./DiningPhilosophersLocal)
- Uses Scala and Akka Actors
- Supports parametric number of philosophers
- Includes deadlock prevention through asymmetric strategy
- Features colorful console output for visualization

**Key Features:**
- Actor-based concurrency
- Runtime deadlock prevention
- Configurable number of philosophers
- Timeout and retry mechanism

### [P Language Implementation](./P-Verify)
- Uses P language for formal verification
- Includes two variants (deadlock-prone and deadlock-free)
- Features formal specification for deadlock detection
- Supports automated verification

**Key Features:**
- Formal verification
- Automated deadlock detection
- State machine modeling
- Specification-based testing

## Requirements

### Akka Implementation
- Scala 2.13.12
- SBT (Scala Build Tool)
- Java 8 or higher

### P Implementation
- P language compiler and runtime
- Graphviz (optional, for visualization)

## Quick Start

### Run Akka Version
```bash
cd DiningPhilosophersLocal
sbt "runMain com.example.diningphilosophers.DiningPhilosophersApp"
```

### Run P Version
```bash
cd P-Verify
p compile
p check -tc tcDeadlock -ms 1000 -s 5    # Test deadlock scenario
p check -tc tcNoDeadlock -ms 1000 -s 5  # Test deadlock-free scenario
```

For detailed instructions and documentation, please refer to each project's individual README. 