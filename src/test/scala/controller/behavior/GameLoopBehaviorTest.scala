package controller.behavior

import akka.actor.testkit.typed.Effect
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, TestInbox}
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import controller.GameLoopActor.*
import controller.GameLoopActor.GameLoopCommands.*
import controller.{Command, GameLoopActor, ViewActor, ViewMessage}
import model.actors.{EnemyActor, ModelMessage}
import model.entities.WorldSpace.{LanesLength, given}
import model.entities.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldNot
import org.scalatest.wordspec.AnyWordSpec
import view.Game

import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

class GameLoopBehaviorTest extends AnyWordSpec with Matchers :

  val viewActor: TestInbox[ViewMessage] = TestInbox[ViewMessage]()
  val gameLoopActor: BehaviorTestKit[Command] = BehaviorTestKit(controller.GameLoopActor.GameLoopActor(viewActor.ref).standardBehavior)
  val updateTime: FiniteDuration = FiniteDuration(16, "milliseconds")

  "The GameLoop Actor" when {
    "created" should {
      "be alive" in {
        gameLoopActor.isAlive must be(true)
      }

      "create a wave" in {
        gameLoopActor run StartLoop()
        val startTimerEffect = gameLoopActor.retrieveEffect()
        val startWaveEffect = gameLoopActor.retrieveEffect()
        startTimerEffect should not be Effect.NoEffects
        startWaveEffect should not be Effect.NoEffects
        startTimerEffect should not be startWaveEffect
      }

      "start the timer" in {
        gameLoopActor run StartLoop()
        gameLoopActor expectEffect Effect.TimerScheduled(UpdateLoop(), UpdateLoop(), updateTime, Effect.TimerScheduled.SingleMode, false)(null)
      }

      "resume the loop" in {
        gameLoopActor run PauseLoop()
        val prevBehavior = gameLoopActor.currentBehavior
        gameLoopActor run ResumeLoop()
        val postBehavior = gameLoopActor.currentBehavior
        prevBehavior should not be postBehavior
      }

      "stop the loop" in {
        gameLoopActor run StopLoop()
        gameLoopActor.isAlive must be(false)
      }
    }
  }




