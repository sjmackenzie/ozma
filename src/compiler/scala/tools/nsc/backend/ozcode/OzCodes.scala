/* NSC -- new scala compiler
 * Copyright 2005-2011 LAMP/EPFL
 * @author  Sébatien Doeraene
 */

package scala.tools.nsc
package backend
package ozcode

import ozma._

import java.io.PrintWriter
import scala.collection.mutable
import scala.tools.nsc.symtab._

/** Glue together OzCode parts.
 *
 *  @author Sébastien Doeraene
 */
abstract class OzCodes extends AnyRef with Members with ASTs with Natives
    with TypeKinds {
  val global: OzmaGlobal
  import global._

  /** The OzCode representation of classes */
  val classes = new mutable.HashMap[global.Symbol, OzClass]

  /** Debugging flag */
  def shouldCheckOzCode = settings.check contains global.ozcode.phaseName
  def checkerDebug(msg: String) =
    if (shouldCheckOzCode && global.opt.debug)
      println(msg)

  /** Print all classes and basic blocks. Used for debugging. */
  def dump {
    classes.values foreach (_.dump)
  }

  def buildMessage(label: ast.Atom, arguments: List[ast.Phrase],
      additionalArg: ast.Phrase = ast.Dollar()) =
    ast.Tuple(label, (arguments ++ List(additionalArg):_*))

  def genNew(clazz: Symbol, arguments: List[ast.Phrase] = Nil,
      label: ast.Atom = null) = {
    val actualLabel = if (label ne null)
      label
    else
      atomForSymbol("<init>", paramsHash(List(clazz.fullName)))

    val typeVar = varForSymbol(clazz)
    val classVar = varForClass(clazz)
    val message = buildMessage(actualLabel, arguments, ast.Wildcard())
    genBuiltinApply("NewObject", typeVar, classVar, message)
  }

  def genNewArray(elementKind: TypeKind, dimensions: Int,
      arguments: List[ast.Phrase]) = {
    val argsLength = arguments.length

    if (argsLength > dimensions)
      abort("too many arguments for array constructor: found " + argsLength +
        " but array has only " + dimensions + " dimension(s)")

    if (argsLength != 1)
      abort("FIXME: cannot create an array with multiple length arguments")

    val componentClass = elementKind.toType.typeSymbol

    genBuiltinApply("NewArrayObject", varForClass(componentClass),
        ast.IntLiteral(dimensions), arguments.head)
  }

  def genBuiltinApply(funName: String, args: ast.Phrase*) = {
    val pos = if (args.isEmpty) NoPosition else args(0).pos
    ast.Apply(ast.Variable(funName) setPos pos, args.toList) setPos pos
  }

  /* Symbol encoding */

  def varForSymbol(sym: Symbol): ast.Phrase with ast.EscapedFeature = {
    val name = if (sym.name.isTypeName)
      "type:" + sym.fullName
    else if (sym.isModule)
      "module:" + sym.fullName
    else if (sym.isStaticMember)
      "static:" + sym.fullName
    else if (sym.owner.isClass && !(sym.name.toString endsWith " "))
      " " + sym.name.toString
    else
      sym.name.toString

    if ((name contains ':') || sym.isPrivate || sym.isLocal)
      ast.QuotedVar(name + suffixFor(sym))
    else
      ast.Atom(name + suffixFor(sym))
  }

  def varForClass(sym: Symbol) = {
    val name = "class:" + sym.fullName
    ast.QuotedVar(name + suffixFor(sym))
  }

  def suffixFor(sym: Symbol) =
    if (sym.hasModuleFlag && !sym.isMethod && !sym.isImplClass) "$" else ""

  def atomForSymbol(sym: Symbol): ast.Atom = {
    val name = sym.name.toString
    val hash = if (sym.isMethod) paramsHash(sym) else 0
    atomForSymbol(name, hash)
  }

  def atomForSymbol(name: String, hash: Int) =
    ast.Atom(if (hash != 0) name + "#" + hash else name)

  /** In order to support method overloading on the Mozart platform, we give
   *  every method a hash code that is appended to its name on the back-end.
   *  <p>This method computes the hash of a method.
   *  <p>Two methods should have the same hash if and only if they would
   *  <i>match</i> (as defined by the Scala reference, definition 5.1.4)
   *  if they had the same name. This ensures that proper overriding applies
   *  when running on Mozart.
   *  <p>As currently implemented, we only guarantee that two matching methods
   *  will have the same hash. We do not guarantee that two non-matching
   *  methods will have different hashes, though we try to achieve this on a
   *  best-effort basis.
   */
  def paramsHash(sym: Symbol): Int = {
    sym.tpe match {
      case MethodType(params, resultType) =>
        paramsHash(params.toList map (_.tpe.typeSymbol.fullName),
            resultType.typeSymbol.fullName)

      case NullaryMethodType(resultType) =>
        paramsHash(Nil, resultType.typeSymbol.fullName)

      case _ => abort("Expected a method type for " + sym)
    }
  }

  def paramsHash(paramTypeNames: List[String], resultTypeName: String): Int =
    paramsHash(paramTypeNames ::: List(resultTypeName))

  def paramsHash(paramAndResultTypeNames: List[String]) = {
    (paramAndResultTypeNames.foldLeft((0, 0)) { (prevPair, item) =>
      val (idx, prev) = prevPair
      (idx+1, prev ^ rotateLeft(item.##, idx))
    })._2
  }

  private def rotateLeft(value: Int, shift: Int) =
    (value << shift) | (value >>> (32-shift))
}
