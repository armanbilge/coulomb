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

trait BaseUnit

final type DimUnit[R, B <: BaseUnit]

final type Kilo[B <: BaseUnit] = DimUnit[Rational[1_000, 1], B]
final type Mega[B <: BaseUnit] = DimUnit[Rational[1_000_000, 1], B]
final type Giga[B <: BaseUnit] = DimUnit[Rational[1_000_000_000, 1], B]
final type Milli[B <: BaseUnit] = DimUnit[Rational[1, 1_000], B]
final type Micro[B <: BaseUnit] = DimUnit[Rational[1, 1_000_000], B]
final type Nano[B <: BaseUnit] = DimUnit[Rational[1, 1_000_000_000], B]
