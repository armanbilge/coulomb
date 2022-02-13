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

import coulomb.kernel.*

import scala.compiletime.ops.int.*

final type SIDimension[
  +Length <: Int,
  +Mass <: Int,
  +Time <: Int,
  +ElectricCurrent <: Int,
  +AmountOfSubstance <: Int,
  +LuminousIntensity <: Int,
  +Temperature <: Int
]

object SIDimension:

  given Dimension[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type Id = SIDimension[0, 0, 0, 0, 0, 0, 0]

    infix type *[D1 <: SIDimension[Int, Int, Int, Int, Int, Int, Int], D2 <: SIDimension[Int, Int, Int, Int, Int, Int, Int]] =
      (D1, D2) match
        case (SIDimension[x1, x2, x3, x4, x5, x6, x7], SIDimension[y1, y2, y3, y4, y5, y6, y7]) =>
          SIDimension[x1 + y1, x2 + y2, x3 + y3, x4 + y4, x5 + y5, x6 + y6, x7 + y7]

    infix type **[D1 <: SIDimension[Int, Int, Int, Int, Int, Int, Int], N <: Int] =
      D1 match
        case SIDimension[x1, x2, x3, x4, x5, x6, x7] =>
          SIDimension[x1 + N, x2 + N, x3 + N, x4 + N, x5 + N, x6 + N, x7 + N]

    infix type /[D1 <: SIDimension[Int, Int, Int, Int, Int, Int, Int], D2 <: SIDimension[Int, Int, Int, Int, Int, Int, Int]] =
      (D1, D2) match
        case (SIDimension[x1, x2, x3, x4, x5, x6, x7], SIDimension[y1, y2, y3, y4, y5, y6, y7]) =>
          SIDimension[x1 - y1, x2 - y2, x3 - y3, x4 - y4, x5 - y5, x6 - y6, x7 - y7]

  given Length[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[1, 0, 0, 0, 0, 0, 0]

  given Mass[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 1, 0, 0, 0, 0, 0]

  given Time[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 0, 1, 0, 0, 0, 0]

  given ElectricCurrent[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 0, 0, 1, 0, 0, 0]

  given AmountOfSubstance[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 0, 0, 0, 1, 0, 0]

  given LuminousIntensity[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 0, 0, 0, 0, 1, 0]

  given Temperature[SIDimension[Int, Int, Int, Int, Int, Int, Int]] with
    type T = SIDimension[0, 0, 0, 0, 0, 0, 1]
