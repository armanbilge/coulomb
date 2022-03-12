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

object mksa:
    import coulomb.define.*
    import coulomb.{`*`, `/`, `^`}
    import coulomb.units.si.*
    import coulomb.units.mks.*

    export coulomb.units.si.{Ampere, ctx_unit_Ampere}
    export coulomb.units.mks.{*, given}
    
    final type Coulomb
    given ctx_unit_Coulomb: DerivedUnit[Coulomb, Ampere * Second, 1, "coulomb", "C"] with {}

    final type Volt
    given ctx_unit_Volt: DerivedUnit[Volt, Watt / Ampere, 1, "volt", "V"] with {}

    final type Ohm
    given ctx_unit_Ohm: DerivedUnit[Ohm, Volt / Ampere, 1, "ohm", "Ω"] with {}

    final type Farad
    given ctx_unit_Farad: DerivedUnit[Farad, Coulomb / Volt, 1, "farad", "F"] with {}

    final type Siemens
    given ctx_unit_Siemens: DerivedUnit[Siemens, 1 / Ohm, 1, "siemens", "S"] with {}

    final type Weber
    given ctx_unit_Weber: DerivedUnit[Weber, Volt * Second, 1, "weber", "Wb"] with {}

    final type Tesla
    given ctx_unit_Tesla: DerivedUnit[Tesla, Weber / (Meter ^ 2), 1, "tesla", "T"] with {}

    final type Henry
    given ctx_unit_Henry: DerivedUnit[Henry, Weber / Ampere, 1, "henry", "H"] with {}
