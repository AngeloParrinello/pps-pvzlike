import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.must.Matchers.must
import akka.actor.ActorSystem
import akka.actor.testkit.typed.Effect
import akka.testkit.{ImplicitSender, TestActors, TestKit}
import akka.actor.testkit.typed.scaladsl.{ActorTestKit, BehaviorTestKit, ScalaTestWithActorTestKit, TestInbox}
import akka.actor.typed.scaladsl.Behaviors
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AnyWordSpecLike}
import model.entities.{Bullet, Enemy, Plant, Seed, Turret, Zombie}
import model.common.DefaultValues.*
import model.actors.{BulletActor, TurretActor, TurretMessages, Update, Shoot}
import controller.GameLoop.GameLoopCommands.{EntitySpawned, EntityUpdate, GameLoopCommand}
import controller.Command
import concurrent.duration.DurationInt

import scala.concurrent.duration.FiniteDuration

class TurretTest extends AnyWordSpec with BeforeAndAfterAll with Matchers:

  val plant: Turret = Plant(50, 1)
  val testZombie1: Enemy = Zombie()
  val turretActor: BehaviorTestKit[TurretMessages] = BehaviorTestKit(TurretActor(plant))
  val inbox = TestInbox[Command]()

  "The Turret Actor" when {
    "created" should {
      "be alive" in {
        turretActor.isAlive must be(true)
      }

      "shoot zombie" in {
        testZombie1.position = (200, 1)
        turretActor run Update(10, List(testZombie1), inbox.ref)
        turretActor expectEffect Effect.TimerScheduled("TurretShooting", Shoot(inbox.ref), plant.fireRate.seconds, Effect.TimerScheduled.SingleMode, false)(null)
      }
    }
  }
