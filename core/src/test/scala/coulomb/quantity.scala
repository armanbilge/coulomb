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

import coulomb.testing.CoulombSuite

class QuantitySuite extends CoulombSuite:
    import coulomb.*
    import coulomb.testing.units.{*, given}

    test("lift via Quantity") {
        Quantity[Meter](3.14).assertQ[Double, Meter](3.14)
        Quantity[Second](7.7f).assertQ[Float, Second](7.7f)
        Quantity[Kilogram](42L).assertQ[Long, Kilogram](42)
        Quantity[Liter](99).assertQ[Int, Liter](99)
        Quantity[Minute]("foo").assertQ[String, Minute]("foo")
    }

    test("lift via withUnit") {
        1d.withUnit[Meter].assertQ[Double, Meter](1)
        1f.withUnit[Second].assertQ[Float, Second](1)
        1L.withUnit[Kilogram].assertQ[Long, Kilogram](1)
        1.withUnit[Liter].assertQ[Int, Liter](1)
        "foo".withUnit[Minute].assertQ[String, Minute]("foo")
    }

    test("value") {
       7d.withUnit[Meter].value.assertVT[Double](7)
       73f.withUnit[Second].value.assertVT[Float](73)
       37L.withUnit[Kilogram].value.assertVT[Long](37)
       13.withUnit[Liter].value.assertVT[Int](13)
       "foo".withUnit[Minute].value.assertVT[String]("foo")
    }

    test("toValue standard") {
        import coulomb.conversion.standard.given

        1.withUnit[Meter].toValue[Int].assertQ[Int, Meter](1)
        1.withUnit[Meter].toValue[Long].assertQ[Long, Meter](1)
        1.withUnit[Meter].toValue[Float].assertQ[Float, Meter](1)
        1.withUnit[Meter].toValue[Double].assertQ[Double, Meter](1)
 
        1L.withUnit[Meter].toValue[Int].assertQ[Int, Meter](1)
        1L.withUnit[Meter].toValue[Long].assertQ[Long, Meter](1)
        1L.withUnit[Meter].toValue[Float].assertQ[Float, Meter](1)
        1L.withUnit[Meter].toValue[Double].assertQ[Double, Meter](1)

        // fp to truncating types is an error without importing truncating rules
        assertCE("1f.withUnit[Meter].toValue[Int]")
        assertCE("1f.withUnit[Meter].toValue[Long]")
        1f.withUnit[Meter].toValue[Float].assertQ[Float, Meter](1)
        1f.withUnit[Meter].toValue[Double].assertQ[Double, Meter](1)

        assertCE("1d.withUnit[Meter].toValue[Int]")
        assertCE("1d.withUnit[Meter].toValue[Long]")
        1d.withUnit[Meter].toValue[Float].assertQ[Float, Meter](1)
        1d.withUnit[Meter].toValue[Double].assertQ[Double, Meter](1)
    }

    test("toValue standard truncating") {
        import coulomb.policy.allowTruncation.given
        import coulomb.conversion.standard.given

        1.5f.withUnit[Meter].toValue[Int].assertQ[Int, Meter](1)
        0.999f.withUnit[Meter].toValue[Long].assertQ[Long, Meter](0)

        1.5d.withUnit[Meter].toValue[Int].assertQ[Int, Meter](1)
        0.999d.withUnit[Meter].toValue[Long].assertQ[Long, Meter](0)
    }

    test("toUnit standard") {
        import coulomb.conversion.standard.given

        1.5f.withUnit[Minute].toUnit[Second].assertQ[Float, Second](90)
        1d.withUnit[Meter].toUnit[Yard].assertQD[Double, Yard](1.0936132983377078)
        // conversions on truncating types require importing truncating conversion rules
        assertCE("1.withUnit[Minute].toUnit[Second]")
        assertCE("1L.withUnit[Yard].toUnit[Meter]")
    }

    test("toUnit standard truncating") {
        import coulomb.policy.allowTruncation.given
        import coulomb.conversion.standard.given

        1.withUnit[Minute].toUnit[Second].assertQ[Int, Second](60)
        1L.withUnit[Yard].toUnit[Meter].assertQ[Long, Meter](0)
    }

    test("implicit conversions") {
        import coulomb.conversion.standard.given

        // implicit conversions only happen if you import them,
        // and set coulomb policy to allow them
        // https://docs.scala-lang.org/scala3/reference/contextual/conversions.html
        def f(q: Quantity[Double, Meter]): Double = q.value
        assertCE("f(1d.withUnit[Yard])")

        // toUnit and toValue will operate, because they are explicit conversion request
        f(1d.withUnit[Yard].toUnit[Meter]).assertVTD[Double](0.9144)
        f(1.withUnit[Meter].toValue[Double]).assertVTD[Double](1.0)

        object t {
            // enabling implicit conversions should allow them
            import scala.language.implicitConversions
            import coulomb.policy.allowImplicitConversions.given
            f(1d.withUnit[Yard]).assertVTD[Double](0.9144)
        }
    }

    test("addition standard strict") {
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        // importing conversions makes them available explicitly but they will
        // not be available implicitly unless that is enabled separately
        import coulomb.conversion.standard.given

        // adding w/ same value and unit requires no implicit conversion
        (1d.withUnit[Second] + 1d.withUnit[Second]).assertQ[Double, Second](2)
        (1f.withUnit[Meter] + 1f.withUnit[Meter]).assertQ[Float, Meter](2)
        (1L.withUnit[Kilogram] + 1L.withUnit[Kilogram]).assertQ[Long, Kilogram](2)
        (1.withUnit[Meter / Second] + 1.withUnit[Meter / Second]).assertQ[Int, Meter / Second](2)

        // changing either unit or value involves implicit conversion, should error out
        assertCE("1d.withUnit[Meter] + 1d.withUnit[Yard]")
        assertCE("1d.withUnit[Meter] + 1f.withUnit[Meter]")

        // explicit conversion will still work
        (1d.withUnit[Meter] + 1d.withUnit[Yard].toUnit[Meter]).assertQD[Double, Meter](1.9144)
        (1d.withUnit[Meter] + 1f.withUnit[Meter].toValue[Double]).assertQD[Double, Meter](2)
    }

    test("addition standard") {
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        // same value type, different units
        (1d.withUnit[Kilo * Second] + 1d.withUnit[Second]).assertQD[Double, Kilo * Second](1.001)
        (1f.withUnit[Meter] + 1f.withUnit[Yard]).assertQD[Float, Meter](1.9144)

        // same unit, differing value types
        (1d.withUnit[Meter] + 1f.withUnit[Meter]).assertQ[Double, Meter](2)
        (1d.withUnit[Meter] + 1L.withUnit[Meter]).assertQ[Double, Meter](2)
        (1d.withUnit[Meter] + 1.withUnit[Meter]).assertQ[Double, Meter](2)

        (1f.withUnit[Meter] + 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (1L.withUnit[Meter] + 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (1.withUnit[Meter] + 1d.withUnit[Meter]).assertQ[Double, Meter](2)

        (1f.withUnit[Meter] + 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (1f.withUnit[Meter] + 1L.withUnit[Meter]).assertQ[Float, Meter](2)
        (1f.withUnit[Meter] + 1.withUnit[Meter]).assertQ[Float, Meter](2)

        (1d.withUnit[Meter] + 1f.withUnit[Meter]).assertQ[Double, Meter](2)
        (1L.withUnit[Meter] + 1f.withUnit[Meter]).assertQ[Float, Meter](2)
        (1.withUnit[Meter] + 1f.withUnit[Meter]).assertQ[Float, Meter](2)

        // differing value and unit
        (1d.withUnit[Kilogram] + 1f.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)
        (1d.withUnit[Kilogram] + 1L.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)
        (1d.withUnit[Kilogram] + 1.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)

        (1f.withUnit[Kilogram] + 1d.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)
        (1L.withUnit[Kilogram] + 1d.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)
        (1.withUnit[Kilogram] + 1d.withUnit[Pound]).assertQD[Double, Kilogram](1.45359237)

        (1f.withUnit[Kilogram] + 1f.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)
        (1f.withUnit[Kilogram] + 1L.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)
        (1f.withUnit[Kilogram] + 1.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)

        (1f.withUnit[Kilogram] + 1f.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)
        (1L.withUnit[Kilogram] + 1f.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)
        (1.withUnit[Kilogram] + 1f.withUnit[Pound]).assertQD[Float, Kilogram](1.45359237)

        // non convertible units should fail
        assertCE("1d.withUnit[Meter] + 1d.withUnit[Second]")

        // unsafe truncating conversions should fail
        assertCE("1.withUnit[Meter] + 1.withUnit[Yard]")
        assertCE("1L.withUnit[Meter] + 1L.withUnit[Yard]")
    }

    test("addition standard truncating") {
        import coulomb.policy.allowTruncation.given
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        (1L.withUnit[Second] + 1L.withUnit[Minute]).assertQ[Long, Second](61)
        (1L.withUnit[Second] + 1.withUnit[Minute]).assertQ[Long, Second](61)
        (1.withUnit[Second] + 1L.withUnit[Minute]).assertQ[Long, Second](61)
        (1.withUnit[Second] + 1.withUnit[Minute]).assertQ[Int, Second](61)

        // integer truncation can cause loss of precision
        (1L.withUnit[Meter] + 1L.withUnit[Yard]).assertQ[Long, Meter](1)
        (1L.withUnit[Meter] + 1.withUnit[Yard]).assertQ[Long, Meter](1)
        (1.withUnit[Meter] + 1L.withUnit[Yard]).assertQ[Long, Meter](1)
        (1.withUnit[Meter] + 1.withUnit[Yard]).assertQ[Int, Meter](1)

        // fp effects (e.g. (3 * 0.3333).toInt -> 0) are avoided when possible
        (1.withUnit[Yard] + 3.withUnit[Foot]).assertQ[Int, Yard](2)
        (1L.withUnit[Yard] + 3L.withUnit[Foot]).assertQ[Long, Yard](2)
    }

    test("subtraction standard strict") {
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        // importing conversions makes them available explicitly but they will
        // not be available implicitly unless that is enabled separately
        import coulomb.conversion.standard.given

        // same value and unit requires no implicit conversion
        (3d.withUnit[Second] - 1d.withUnit[Second]).assertQ[Double, Second](2)
        (3f.withUnit[Meter] - 1f.withUnit[Meter]).assertQ[Float, Meter](2)
        (3L.withUnit[Kilogram] - 1L.withUnit[Kilogram]).assertQ[Long, Kilogram](2)
        (3.withUnit[Meter / Second] - 1.withUnit[Meter / Second]).assertQ[Int, Meter / Second](2)

        // changing either unit or value involves implicit conversion, should error out
        assertCE("1d.withUnit[Meter] - 1d.withUnit[Yard]")
        assertCE("1d.withUnit[Meter] - 1f.withUnit[Meter]")

        // explicit conversion will still work
        (1d.withUnit[Meter] - 1d.withUnit[Yard].toUnit[Meter]).assertQD[Double, Meter](0.0856)
        (3d.withUnit[Meter] - 1f.withUnit[Meter].toValue[Double]).assertQD[Double, Meter](2)
    }

    test("subtraction standard") {
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        // same value type, different units
        (1d.withUnit[Kilo * Second] - 1d.withUnit[Second]).assertQD[Double, Kilo * Second](0.999)
        (1f.withUnit[Meter] - 1f.withUnit[Yard]).assertQD[Float, Meter](0.0856)

        // same unit, differing value types
        (3d.withUnit[Meter] - 1f.withUnit[Meter]).assertQ[Double, Meter](2)
        (3d.withUnit[Meter] - 1L.withUnit[Meter]).assertQ[Double, Meter](2)
        (3d.withUnit[Meter] - 1.withUnit[Meter]).assertQ[Double, Meter](2)

        (3f.withUnit[Meter] - 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (3L.withUnit[Meter] - 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (3.withUnit[Meter] - 1d.withUnit[Meter]).assertQ[Double, Meter](2)

        (3f.withUnit[Meter] - 1d.withUnit[Meter]).assertQ[Double, Meter](2)
        (3f.withUnit[Meter] - 1L.withUnit[Meter]).assertQ[Float, Meter](2)
        (3f.withUnit[Meter] - 1.withUnit[Meter]).assertQ[Float, Meter](2)

        (3d.withUnit[Meter] - 1f.withUnit[Meter]).assertQ[Double, Meter](2)
        (3L.withUnit[Meter] - 1f.withUnit[Meter]).assertQ[Float, Meter](2)
        (3.withUnit[Meter] - 1f.withUnit[Meter]).assertQ[Float, Meter](2)

        // differing value and unit
        (1d.withUnit[Kilogram] - 1f.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)
        (1d.withUnit[Kilogram] - 1L.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)
        (1d.withUnit[Kilogram] - 1.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)

        (1f.withUnit[Kilogram] - 1d.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)
        (1L.withUnit[Kilogram] - 1d.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)
        (1.withUnit[Kilogram] - 1d.withUnit[Pound]).assertQD[Double, Kilogram](0.54640763)

        (1f.withUnit[Kilogram] - 1f.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)
        (1f.withUnit[Kilogram] - 1L.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)
        (1f.withUnit[Kilogram] - 1.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)

        (1f.withUnit[Kilogram] - 1f.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)
        (1L.withUnit[Kilogram] - 1f.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)
        (1.withUnit[Kilogram] - 1f.withUnit[Pound]).assertQD[Float, Kilogram](0.54640763)

        // non convertible units should fail
        assertCE("1d.withUnit[Meter] - 1d.withUnit[Second]")

        // unsafe truncating conversions should fail
        assertCE("1.withUnit[Meter] - 1.withUnit[Yard]")
        assertCE("1L.withUnit[Meter] - 1L.withUnit[Yard]")
    }

    test("subtraction standard truncating") {
        import coulomb.policy.allowTruncation.given
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        (61L.withUnit[Second] - 1L.withUnit[Minute]).assertQ[Long, Second](1)
        (61L.withUnit[Second] - 1.withUnit[Minute]).assertQ[Long, Second](1)
        (61.withUnit[Second] - 1L.withUnit[Minute]).assertQ[Long, Second](1)
        (61.withUnit[Second] - 1.withUnit[Minute]).assertQ[Int, Second](1)

        // integer truncation can cause loss of precision
        (2L.withUnit[Meter] - 1L.withUnit[Yard]).assertQ[Long, Meter](2)
        (2L.withUnit[Meter] - 1.withUnit[Yard]).assertQ[Long, Meter](2)
        (2.withUnit[Meter] - 1L.withUnit[Yard]).assertQ[Long, Meter](2)
        (2.withUnit[Meter] - 1.withUnit[Yard]).assertQ[Int, Meter](2)

        // fp effects (e.g. (3 * 0.3333).toInt -> 0) are avoided when possible
        (2.withUnit[Yard] - 3.withUnit[Foot]).assertQ[Int, Yard](1)
        (2L.withUnit[Yard] - 3L.withUnit[Foot]).assertQ[Long, Yard](1)
    }

    test("multiplication standard strict") {
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        // same value types require no implicit conversion
        (2d.withUnit[Meter] * 3d.withUnit[Meter]).assertQ[Double, Meter ^ 2](6)
        (2f.withUnit[Meter / Kilogram] * 3f.withUnit[Kilogram / Second]).assertQ[Float, Meter / Second](6)
        (2L.withUnit[Meter / Second] * 3L.withUnit[1 / Second]).assertQ[Long, Meter / (Second ^ 2)](6)
        (2.withUnit[Kilogram * Meter / (Second ^ 2)] * 3.withUnit[Meter])
            .assertQ[Int, Kilogram * (Meter ^ 2) / (Second ^ 2)](6)

        // changing value involves implicit conversion, should error out
        assertCE("2d.withUnit[Meter] * 3.withUnit[Meter]")

        // explicit conversion will still work
        (2d.withUnit[Meter] * 3.withUnit[Meter].toValue[Double]).assertQ[Double, Meter ^ 2](6)
    }

    test("multiplication standard") {
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        // differing value types
        (3d.withUnit[Meter] * 5d.withUnit[Meter]).assertQ[Double, Meter ^ 2](15)
        (3d.withUnit[Meter] * 5f.withUnit[Meter]).assertQ[Double, Meter ^ 2](15)
        (3d.withUnit[Meter] * 5L.withUnit[Meter]).assertQ[Double, Meter ^ 2](15)
        (3d.withUnit[Meter] * 5.withUnit[Meter]).assertQ[Double, Meter ^ 2](15)

        (3f.withUnit[Meter / Second] * 5d.withUnit[Second / Meter]).assertQ[Double, 1](15)
        (3f.withUnit[Meter / Second] * 5f.withUnit[Second / Meter]).assertQ[Float, 1](15)
        (3f.withUnit[Meter / Second] * 5L.withUnit[Second / Meter]).assertQ[Float, 1](15)
        (3f.withUnit[Meter / Second] * 5.withUnit[Second / Meter]).assertQ[Float, 1](15)

        (3L.withUnit[1 / Second] * 5d.withUnit[Meter / 1]).assertQ[Double, Meter / Second](15)
        (3L.withUnit[1 / Second] * 5f.withUnit[Meter / 1]).assertQ[Float, Meter / Second](15)
        (3L.withUnit[1 / Second] * 5L.withUnit[Meter / 1]).assertQ[Long, Meter / Second](15)
        (3L.withUnit[1 / Second] * 5.withUnit[Meter / 1]).assertQ[Long, Meter / Second](15)

        (3.withUnit[Second] * 5d.withUnit[Kilogram]).assertQ[Double, Second * Kilogram](15)
        (3.withUnit[Second] * 5f.withUnit[Kilogram]).assertQ[Float, Second * Kilogram](15)
        (3.withUnit[Second] * 5L.withUnit[Kilogram]).assertQ[Long, Second * Kilogram](15)
        (3.withUnit[Second] * 5.withUnit[Kilogram]).assertQ[Int, Second * Kilogram](15)
    }

    test("division standard strict") {
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        // same value types require no implicit conversion
        (12d.withUnit[Meter] / 3d.withUnit[Second]).assertQ[Double, Meter / Second](4)
        (12f.withUnit[Meter / Kilogram] / 3f.withUnit[Second / Kilogram]).assertQ[Float, Meter / Second](4)

        // changing value involves implicit conversion, should error out
        assertCE("12d.withUnit[Meter] / 3.withUnit[Second]")

        // integer division requires truncation 
        assertCE("12L.withUnit[Meter] / 3L.withUnit[Second]")
        assertCE("12.withUnit[Meter] / 3.withUnit[Second]")

        // explicit conversion will still work
        (12d.withUnit[Meter] / 3.withUnit[Second].toValue[Double]).assertQ[Double, Meter / Second](4)
    }

    test("division standard") {
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        (5d.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5d.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5d.withUnit[Meter] / 2L.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5d.withUnit[Meter] / 2.withUnit[Second]).assertQ[Double, Meter / Second](2.5)

        (5f.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5f.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        (5f.withUnit[Meter] / 2L.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        (5f.withUnit[Meter] / 2.withUnit[Second]).assertQ[Float, Meter / Second](2.5)

        (5L.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5L.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        assertCE("5L.withUnit[Meter] / 2L.withUnit[Second]")
        assertCE("5L.withUnit[Meter] / 2.withUnit[Second]")

        (5.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        assertCE("5.withUnit[Meter] / 2L.withUnit[Second]")
        assertCE("5.withUnit[Meter] / 2.withUnit[Second]")
    }

    test("division standard truncating") {
        import coulomb.policy.allowTruncation.given
        import coulomb.policy.allowImplicitConversions.given
        import coulomb.ops.standard.given
        import coulomb.ops.resolution.standard.given
        import coulomb.conversion.standard.given

        (5L.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5L.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        (5L.withUnit[Meter] / 2L.withUnit[Second]).assertQ[Long, Meter / Second](2)
        (5L.withUnit[Meter] / 2.withUnit[Second]).assertQ[Long, Meter / Second](2)

        (5.withUnit[Meter] / 2d.withUnit[Second]).assertQ[Double, Meter / Second](2.5)
        (5.withUnit[Meter] / 2f.withUnit[Second]).assertQ[Float, Meter / Second](2.5)
        (5.withUnit[Meter] / 2L.withUnit[Second]).assertQ[Long, Meter / Second](2)
        (5.withUnit[Meter] / 2.withUnit[Second]).assertQ[Int, Meter / Second](2)
    }

