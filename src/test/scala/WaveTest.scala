import model.entities.{BasicZombie, Zombie}
import model.waves.WaveGenerator.Wave
import model.waves.WaveGenerator
import org.scalatest.flatspec.*
import org.scalatest.matchers.*

class WaveTest extends AnyFlatSpec with should.Matchers:
  "Wave generator" should "create a basic wave of zombies" in {
    WaveGenerator.generateNextBasicWave(5).enemies.forall(e => e.isInstanceOf[BasicZombie])
  }

  "Wave generator" should "create a fanciful wave of zombies with max 9 zombies" in {
    WaveGenerator.generateNextWave(5).enemies.size shouldBe  <=(9)
  }
