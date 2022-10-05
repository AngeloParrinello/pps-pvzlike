package model.actors

import akka.actor.ActorSystem
import akka.actor.testkit.typed.Effect
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed.scaladsl.Behaviors
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import controller.Command
import controller.GameLoopActor.GameLoopCommands.{EntityUpdate, GameLoopCommand, EntitySpawned}
import model.actors.{BulletActor, Shoot, TurretActor, Update}
import model.common.DefaultValues.*
import model.entities.*
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.must.Matchers.must
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import WorldSpace.LanesLength

class TurretActorTest extends AnyWordSpec with BeforeAndAfterAll with Matchers:

  val plant: Turret = Plant(1, LanesLength / 2)
  val turretActor: BehaviorTestKit[ModelMessage] = BehaviorTestKit(TurretActor(plant))
  val inbox = TestInbox[Command]()

  "The Turret Actor" when {
    "created" should {
      "be alive" in {
        turretActor.isAlive must be(true)
      }

      "shoot zombie" in {
        turretActor run Update(10, List(Zombie(1, LanesLength)), inbox.ref)
        turretActor expectEffect Effect.TimerScheduled("TurretShooting", Shoot(inbox.ref), plant.fireRate.seconds, Effect.TimerScheduled.SingleMode, false)(null)
      }

      "spawn a bullet" in {
        turretActor run Shoot(inbox.ref)
        assert(inbox.hasMessages)
        val message = inbox.receiveMessage()
        assert(message.isInstanceOf[EntitySpawned[Bullet]])
      }

      /*"should lose hp" in {
        turretActor run Collision(testZombie1)
        turretActor.isAlive must be(false)
      }*/
    }
  }
