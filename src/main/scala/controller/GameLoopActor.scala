package controller

import akka.actor.typed.*
import akka.actor.typed.scaladsl.*
import akka.actor.typed.scaladsl.adapter.*
import controller.GameLoopActor.GameLoopCommands.{StartLoop, UpdateLoop}
import model.actors.*
import model.entities.*
import model.{Generator, WaveGenerator}

import scala.collection.immutable.Seq
import scala.collection.mutable
import scala.concurrent.duration.FiniteDuration
import scala.language.postfixOps

object GameLoopActor:

  val waveGenerator: WaveGenerator = Generator()

  import GameLoopCommands.*
  val updateTime: FiniteDuration = FiniteDuration(16, "milliseconds")

  case class GameLoopActor(viewActor: ActorRef[ViewMessage],
                           entities: Seq[(ActorRef[ModelMessage], Entity)] = List.empty,
                           metaData: MetaData = MetaData()) extends Controller with PauseAbility:

    override def standardBehavior: Behavior[Command] =
      Behaviors.withTimers(timer =>
        Behaviors.receive((ctx, msg) => {
          msg match
            case StartLoop() =>
              startLoopTimer(timer)
              ctx.self ! StartResourcesLoop()
              GameLoopActor(viewActor, createWave(ctx)).standardBehavior

            case StartResourcesLoop() =>
              startResourcesTimer(timer)
              GameLoopActor(viewActor, entities).standardBehavior

            case StopLoop() => Behaviors.stopped

            case PauseLoop() => GameLoopActor(viewActor, entities, metaData).pauseBehavior

            case UpdateResources() =>
              startResourcesTimer(timer)
              GameLoopActor(viewActor, entities, updateResources).standardBehavior

            case UpdateLoop() =>
              detectCollision foreach { e => e._1._1 ! Collision(e._2._2, ctx.self); e._2._1 ! Collision(e._1._2, ctx.self) }
              updateAll(ctx, detectInterest)
              val newWave: List[(ActorRef[ModelMessage], Entity)] = if isWaveOver then createWave(ctx) else List.empty
              startLoopTimer(timer)
              GameLoopActor(viewActor, newWave ++ entities, metaData).standardBehavior

            case EntityUpdated(ref, entity) =>
              val newEntities = entities collect { case x if x._1 == ref => (ref, entity) case x => x}
              render(ctx, newEntities.map(_._2).toList)
              GameLoopActor(viewActor, newEntities, metaData).standardBehavior

            case EntitySpawned(ref, entity) =>
              GameLoopActor(viewActor, entities :+ (ref, entity), metaData).standardBehavior

            case EntityDead(ref) =>
              GameLoopActor(viewActor, entities filter { e => e._1 != ref }, metaData).standardBehavior

            case _ => Behaviors.same
        }))

    override def pauseBehavior: Behavior[Command] =
      Behaviors.receive((ctx, msg) => {
        msg match
          case StopLoop() => Behaviors.stopped

          case ResumeLoop() =>
            ctx.self ! UpdateLoop()
            GameLoopActor(viewActor, entities, metaData).standardBehavior

          case _ => Behaviors.same
      })

    private def startLoopTimer(timer: TimerScheduler[Command]): Unit = timer.startSingleTimer(UpdateLoop(), updateTime)

    private def startResourcesTimer(timer: TimerScheduler[Command]): Unit = timer.startSingleTimer(UpdateResources(), metaData.velocity.speed)

    private def updateResources: MetaData = MetaData(metaData.sun + Sun(), metaData.availableEntities, metaData.velocity)

    private def createWave(ctx: ActorContext[Command]) =
      waveGenerator.generateNextWave.enemies.map(e => (ctx.spawnAnonymous(EnemyActor(e)), e))

    private def detectCollision =
      for
        e1 <- entities
        e2 <- entities
        if e1 != e2
        if e1._2.isInstanceOf[Bullet]
        if e1._2.asInstanceOf[Bullet] checkCollisionWith e2._2
      yield (e1, e2)

    private def detectInterest =
      for
        e1 <- entities
      yield
        (e1._1, for
          e2 <- entities
          if e1 != e2
          if e1._2 isInterestedIn e2._2
        yield e2._2)

    private def isWaveOver: Boolean = entities map (_._2) collect { case enemy: Enemy => enemy } isEmpty

    private def updateAll(ctx: ActorContext[Command], interests: Seq[(ActorRef[ModelMessage], Seq[Entity])]): Unit =
      interests.foreach(e => e._1 ! Update(updateTime, e._2.toList, ctx.self))

    private def render(ctx: ActorContext[Command], renderedEntities: List[Entity]): Unit = viewActor ! Render(renderedEntities, ctx.self, metaData)

  object GameLoopCommands:
    sealed trait GameLoopCommand extends Command

    case class StartLoop() extends GameLoopCommand

    case class StartResourcesLoop() extends GameLoopCommand

    case class StopLoop() extends GameLoopCommand

    case class PauseLoop() extends GameLoopCommand

    case class ResumeLoop() extends GameLoopCommand

    case class UpdateLoop() extends GameLoopCommand

    case class UpdateResources() extends GameLoopCommand

    case class EntityDead[E <: Entity](ref: ActorRef[ModelMessage]) extends GameLoopCommand

    case class EntityUpdated[E <: Entity](ref: ActorRef[ModelMessage], entity: E) extends GameLoopCommand

    case class EntitySpawned[E <: Entity](ref: ActorRef[ModelMessage], entity: E) extends GameLoopCommand











  
  


