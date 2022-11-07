package model

import model.waves.PrologWaveManager.*
import model.waves.PrologWaveManager.PrologEngine.PrologEngine
import alice.tuprolog.SolveInfo
import model.entities.{BasicZombie, FastZombie, WarriorZombie}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class PrologWaveTest extends AnyFlatSpec with should.Matchers:
  val pathTheory = "prolog/waves.pl"
  val engine: PrologEngine = PrologEngine(PrologTheory.getTheory(pathTheory))
  
  "The engine" should "create a 1-length solution" in {
    val query: String = "wave(1, L)"
    val solution: LazyList[SolveInfo] = engine solve WaveTerm.queryToTerm(query)
    PrologSolution.waveFromPrologSolution(solution.head).size shouldEqual 1
  }

  "The engine" should "create a correct solution" in {
    val query: String = "wave(6, L)"
    val solution: LazyList[SolveInfo] = engine solve WaveTerm.queryToTerm(query)
    PrologSolution.waveFromPrologSolution(solution.head).foldRight(0)((e, acc) => {
      e match
        case BasicZombie(_, _, _) => acc + 1
        case FastZombie(_, _, _) => acc + 2
        case WarriorZombie(_, _, _) => acc + 3
    }) shouldEqual 6
  }

