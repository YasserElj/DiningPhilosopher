package com.example.diningphilosophers

import akka.actor.{Actor, ActorRef, Props, Timers}
import com.example.diningphilosophers.Messages._

import scala.concurrent.duration._
// import scala.util.Random // No longer needed for random durations

object Philosopher {
  // Add deadlockableMode flag to props
  def props(philosopherId: Int, leftFork: ActorRef, rightFork: ActorRef, picksLeftForkFirst: Boolean, deadlockableMode: Boolean): Props = {
    Props(new Philosopher(philosopherId, leftFork, rightFork, picksLeftForkFirst, deadlockableMode))
  }

  // Internal states of the Philosopher
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

  // Timer keys for Akka Timers
  private case object EatTimerKey
  private case object ForkBusyRetryTimerKey

  // Internal messages for self-scheduling actions
  private case object AttemptToAcquireForksMsg
  private case object TimeoutDropHeldForkAndRetryMsg
}

class Philosopher(philosopherId: Int, leftFork: ActorRef, rightFork: ActorRef, picksLeftForkFirst: Boolean, deadlockableMode: Boolean)
    extends Actor
    with Timers {

  import Philosopher._
  import context.dispatcher

  var forksHeld: Set[ActorRef] = Set.empty
  private var currentStateForLogging: String = InternalState.WaitingForStartSignal

  // Durations for thinking/eating/timeouts
  private val fixedThinkRetryDuration = 2.seconds
  private val fixedEatDuration = 2.seconds
  private val fixedForkBusyTimeout = 2.seconds

  override def preStart(): Unit = {
    val order = if (picksLeftForkFirst) "L->R" else "R->L"
    val mode = if (deadlockableMode) "[Deadlockable]" else "[Retry]"
    println(s"${ANSI_CYAN}P$philosopherId ($self) L:${leftFork.path.name}, R:${rightFork.path.name}, Order: $order $mode WAITING FOR START.$ANSI_RESET")
    become(waitingForStartSignalBehavior, InternalState.WaitingForStartSignal)
  }

  // Helper to change behavior and log current state
  private def become(behavior: Receive, stateName: String): Unit = {
    currentStateForLogging = stateName
    context.become(behavior)
  }

  def receive: Receive = waitingForStartSignalBehavior // Initial behavior: wait for start signal

  // Behavior: Waiting for the simulation to start
  val waitingForStartSignalBehavior: Receive = {
    case StartAttemptingForks =>
      println(s"${ANSI_GREEN}P$philosopherId received START SIGNAL. Will attempt forks.$ANSI_RESET")
      self ! AttemptToAcquireForksMsg // Transition to thinking/attempting forks
      become(thinkingBehavior, InternalState.Thinking)
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }

  // Behavior: Thinking or preparing to attempt fork acquisition
  val thinkingBehavior: Receive = {
    case AttemptToAcquireForksMsg =>
      if (picksLeftForkFirst) {
        println(s"${ANSI_YELLOW}P$philosopherId (L->R) attempts Left Fork (${leftFork.path.name}).$ANSI_RESET")
        leftFork ! Messages.Take(self)
        become(attemptingLeftForkFirstBehavior, InternalState.AttemptingLeftFork)
      } else {
        println(s"${ANSI_YELLOW}P$philosopherId (R->L) attempts Right Fork (${rightFork.path.name}).$ANSI_RESET")
        rightFork ! Messages.Take(self)
        become(attemptingRightForkFirstBehavior, InternalState.AttemptingRightFork)
      }
  }

  // --- Behaviors for Left -> Right picker ---
  // Behavior: Attempting to take the left fork first
  val attemptingLeftForkFirstBehavior: Receive = {
    case Messages.Taken(fork) if fork == leftFork =>
      forksHeld += leftFork
      println(s"${ANSI_YELLOW}P$philosopherId (L->R) took Left (${leftFork.path.name}). Attempts Right (${rightFork.path.name}).$ANSI_RESET")
      rightFork ! Messages.Take(self)
      become(attemptingRightForkHoldingLeftBehavior, InternalState.AttemptingRightForkHoldingLeft)
    case Messages.Busy(fork) if fork == leftFork =>
      println(s"${ANSI_RED}P$philosopherId (L->R): Left Fork (${leftFork.path.name}) busy. Waiting $fixedForkBusyTimeout.$ANSI_RESET")
      timers.startSingleTimer(ForkBusyRetryTimerKey, AttemptToAcquireForksMsg, fixedForkBusyTimeout) // Retry after timeout
      become(thinkingBehavior, InternalState.Thinking)
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }

  // Behavior: Holding left fork, attempting to take the right fork
  val attemptingRightForkHoldingLeftBehavior: Receive = {
    case Messages.Taken(fork) if fork == rightFork =>
      forksHeld += rightFork
      startEating()
    case Messages.Busy(fork) if fork == rightFork =>
      if (deadlockableMode && picksLeftForkFirst) {
        println(s"${ANSI_RED}P$philosopherId (L->R) [DEADLOCK MODE]: Right Fork (${rightFork.path.name}) busy. Holding Left (${leftFork.path.name}). WILL DEADLOCK.$ANSI_RESET")
        become(deadlockedBehavior, InternalState.Deadlocked) // Deadlock: hold fork and do nothing
      } else {
        println(s"${ANSI_RED}P$philosopherId (L->R) [RETRY MODE]: Right Fork (${rightFork.path.name}) busy. Holding Left (${leftFork.path.name}). Will drop Left and retry after $fixedForkBusyTimeout.$ANSI_RESET")
        timers.startSingleTimer(ForkBusyRetryTimerKey, TimeoutDropHeldForkAndRetryMsg, fixedForkBusyTimeout) // Schedule fork drop & retry
      }
    case TimeoutDropHeldForkAndRetryMsg => // Received if not in deadlockableMode
      println(s"${ANSI_RED}P$philosopherId (L->R): Timeout. Dropping Left Fork (${leftFork.path.name}). Retrying.$ANSI_RESET")
      leftFork ! Messages.PutDown(self)
      forksHeld = Set.empty
      timers.startSingleTimer(ForkBusyRetryTimerKey, AttemptToAcquireForksMsg, fixedThinkRetryDuration)
      become(thinkingBehavior, InternalState.Thinking)
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }

  // --- Behaviors for Right -> Left picker ---
  // Behavior: Attempting to take the right fork first
  val attemptingRightForkFirstBehavior: Receive = {
    case Messages.Taken(fork) if fork == rightFork =>
      forksHeld += rightFork
      println(s"${ANSI_YELLOW}P$philosopherId (R->L) took Right (${rightFork.path.name}). Attempts Left (${leftFork.path.name}).$ANSI_RESET")
      leftFork ! Messages.Take(self)
      become(attemptingLeftForkHoldingRightBehavior, InternalState.AttemptingLeftForkHoldingRight)
    case Messages.Busy(fork) if fork == rightFork =>
      println(s"${ANSI_RED}P$philosopherId (R->L): Right Fork (${rightFork.path.name}) busy. Waiting $fixedForkBusyTimeout.$ANSI_RESET")
      timers.startSingleTimer(ForkBusyRetryTimerKey, AttemptToAcquireForksMsg, fixedForkBusyTimeout)
      become(thinkingBehavior, InternalState.Thinking)
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }

  // Behavior: Holding right fork, attempting to take the left fork
  val attemptingLeftForkHoldingRightBehavior: Receive = {
    case Messages.Taken(fork) if fork == leftFork =>
      forksHeld += leftFork
      startEating()
    case Messages.Busy(fork) if fork == leftFork =>
       if (deadlockableMode && !picksLeftForkFirst) { // This is the R->L picker in deadlockable mode
        println(s"${ANSI_RED}P$philosopherId (R->L) [DEADLOCK MODE]: Left Fork (${leftFork.path.name}) busy. Holding Right (${rightFork.path.name}). WILL DEADLOCK.$ANSI_RESET")
        become(deadlockedBehavior, InternalState.Deadlocked)
      } else {
        println(s"${ANSI_RED}P$philosopherId (R->L) [RETRY MODE]: Left Fork (${leftFork.path.name}) busy. Holding Right (${rightFork.path.name}). Will drop Right and retry after $fixedForkBusyTimeout.$ANSI_RESET")
        timers.startSingleTimer(ForkBusyRetryTimerKey, TimeoutDropHeldForkAndRetryMsg, fixedForkBusyTimeout)
      }
    case TimeoutDropHeldForkAndRetryMsg =>
      println(s"${ANSI_RED}P$philosopherId (R->L): Timeout. Dropping Right Fork (${rightFork.path.name}). Retrying.$ANSI_RESET")
      rightFork ! Messages.PutDown(self)
      forksHeld = Set.empty
      timers.startSingleTimer(ForkBusyRetryTimerKey, AttemptToAcquireForksMsg, fixedThinkRetryDuration)
      become(thinkingBehavior, InternalState.Thinking)
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }
  
  // --- Common Eating Behavior ---
  private def startEating(): Unit = {
    println(s"${ANSI_GREEN}P$philosopherId took both forks. Eating.$ANSI_RESET")
    timers.startSingleTimer(EatTimerKey, DoneEating, fixedEatDuration) // Schedule end of eating
    become(eatingBehavior, InternalState.Eating)
  }

  // Behavior: Currently eating
  val eatingBehavior: Receive = {
    case DoneEating =>
      println(s"${ANSI_BLUE}P$philosopherId Done Eating. Releasing forks: ${forksHeld.map(_.path.name).mkString(", ")}.$ANSI_RESET")
      forksHeld.foreach(f => f ! Messages.PutDown(self))
      forksHeld = Set.empty
      println(s"${ANSI_RED}P$philosopherId has eaten and will now be terminated.$ANSI_RESET")
      context.stop(self) // Philosopher terminates after eating once
    case Messages.Take(_) => println(s"${ANSI_RED}P$philosopherId received Take request while eating. Ignored.$ANSI_RESET")
    case other => println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $other from ${sender()}")
  }

  // Behavior: Deadlocked (holding one fork, unable to acquire the other in deadlockableMode)
  val deadlockedBehavior: Receive = {
    case msg => 
      println(s"${ANSI_RED}P$philosopherId is DEADLOCKED. Ignoring message: $msg$ANSI_RESET")
  }

  override def unhandled(message: Any): Unit = {
    println(s"P$philosopherId (State: $currentStateForLogging) received unhandled: $message from ${sender()}")
  }
} 