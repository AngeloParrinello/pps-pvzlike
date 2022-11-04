package model.waves

import alice.tuprolog.{Engine, Number, Prolog, SolveInfo, Struct, Term, Theory}
import model.entities.{BasicZombie, FastZombie, Troops, WarriorZombie, Zombie}

import java.util.Scanner
import scala.io.Source

object PrologWaveManager:
  
  object PrologEngine:

    trait Engine {
      def solve: Term => LazyList[SolveInfo]

      def generateWave(Power: Int): List[Zombie]
    }

    case class PrologEngine(theory: Theory) extends Engine {
      val engine: Prolog = new Prolog
      engine.setTheory(theory)

      override def solve: Term => LazyList[SolveInfo] = term =>
        LazyList.continually(engine solve term)

      override def generateWave(power: Int): List[Zombie] =
        val query: String = "wave(" + power + ", L)"
        val solution: LazyList[SolveInfo] = solve(WaveTerm.queryToTerm(query))
        PrologSolution.waveFromPrologSolution(solution.head)
    }

  object PrologTheory:
    private def getStringTheory(resourcePath: String): String = Source.fromResource(resourcePath).mkString

    def getTheory(resourcePath: String): Theory = 
      val stringTheory = getStringTheory(resourcePath)
      Theory.parseWithStandardOperators(stringTheory)

  object WaveTerm:

    def queryToTerm(query: String): Term = Term.createTerm(query)

  object PrologSolution:

    def waveFromPrologSolution(solution: SolveInfo): List[Zombie] =
      waveFromTerm(solution.getTerm("L"))

    def waveFromTerm(term: Term): List[Zombie] =
      term.toString.replaceAll("\\[|\\]", "")
                   .split(",")
                   .foldRight(List.empty[Int])((e, acc) => acc :+ e.toInt)
                   .map(e => e match
                     case 1 => BasicZombie()
                     case 2 => FastZombie()
                     case 3 => WarriorZombie())

