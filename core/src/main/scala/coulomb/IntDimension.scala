/*
 * Copyright 2022 Erik Erlandson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package coulomb

import coulomb.kernel.Dimension

import scala.compiletime.ops.int.*

sealed trait IntDimension:
  type LengthN <: Int & Singleton
  type MassN <: Int & Singleton
  type TimeN <: Int & Singleton
  type ElectricCurrentN <: Int & Singleton
  type AmountOfSubstanceN <: Int & Singleton
  type LuminousIntensityN <: Int & Singleton
  type TemperatureN <: Int & Singleton

object IntDimension:
  type Aux[
    LengthN0,
    MassN0,
    TimeN0,
    ElectricCurrentN0,
    AmountOfSubstanceN0,
    LuminousIntensityN0,
    TempatureN0
  ] = IntDimension {
    type LengthN <: Int & Singleton
    type MassN <: Int & Singleton
    type TimeN <: Int & Singleton
    type ElectricCurrentN <: Int & Singleton
    type AmountOfSubstanceN <: Int & Singleton
    type LuminousIntensityN <: Int & Singleton
    type TemperatureN <: Int & Singleton
  }

  given dimension: Dimension[IntDimension] with
    type Identity = Aux[0, 0, 0, 0, 0, 0, 0]

    type Length = Aux[1, 0, 0, 0, 0, 0, 0]
    type Mass = Aux[0, 1, 0, 0, 0, 0, 0]
    type Time = Aux[0, 0, 1, 0, 0, 0, 0]
    type ElectricCurrent = Aux[0, 0, 0, 1, 0, 0, 0]
    type AmountOfSubstance = Aux[0, 0, 0, 0, 1, 0, 0]
    type LuminousIntensity = Aux[0, 0, 0, 0, 0, 1, 0]
    type Temperature = Aux[0, 0, 0, 0, 0, 0, 1]

    infix type *[D1 <: IntDimension, D2 <: IntDimension] =
      (D1, D2) match
        case (Aux[x1, x2, x3, x4, x5, x6, x7], Aux[y1, y2, y3, y4, y5, y6, y7]) =>
          Aux[x1 + y1, x2 + y2, x3 + y3, x4 + y4, x5 + y5, x6 + y6, x7 + y7]

    infix type **[D1 <: IntDimension, N <: Int & Singleton] =
      D1 match
        case Aux[x1, x2, x3, x4, x5, x6, x7] =>
          Aux[x1 + N, x2 + N, x3 + N, x4 + N, x5 + N, x6 + N, x7 + N]
    infix type /[D1 <: IntDimension, D2 <: IntDimension] = (D1, D2) match
      case (Aux[x1, x2, x3, x4, x5, x6, x7], Aux[y1, y2, y3, y4, y5, y6, y7]) =>
        Aux[x1 - y1, x2 - y2, x3 - y3, x4 - y4, x5 - y5, x6 - y6, x7 - y7]
