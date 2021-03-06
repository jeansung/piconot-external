package piconot.parser

import scala.util.parsing.combinator._
import piconot.ir._

object PiconotParser extends JavaTokenParsers with PackratParsers {
	// tell parser to consider whitespace
	override val skipWhitespace = false
	
    // parsing interface
    def apply(s: String): ParseResult[AST] = parseAll(program, s)
    
    def applyForRules(s: String): ParseResult[Rules] = parseAll(rulesList, s)

    // full program
    lazy val program: Parser[PicobotProgram] = 
      (
       filename~"\n"~rulesList ^^ {case f~"\n"~l => Program(f, l) }
      )
      
    lazy val rulesList:Parser[Rules] =
      (
       repsep(rule, "\n") ^^ {case list1 => Rules(list1)}
      )
    
    lazy val filename: Parser[String] = (ident ^^ {s => s.toString + ".txt"} )
    // rule
    lazy val rule: PackratParser[Rule] = 
      (
          state~" "~surr~" -> "~mov~" "~state ^^ 
          {case state1~" "~surr1~" -> "~movedir~" "~state2 => 
  			Rule(state1, surr1, movedir, state2)}
          | failure("Invalid rule")
      )
                    
    // state
    lazy val state: Parser[State] = 
     ( wholeNumber ^^ {s ⇒ State(s.toInt)} | failure("Invalid state"))
    
    // surroundings
    lazy val surr: Parser[Surroundings] =  
    (
      surrCompNorth~surrCompEast~surrCompWest~surrCompSouth ^^ {case a~b~c~d => Surroundings(a, b, c, d)} 
      | failure("Invalid surroundings (bad order)")
    )
    
    // surrounding components (with proper ordering enforced)
    lazy val surrComp: Parser[SurroundingComponentType] = 
      ( free | wildcard | failure("Invalid surrounding component") )
      
    def free: Parser[SurroundingComponentType] = 'x' ^^^ Free
    def wildcard: Parser[SurroundingComponentType] = '*' ^^^ Wildcard
    
    lazy val surrCompNorth: Parser[SurroundingComponentType] = ( surrComp | north )
    lazy val surrCompEast: Parser[SurroundingComponentType] = ( surrComp | east )
    lazy val surrCompWest: Parser[SurroundingComponentType] = ( surrComp | west )
    lazy val surrCompSouth: Parser[SurroundingComponentType] = ( surrComp | south )
    
    lazy val surrComps = ( north | south | east | west | failure("Invalid surrounding component"))
    def north: Parser[SurroundingComponentType] = 'N' ^^^ Blocked
    def south: Parser[SurroundingComponentType] = 'S' ^^^ Blocked
    def east: Parser[SurroundingComponentType] = 'E' ^^^ Blocked
    def west: Parser[SurroundingComponentType] = 'W' ^^^ Blocked
       
    // movement directions
    lazy val mov: Parser[MoveDirection] =
    (
      movComp ^^ {a => MoveDirection(a)}
    )
    
    lazy val moveDirs = ( movNor | movSouth | movEast | movWest | failure("Invalid move direction"))
    def movNor: Parser[MoveDirectionType] = 'N' ^^^ MoveNorth
    def movSouth: Parser[MoveDirectionType] = 'S' ^^^ MoveSouth
    def movEast: Parser[MoveDirectionType] = 'E' ^^^ MoveEast
    def movWest: Parser[MoveDirectionType] = 'W' ^^^ MoveWest
    def halt: Parser[MoveDirectionType] = 'X' ^^^ Halt

    lazy val movComp: Parser[MoveDirectionType] = ( moveDirs | halt | failure("Invalid move direction"))
    
}
