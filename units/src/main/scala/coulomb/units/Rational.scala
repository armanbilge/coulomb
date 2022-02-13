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

package coulomb.units

import scala.compiletime.ops.int.*

import Rational.ops.gcd

final type Rational[Num <: Int, Den <: Int] = gcd[Num, Den] match
  case 1 => ReducedRational[Num, Den]
  case Int => ReducedRational[Num / gcd[Num, Den], Den / gcd[Num, Den]]

private type ReducedRational[Num <: Int, Den <: Int]

object Rational:
  object ops:
    private[units] type gcd[X <: Int, Y <: Int] <: Int =
      Y match
        case 0 => X
        case Int => gcd[Y, X % Y]
