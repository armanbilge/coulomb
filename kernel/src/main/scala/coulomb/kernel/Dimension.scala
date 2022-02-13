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

package coulomb.kernel

trait Dimension[D]:
  type Id <: D

  infix type *[D1 <: D, D2 <: D] <: D
  infix type **[D1 <: D, N <: Int] <: D
  infix type /[D1 <: D, D2 <: D] <: D

trait Time[D]:
  type T <: D

trait Length[D]:
  type T <: D

trait Mass[D]:
  type T <: D
  
trait ElectricCurrent[D]:
  type T <: D

trait Temperature[D]:
  type T <: D

trait AmountOfSubstance[D]:
  type T <: D

trait LuminousIntensity[D]:
  type T <: D
