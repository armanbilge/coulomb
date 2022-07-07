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

package coulomb.ops.resolution

object standard:
    import coulomb.ops.ValuePromotionPolicy
    import coulomb.ops.ValuePromotion.{ &:, TNil }

    // ValuePromotion infers the transitive closure of all promotions
    given ctx_vpp_standard: ValuePromotionPolicy[
        (Int, Long) &: (Long, Float) &: (Float, Double) &: TNil
    ] = ValuePromotionPolicy()
