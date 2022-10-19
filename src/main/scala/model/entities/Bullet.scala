package model.entities

import model.common.DefaultValues.*
import model.entities.WorldSpace.{Position, given}
import scala.concurrent.duration.FiniteDuration

trait Bullet extends MovingAbility with Entity:
  override type UpdatedEntity = Bullet
  def damage: Int = damages(this)
  def shouldDisappearAfterHitting(entity: Entity): Boolean = true
  def checkCollisionWith(entity: Entity): Boolean =
    entity.position.x - entity.width <= position.x + width

class PeaBullet(override val position: Position) extends Bullet:
  override def velocity: Float = 0.1

  override def checkCollisionWith(entity: Entity): Boolean =
    entity match
      case _: Turret => false
      case _: Enemy => super.checkCollisionWith(entity)

  override def update(elapsedTime: FiniteDuration, interests: List[Entity]): Bullet =
    PeaBullet(updatePosition(elapsedTime))
