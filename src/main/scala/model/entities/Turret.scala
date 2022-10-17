package model.entities

import model.common.DefaultValues.*
import model.entities.WorldSpace.{Position, given}
import model.entities.{AttackingAbility, Bullet, Enemy, Entity, PeaBullet, Turret, Zombie}

import scala.concurrent.duration.FiniteDuration

trait Turret extends Entity with AttackingAbility with Troop :
  def cost: Int = costs(this)

  override def isInterestedIn: Entity => Boolean =
    case enemy: Enemy => enemy.position.y == position.y
    case _ => false

/**
 * Basic turret.
 *
 * @param position The position in which the plant is placed by the player.
 */
case class PeaShooter(override val position: Position)(override val life: Int = 300) extends Turret :

  override type BulletType = PeaBullet

  override def collideWith(bullet: Bullet): Option[Turret] =
    val newHPs: Int = life - bullet.damage
    newHPs match
      case x if x > 0 => Some(PeaShooter(position)(newHPs))
      case _ => None

  override def bullet: BulletType = new PeaBullet(position)

  override def canAttack(enemy: Entity): Boolean =
    enemy.position.x.toInt <= range

  override def update(elapsedTime: FiniteDuration, interests: List[Entity]): PeaShooter =
    PeaShooter(position)()

