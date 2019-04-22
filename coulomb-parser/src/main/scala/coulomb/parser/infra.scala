/*
Copyright 2017-2019 Erik Erlandson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package coulomb.parser

object infra {
  import scala.language.implicitConversions
  import scala.reflect.runtime.universe._

  import shapeless._

  import coulomb._
  import coulomb.infra._
  import coulomb.define._

  trait Evidence[T] {
    type Out
  }
  trait EvidenceLowPriority {
    type Aux[T, O] = Evidence[T] { type Out = O }
    implicit def evidenceFalse[T]: Aux[T, false] =
      new Evidence[T] { type Out = false }
  }
  object Evidence extends EvidenceLowPriority {
    implicit def evidenceTrue[T](implicit t: T): Aux[T, true] =
      new Evidence[T] { type Out = true }
  }

  trait NoEvidence[T]
  object NoEvidence {
    // Note: if you try to directly ask for Evidence.Aux[T, false], it will match the
    // low-priority rule above, and this won't work right.
    implicit def evidence0[T, V](implicit no: Evidence.Aux[T, V], vf: V =:= false): NoEvidence[T] =
      new NoEvidence[T] {}
  }

  trait SetInsert[E, S] {
    type Out
  }
  object SetInsert {
    type Aux[E, S, O] = SetInsert[E, S] { type Out = O }
    implicit def evidence0[E]: Aux[E, HNil, E :: HNil] = new SetInsert[E, HNil] { type Out = E :: HNil }
    implicit def evidence1[E, T <: HList]: Aux[E, E :: T, E :: T] =
      new SetInsert[E, E :: T] { type Out = E :: T }
    implicit def evidence2[E, H, T <: HList, O <: HList ](implicit
        ne: E =:!= H,
        rc: Aux[E, T, O]): Aux[E, H :: T, H :: O] =
      new SetInsert[E, H :: T] { type Out = H :: O }
  }

  trait SetUnion[S1, S2] {
    type Out
  }
  object SetUnion {
    type Aux[S1, S2, O] = SetUnion[S1, S2] { type Out = O }
    implicit def evidence0[S2]: Aux[HNil, S2, S2] = new SetUnion[HNil, S2] { type Out = S2 }
    implicit def evidence1[H, T <: HList, S2, T2, O](implicit
        ih: SetInsert.Aux[H, S2, T2],
        rc: Aux[T, T2, O]): Aux[H :: T, S2, O] =
      new SetUnion[H :: T, S2] { type Out = O }
  }

  trait UnitClosure[U] {
    type Out
  }
  object UnitClosure {
    type Aux[U, O] = UnitClosure[U] { type Out = O }

    implicit def evidenceUnitless: Aux[Unitless, Unitless :: HNil] =
      new UnitClosure[Unitless] { type Out = Unitless :: HNil }

    implicit def evidenceBase[U](implicit bu: BaseUnit[U]): Aux[U, U :: HNil] =
      new UnitClosure[U] { type Out = U :: HNil }

    implicit def evidenceDerivedUnit[U, D, DC <: HList](implicit
        du: DerivedUnit[U, D],
        dc: Aux[D, DC]): Aux[U, U :: DC] = {
      new UnitClosure[U] { type Out = U :: DC }
    }

    implicit def evidenceMul[L, LC, R, RC, OC](implicit
        l: Aux[L, LC],
        r: Aux[R, RC],
        u: SetUnion.Aux[LC, RC, OC]): Aux[%*[L, R], OC] = {
      new UnitClosure[%*[L, R]] { type Out = OC }
    }

    implicit def evidenceDiv[L, LC, R, RC, OC](implicit
        l: Aux[L, LC],
        r: Aux[R, RC],
        u: SetUnion.Aux[LC, RC, OC]): Aux[%/[L, R], OC] = {
      new UnitClosure[%/[L, R]] { type Out = OC }
    }

    implicit def evidencePow[B, BC, E](implicit
        b: Aux[B, BC],
        e: XIntValue[E]): Aux[%^[B, E], BC] = {
      new UnitClosure[%^[B, E]] {
        type Out = BC
      }
    }

    implicit def evidenceHNil: Aux[HNil, HNil] =
      new UnitClosure[HNil] { type Out = HNil }

    implicit def evidenceHList[H, T <: HList, HC, TC, OC](implicit
        h: Aux[H, HC],
        t: Aux[T, TC],
        u: SetUnion.Aux[HC, TC, OC]): Aux[H :: T, OC] = {
      new UnitClosure[H :: T] { type Out = OC }
    }
  }

  trait FilterPrefixUnits[S] {
    type Out
  }
  object FilterPrefixUnits {
    type Aux[S, O] = FilterPrefixUnits[S] { type Out = O }
    implicit def evidence0: Aux[HNil, HNil] = new FilterPrefixUnits[HNil] { type Out = HNil }
    implicit def evidence1[U, T <: HList, TF <: HList](implicit
        pfu: DerivedUnit[U, Unitless],
        tf: Aux[T, TF]): Aux[U :: T, U :: TF] = {
      new FilterPrefixUnits[U :: T] { type Out = U :: TF }
    }
    implicit def evidence2[U, T <: HList, TF <: HList](implicit
        npfu: NoEvidence[DerivedUnit[U, Unitless]],
        tf: Aux[T, TF]): Aux[U :: T, TF] = {
      new FilterPrefixUnits[U :: T] { type Out = TF }
    }
  }

  trait FilterNonPrefixUnits[S] {
    type Out
  }
  trait FilterNonPrefixUnitsP1 {
    type Aux[S, O] = FilterNonPrefixUnits[S] { type Out = O }
    implicit def evidence3[U, T <: HList, TF <: HList](implicit
        tf: Aux[T, TF]): Aux[U :: T, TF] = {
      new FilterNonPrefixUnits[U :: T] { type Out = TF }
    }
  }
  object FilterNonPrefixUnits extends FilterNonPrefixUnitsP1 {
    implicit def evidence0: Aux[HNil, HNil] = new FilterNonPrefixUnits[HNil] { type Out = HNil }
    implicit def evidence1[U, T <: HList, TF <: HList](implicit
        pfu: DerivedUnit[U, _],
        npfu: NoEvidence[DerivedUnit[U, Unitless]],
        tf: Aux[T, TF]): Aux[U :: T, U :: TF] = {
      new FilterNonPrefixUnits[U :: T] { type Out = U :: TF }
    }
    implicit def evidence2[U, T <: HList, TF <: HList](implicit
        pfu: BaseUnit[U],
        tf: Aux[T, TF]): Aux[U :: T, U :: TF] = {
      new FilterNonPrefixUnits[U :: T] { type Out = U :: TF }
    }
  }

  trait UnitTypeString[U] {
    def expr: String
  }
  object UnitTypeString {
    implicit def evidenceUnitless: UnitTypeString[Unitless] =
      new UnitTypeString[Unitless] { val expr = "coulomb.Unitless" }

    implicit def evidenceBase[U](implicit utt: TypeTag[U], bu: BaseUnit[U]): UnitTypeString[U] =
      new UnitTypeString[U] { val expr = utt.tpe.typeSymbol.fullName }

    implicit def evidenceDerived[U](implicit utt: TypeTag[U], du: DerivedUnit[U, _]): UnitTypeString[U] =
      new UnitTypeString[U] { val expr = utt.tpe.typeSymbol.fullName }

    implicit def evidenceMul[L, R](implicit udfL: UnitTypeString[L], udfR: UnitTypeString[R]): UnitTypeString[%*[L, R]] =
      new UnitTypeString[%*[L, R]] { val expr = s"%*[${udfL.expr}, ${udfR.expr}]" }

    implicit def evidenceDiv[L, R](implicit udfL: UnitTypeString[L], udfR: UnitTypeString[R]): UnitTypeString[%/[L, R]] =
      new UnitTypeString[%/[L, R]] { val expr = s"%/[${udfL.expr}, ${udfR.expr}]" }

    implicit def evidencePow[B, E](implicit udfB: UnitTypeString[B], e: XIntValue[E]): UnitTypeString[%^[B, E]] =
      new UnitTypeString[%^[B, E]] { val expr = s"%^[${udfB.expr}, ${e.value}]" }
  }

  case class UnitDefCode[U](name: String, tpe: String, tpeFull: String, defCode: String)
  object UnitDefCode {
    implicit def evidenceBase[U](implicit utt: TypeTag[U], bu: BaseUnit[U]): UnitDefCode[U] = {
      val tpe = utt.tpe.typeSymbol.name
      val tpeFull = utt.tpe.typeSymbol.fullName
      UnitDefCode[U](
        bu.name,
        tpe.toString,
        tpeFull.toString,
        s"""implicit val qpDefineUnit$tpe = new BaseUnit[$tpeFull]("${bu.name}", "${bu.abbv}")""")
    }

    implicit def evidenceDerived[U, D](implicit utt: TypeTag[U], du: DerivedUnit[U, D], uts: UnitTypeString[D]): UnitDefCode[U] = {
      val tpe = utt.tpe.typeSymbol.name
      val tpeFull = utt.tpe.typeSymbol.fullName
      UnitDefCode[U](
        du.name,
        tpe.toString,
        tpeFull.toString,
        s"""implicit val qpDefineUnit$tpe = new DerivedUnit[$tpeFull, ${uts.expr}](Rational("${du.coef}"), "${du.name}", "${du.abbv}")""")
    }
  }

  case class UnitDefs[S](codes: List[UnitDefCode[_]])
  object UnitDefs {
    implicit def evidence0: UnitDefs[HNil] = UnitDefs[HNil](Nil)
    implicit def evidence1[U, T <: HList](implicit c: UnitDefCode[U], t: UnitDefs[T]): UnitDefs[U :: T] =
      UnitDefs[U :: T](c :: t.codes)
  }

  case class QPP[S](unames: Seq[String], pfnames: Seq[String], decls: Seq[String], nameToType: Map[String, String])
  object QPP {
    implicit def evidence0[S, SU, SPU](implicit
        units: FilterNonPrefixUnits.Aux[S, SU],
        pfunits: FilterPrefixUnits.Aux[S, SPU],
        udefs: UnitDefs[SU],
        pfdefs: UnitDefs[SPU]): QPP[S] = {
      val alldefs = udefs.codes ++ pfdefs.codes
      val nameToType = Map(alldefs.map { u => (u.name, u.tpeFull) } :_*)
      QPP[S](udefs.codes.map(_.name), pfdefs.codes.map(_.name), alldefs.map(_.defCode), nameToType)
    }
  }
}