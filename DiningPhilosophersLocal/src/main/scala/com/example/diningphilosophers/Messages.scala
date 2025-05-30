package com.example.diningphilosophers

import akka.actor.ActorRef // Using classic ActorRef

object Messages {

  // ANSI Color Codes for console logging
  val ANSI_RESET = "\u001B[0m"
  val ANSI_BLACK = "\u001B[30m"
  val ANSI_RED = "\u001B[31m"
  val ANSI_GREEN = "\u001B[32m"
  val ANSI_YELLOW = "\u001B[33m"
  val ANSI_BLUE = "\u001B[34m"
  val ANSI_PURPLE = "\u001B[35m"
  val ANSI_CYAN = "\u001B[36m"
  val ANSI_WHITE = "\u001B[37m"

  // --- Messages between Philosopher and Fork ---

  /** Philosopher -> Fork: Request to take the fork. */
  case class Take(philosopher: ActorRef)

  /** Philosopher -> Fork: Release the fork. */
  case class PutDown(philosopher: ActorRef)

  /** Fork -> Philosopher: Confirmation that fork has been taken. */
  case class Taken(fork: ActorRef)

  /** Fork -> Philosopher: Indication that fork is busy. */
  case class Busy(fork: ActorRef)

  // --- Philosopher Internal Lifecycle Messages ---

  /** Timer message: Philosopher has finished eating. */
  case object DoneEating

  // --- Simulation Control Messages ---
  /** DiningPhilosophersApp -> Philosopher: Start the simulation for this philosopher. */
  case object StartAttemptingForks

} 