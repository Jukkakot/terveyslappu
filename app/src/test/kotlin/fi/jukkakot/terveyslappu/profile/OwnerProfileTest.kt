package fi.jukkakot.terveyslappu.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OwnerProfileTest {
    private val year = 2026

    @Test
    fun `birth year accepts 1900 to this year`() {
        assertEquals(FieldInput.Valid(1990), parseBirthYear("1990", year))
        assertEquals(FieldInput.Valid(1900), parseBirthYear(" 1900 ", year))
        assertEquals(FieldInput.Valid(2026), parseBirthYear("2026", year))
        assertEquals(FieldInput.Invalid, parseBirthYear("2027", year))
        assertEquals(FieldInput.Invalid, parseBirthYear("1899", year))
        assertEquals(FieldInput.Invalid, parseBirthYear("19x0", year))
        assertEquals(FieldInput.Empty, parseBirthYear("  ", year))
    }

    @Test
    fun `height accepts whole centimetres 50 to 250`() {
        assertEquals(FieldInput.Valid(180), parseHeight("180"))
        assertEquals(FieldInput.Valid(50), parseHeight("50"))
        assertEquals(FieldInput.Valid(250), parseHeight("250"))
        assertEquals(FieldInput.Invalid, parseHeight("25"))
        assertEquals(FieldInput.Invalid, parseHeight("251"))
        assertEquals(FieldInput.Invalid, parseHeight("180,5"))
        assertEquals(FieldInput.Empty, parseHeight(""))
    }

    @Test
    fun `age is this year minus the birth year`() {
        assertEquals(36, ProfileRules.age(1990, year))
    }

    @Test
    fun `filling keeps set fields and takes only the empty ones`() {
        val phone = OwnerProfile(heightCm = 182)
        val file = OwnerProfile(birthYear = 1990, heightCm = 180, gender = Gender.MALE)
        assertEquals(OwnerProfile(1990, 182, Gender.MALE), phone.fillEmptyFrom(file))
    }

    @Test
    fun `changed fields are named without values`() {
        assertEquals(listOf("height"), OwnerProfile(heightCm = 180).changedFields(OwnerProfile()))
        assertEquals(emptyList(), OwnerProfile().changedFields(OwnerProfile()))
    }

    @Test
    fun `profile line holds the set fields only`() {
        assertEquals("#profile;birth_year=1990|height_cm=180", ProfileLine.encode(OwnerProfile(birthYear = 1990, heightCm = 180)))
        assertEquals("#profile;gender=female", ProfileLine.encode(OwnerProfile(gender = Gender.FEMALE)))
        assertNull(ProfileLine.encode(OwnerProfile()))
    }

    @Test
    fun `profile line reads back and ignores the unknown and implausible`() {
        val full = OwnerProfile(1990, 180, Gender.MALE)
        assertEquals(full, ProfileLine.decode(ProfileLine.encode(full)!!, year))
        val odd = ProfileLine.decode("#profile;height_cm=999|birth_year=1985|shoe=44|gender=x", year)!!
        assertEquals(OwnerProfile(birthYear = 1985), odd)
        assertNull(ProfileLine.decode("date;time;weight_kg;source;raw", year))
        assertTrue(ProfileLine.decode("#profile;", year)!!.isEmpty)
    }
}
