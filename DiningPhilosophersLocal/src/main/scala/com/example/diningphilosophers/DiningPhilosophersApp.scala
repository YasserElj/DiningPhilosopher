package com.example.diningphilosophers

import akka.actor.{ActorRef, ActorSystem, Props}
import com.example.diningphilosophers.Messages._ // For logging colors & StartAttemptingForks

import scala.collection.mutable.ArrayBuffer

object DiningPhilosophersApp extends App {
  val forceDeadlockBehavior = false // SET TO true TO MAKE ALL PHILOSOPHERS PICK L->R

  val system = ActorSystem("DiningPhilosophersLocalSystem")

  val numPhilosophersAndForks = 5 // change it to whatever you want

  println(s"${ANSI_GREEN}Dining Philosophers Local Simulation Starting...${ANSI_RESET}")

  // Create Fork Actors
  // Each fork actor runs independently, processing messages concurrently.
  val forks = ArrayBuffer[ActorRef]()
  for (i <- 0 until numPhilosophersAndForks) {
    val fork = system.actorOf(Fork.props(i), name = s"Fork$i")
    forks += fork
    println(s"${ANSI_CYAN}Created Fork$i${ANSI_RESET}")
  }

  // Create Philosopher Actors
  // Each philosopher actor also runs independently, interacting with forks via asynchronous messages.
  val philosophers = ArrayBuffer[ActorRef]()
  for (i <- 0 until numPhilosophersAndForks) {
    val leftFork = forks(i)
    val rightFork = forks((i + 1) % numPhilosophersAndForks)
    
    val picksLeftFirst = if (forceDeadlockBehavior) {
      true // All pick L->R if forcing deadlock
    } else {
      // Last philosopher picks R->L to normally avoid deadlock
      if (i == numPhilosophersAndForks - 1) false else true
    }

    val philosopher = system.actorOf(Philosopher.props(i, leftFork, rightFork, picksLeftFirst, forceDeadlockBehavior), name = s"Philosopher$i")
    philosophers += philosopher
    val order = if (picksLeftFirst) "L->R" else "R->L"
    val mode = if (forceDeadlockBehavior) "[Deadlockable]" else "[Retry]"
    println(s"${ANSI_CYAN}Created Philosopher$i with L: ${leftFork.path.name}, R: ${rightFork.path.name}, Order: $order, Mode: $mode${ANSI_RESET}")
  }

  println(s"${ANSI_GREEN}All $numPhilosophersAndForks philosophers and forks created.${ANSI_RESET}")
  
  Thread.sleep(100) // Brief pause for actor readiness

  // Start simulation
  philosophers.foreach(p => p ! StartAttemptingForks)

  // Simulation runs
  println(s"${ANSI_YELLOW}Simulation will run. Philosophers terminate after eating once.${ANSI_RESET}")

  println(s"${ANSI_YELLOW}Press ENTER to shutdown the system...${ANSI_RESET}")

  // Let the system run until Enter is pressed or all philosophers terminate (though system termination is manual here)
  
  scala.io.StdIn.readLine()
  system.terminate()
  println(s"${ANSI_RED}Dining Philosophers Local Simulation Shutdown.${ANSI_RESET}")
} 