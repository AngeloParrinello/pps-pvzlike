package model.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import controller.GameLoopActor.GameLoopCommands.{EntityUpdate, GameLoopCommand}
import model.entities.{Bullet, Enemy}

object BulletActor:
  def apply(bullet: Bullet): Behavior[ModelMessage] =
    moving(bullet)

  def moving(bullet: Bullet): Behavior[ModelMessage] =
    Behaviors.receive( (ctx,msg) => {
      msg match
        case Update(timeElapsed, _, replyTo) =>
          bullet updatePositionAfter timeElapsed
          replyTo ! EntityUpdate(ctx.self, bullet)
          Behaviors.same

        case Collision(entity, replyTo) =>
          if bullet shouldDisappearAfterHitting entity
          then
            replyTo ! EntityDead(bullet, ctx.self)
            Behaviors.stopped
          Behaviors.same

        case _ => Behaviors.same
    })
