# Dining Philosophers Problem in Akka - Assignment Implementation

This project implements the classic Dining Philosophers problem using Scala and Akka Actors as required by the assignment. The implementation demonstrates concurrent programming concepts, deadlock prevention, and actor-based communication.

## Assignment Requirements Fulfilled

✅ **Core Requirements:**
- At least 5 philosophers and 5 forks implemented
- Each philosopher is an individual Actor
- Each fork is an individual Actor
- Deadlock prevention mechanism implemented
- Parametric version supporting any number of philosophers

✅ **Bonus Requirements:**
- ✅ Parametric version (configurable number of philosophers)
- ❌ Remote deployment across multiple JVMs (not implemented)

## Project Structure

```
DiningPhilosophersLocal/
├── src/main/scala/com/example/diningphilosophers/
│   ├── DiningPhilosophersApp.scala    # Main application and simulation setup
│   ├── Philosopher.scala              # Philosopher actor implementation
│   ├── Fork.scala                     # Fork actor implementation
│   └── Messages.scala                 # Message protocol definitions
├── src/main/resources/
│   └── logback.xml                    # Logging configuration
├── project/
│   └── build.properties               # SBT version
└── build.sbt                         # Project dependencies and configuration
```

## 1. How Deadlocks Are Prevented

The implementation uses **multiple complementary strategies** to prevent deadlocks:

### Primary Strategy: Asymmetric Resource Ordering
```scala
val picksLeftFirst = if (forceDeadlockBehavior) {
  true // All pick L->R if forcing deadlock
} else {
  // Last philosopher picks R->L to normally avoid deadlock
  if (i == numPhilosophersAndForks - 1) false else true
}
```

- **Philosophers 0 to N-2**: Pick left fork first, then right fork (L→R)
- **Philosopher N-1 (last)**: Pick right fork first, then left fork (R→L)
- **Result**: Breaks the circular wait condition that causes deadlock

### Secondary Strategy: Timeout and Retry Mechanism
```scala
case Messages.Busy(fork) if fork == rightFork =>
  if (deadlockableMode) {
    become(deadlockedBehavior, InternalState.Deadlocked) // Demonstrate deadlock
  } else {
    // Drop held fork and retry after timeout
    timers.startSingleTimer(ForkBusyRetryTimerKey, TimeoutDropHeldForkAndRetryMsg, fixedForkBusyTimeout)
  }
```

- When a fork is busy, philosophers **drop their held fork** and retry later
- Prevents indefinite waiting and resource hoarding
- Uses configurable timeout periods (`fixedForkBusyTimeout = 2.seconds`)

### Deadlock Detection Mode
The system includes a `forceDeadlockBehavior` flag to demonstrate deadlock scenarios:
- **When `true`**: All philosophers use L→R strategy → Deadlock occurs
- **When `false`**: Asymmetric strategy → Deadlock prevented

## 2. How to Run the Program

### Prerequisites
- Scala 2.13.12
- SBT (Scala Build Tool)
- Java 8 or higher

### Step-by-Step Execution

1. **Navigate to project directory:**
   ```bash
   cd DiningPhilosophersLocal
   ```

2. **Compile the project:**
   ```bash
   sbt compile
   ```

3. **Run the simulation (Normal Mode - No Deadlock):**
   ```bash
   sbt "runMain com.example.diningphilosophers.DiningPhilosophersApp"
   ```

4. **To test deadlock scenario** (Optional):
   - Edit `DiningPhilosophersApp.scala`
   - Change `val forceDeadlockBehavior = false` to `true`
   - Run again: `sbt "runMain com.example.diningphilosophers.DiningPhilosophersApp"`

### Configuration Options

**Parametric Version**: You can modify the number of philosophers in `DiningPhilosophersApp.scala`:

```scala
val numPhilosophersAndForks = 5 // Change to any number 
val forceDeadlockBehavior = false // true = demonstrate deadlock, false = prevent deadlock
```

## 3. Expected Outcome

### Normal Mode (`forceDeadlockBehavior = false`)

**Expected Behavior:**
1. **System Initialization**: All 5 philosophers and 5 forks are created
2. **Fork Acquisition**: Philosophers successfully acquire forks using asymmetric strategy
3. **Eating Phase**: All philosophers eat exactly once
4. **Termination**: Philosophers release forks and terminate after eating
5. **Clean Shutdown**: System waits for user input before shutting down

**Console Output Sample:**
```
[Green] Dining Philosophers Local Simulation Starting...
[Cyan] Created Fork0
[Cyan] Created Fork1
...
[Cyan] Created Philosopher0 with L: Fork0, R: Fork1, Order: L->R, Mode: [Retry]
[Cyan] Created Philosopher4 with L: Fork4, R: Fork0, Order: R->L, Mode: [Retry]
...
[Yellow] P0 (L->R) attempts Left Fork (Fork0).
[Yellow] P0 (L->R) took Left (Fork0). Attempts Right (Fork1).
[Green] P0 took both forks. Eating.
[Blue] P0 Done Eating. Releasing forks: Fork0, Fork1.
[Red] P0 has eaten and will now be terminated.
...
Press ENTER to shutdown the system...
```

**Color-Coded Output Legend:**
- 🟢 **Green**: Successful operations and system messages
- 🟡 **Yellow**: Fork acquisition attempts
- 🔵 **Blue**: Eating completion
- 🔴 **Red**: Termination messages
- 🟣 **Purple**: Fork busy notifications
- 🔷 **Cyan**: System setup

### Deadlock Mode (`forceDeadlockBehavior = true`)

**Expected Behavior:**
1. All philosophers acquire their left fork
2. All philosophers attempt their right fork (all busy)
3. **Deadlock State**: System becomes unresponsive
4. Manual termination required (Ctrl+C or Enter)

**Console Output Sample:**
```
[Red] P0 (L->R) [DEADLOCK MODE]: Right Fork (Fork1) busy. Holding Left (Fork0). WILL DEADLOCK.
[Red] P1 (L->R) [DEADLOCK MODE]: Right Fork (Fork2) busy. Holding Left (Fork1). WILL DEADLOCK.
...
[Red] P0 is DEADLOCKED. Ignoring message: ...
```


## Actor Implementation Details

### Philosopher Actor States
```scala
object InternalState {
  val WaitingForStartSignal = "WaitingForStartSignal"
  val Thinking = "Thinking"
  val AttemptingLeftFork = "AttemptingLeftFork"
  val AttemptingRightForkHoldingLeft = "AttemptingRightForkHoldingLeft"
  val AttemptingRightFork = "AttemptingRightFork"
  val AttemptingLeftForkHoldingRight = "AttemptingLeftForkHoldingRight"
  val Eating = "Eating"
  val Deadlocked = "Deadlocked"
}
```

### Fork Actor States
```scala
sealed trait ForkState
case object Available extends ForkState
case class TakenBy(philosopher: ActorRef) extends ForkState
```

### Message Protocol
```scala
case class Take(philosopher: ActorRef)      // Philosopher -> Fork
case class PutDown(philosopher: ActorRef)   // Philosopher -> Fork
case class Taken(fork: ActorRef)           // Fork -> Philosopher
case class Busy(fork: ActorRef)            // Fork -> Philosopher
case object StartAttemptingForks           // App -> Philosopher
```

## Bonus Implementation: Parametric Version

The implementation supports any number of philosophers (tested from 3 to 10):

```scala
val numPhilosophersAndForks = 5 // Configurable parameter

// Dynamic actor creation
for (i <- 0 until numPhilosophersAndForks) {
  val leftFork = forks(i)
  val rightFork = forks((i + 1) % numPhilosophersAndForks) // Circular arrangement
  
  val picksLeftFirst = if (i == numPhilosophersAndForks - 1) false else true
  // Last philosopher always picks right first for deadlock prevention
}
```

**Asymmetric Strategy Scales:**
- For N philosophers: Philosophers 0 to N-2 use L→R, Philosopher N-1 uses R→L
- Maintains deadlock prevention regardless of table size

## Dependencies

```scala
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % "2.8.5",
  "com.typesafe.akka" %% "akka-actor" % "2.8.5",        // Classic actors
  "com.typesafe.akka" %% "akka-slf4j" % "2.8.5",
  "ch.qos.logback" % "logback-classic" % "1.4.14"
)
```

## Assignment Compliance Summary

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| 5+ Philosophers | ✅ | Configurable (default 5, supports 3-10+) |
| 5+ Forks | ✅ | Matches philosopher count |
| Each Philosopher = Actor | ✅ | `Philosopher` extends `Actor` |
| Each Fork = Actor | ✅ | `Fork` extends `Actor` |
| Deadlock Prevention | ✅ | Asymmetric strategy + timeout/retry |
| Parametric Version | ✅ | `numPhilosophersAndForks` parameter |
| Remote Deployment | ❌ | Not implemented |

This implementation successfully demonstrates the Dining Philosophers problem using Akka actors with robust deadlock prevention mechanisms and supports the core assignment requirements plus the parametric bonus feature. 