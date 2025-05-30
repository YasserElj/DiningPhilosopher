# Dining Philosophers Problem in P Language

This project implements the classic Dining Philosophers problem using the P language to demonstrate deadlock detection and verification.

## Problem Description

The Dining Philosophers problem consists of:
- 5 philosophers sitting around a circular table
- 5 forks placed between each philosopher
- Each philosopher needs 2 forks to eat
- Philosophers can either think or eat
- A philosopher must acquire both forks before eating
- After eating, philosophers release both forks

## Project Structure

- `PSrc/` - Contains the source P files
  - `Philosopher.p` - Implements philosopher behavior
  - `Fork.p` - Implements fork behavior 
  - `Simulation1.p` & `Simulation2.p` - Different simulation configurations
  - `Modules.p` - Module declarations
- `PSpec/` - Contains specifications
  - `Specification.p` - Deadlock detection specification
- `PTst/` - Test files
- `PGenerated/` - Generated output files

## Simulation Differences

The project includes two simulation approaches:

### Simulation1
- All philosophers pick up their left fork first
- This can lead to deadlock as each philosopher holds their left fork and waits for right
- Used to demonstrate deadlock detection

### Simulation2 
- All philosophers except the last one pick up left fork first
- The last philosopher picks up right fork first
- This breaks the circular wait condition and prevents deadlock
- Used to demonstrate deadlock-free implementation

## Running the Project

1. **Compile the project:**
   ```bash
   p compile
   ```

2. **Check for deadlock scenarios (should detect deadlock):**
   ```bash
   p check -tc tcDeadlock
   ```

3. **Check deadlock-free scenarios (should pass without deadlock):**
   ```bash
   p check -tc tcNoDeadlock
   ```

## Expected Results

- **tcDeadlock**: Should report a deadlock scenario when all philosophers pick up left fork first
- **tcNoDeadlock**: Should complete successfully without deadlock when using the asymmetric approach

## Requirements

- P language compiler and runtime
- Make sure all source files are in their respective directories as shown in the project structure