package com.example.diningphilosophers

import akka.actor.{Actor, ActorRef, Props}
import com.example.diningphilosophers.Messages._ // Import message objects and color codes

object Fork {
  // Props for creating a Fork actor
  def props(forkId: Int): Props = Props(new Fork(forkId))

  // States of the Fork
  sealed trait ForkState
  case object Available extends ForkState // Fork is available
  case class TakenBy(philosopher: ActorRef) extends ForkState // Fork is taken by a philosopher
}

class Fork(forkId: Int) extends Actor {
  import Fork._

  private var currentState: ForkState = Available // Fork starts as Available

  override def preStart(): Unit = {
    println(s"${ANSI_CYAN}Fork $forkId created and is Available.$ANSI_RESET")
  }

  def receive: Receive = {
    case Take(philosopher) => // Request from a philosopher to take this fork
      currentState match {
        case Available =>
          println(s"${ANSI_YELLOW}Fork $forkId taken by ${philosopher.path.name}.$ANSI_RESET")
          currentState = TakenBy(philosopher)
          sender() ! Messages.Taken(self) // Confirm fork was taken
        case TakenBy(currentOwner) =>
          println(s"${ANSI_PURPLE}Fork $forkId is already taken by ${currentOwner.path.name}. ${philosopher.path.name} cannot take it.$ANSI_RESET")
          sender() ! Messages.Busy(self) // Inform requester that fork is busy
      }

    case PutDown(philosopher) => // Notification from a philosopher that they are done with the fork
      currentState match {
        case TakenBy(currentOwner) if currentOwner == philosopher =>
          println(s"${ANSI_GREEN}Fork $forkId put down by ${philosopher.path.name} and is now Available.$ANSI_RESET")
          currentState = Available
        case TakenBy(currentOwner) =>
          // This case should ideally not happen if philosophers behave correctly
          println(s"${ANSI_RED}Fork $forkId cannot be put down by ${philosopher.path.name}, it is held by ${currentOwner.path.name}.$ANSI_RESET")
        case Available =>
          // This case should also ideally not happen
          println(s"${ANSI_RED}Fork $forkId is already Available, cannot be put down by ${philosopher.path.name}.$ANSI_RESET")
      }
  }
} 