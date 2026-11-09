package fi.jukkakot.terveyslappu.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import fi.jukkakot.terveyslappu.R
import fi.jukkakot.terveyslappu.profile.FieldInput
import fi.jukkakot.terveyslappu.profile.Gender
import fi.jukkakot.terveyslappu.profile.OwnerProfile
import fi.jukkakot.terveyslappu.profile.ProfileRules
import fi.jukkakot.terveyslappu.profile.parseBirthYear
import fi.jukkakot.terveyslappu.profile.parseHeight
import java.time.LocalDate

/**
 * Omat tiedot (owner-profile): birth year with the age it gives, height, gender. A typed value is kept
 * when the field loses focus or Valmis is pressed; an implausible one is not kept and shows the range.
 */
@Composable
internal fun OwnerFields(profile: OwnerProfile, heightInHealthConnect: Boolean, onProfile: (OwnerProfile) -> Unit) {
    val year = LocalDate.now().year
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NumberField(
            value = profile.birthYear,
            label = stringResource(R.string.profile_birth_year),
            parse = { parseBirthYear(it, year) },
            hint = profile.birthYear?.let { stringResource(R.string.profile_age, ProfileRules.age(it, year)) },
            rangeText = stringResource(R.string.profile_birth_year_range, ProfileRules.MIN_BIRTH_YEAR, year),
            tag = "birthYear",
            onKeep = { onProfile(profile.copy(birthYear = it)) },
        )
        NumberField(
            value = profile.heightCm,
            label = stringResource(R.string.profile_height),
            parse = ::parseHeight,
            hint = if (profile.heightCm != null && !heightInHealthConnect) stringResource(R.string.profile_height_not_hc) else null,
            rangeText = stringResource(R.string.profile_height_range, ProfileRules.MIN_HEIGHT_CM, ProfileRules.MAX_HEIGHT_CM),
            tag = "height",
            onKeep = { onProfile(profile.copy(heightCm = it)) },
        )
        Text(stringResource(R.string.profile_gender), style = MaterialTheme.typography.bodyLarge)
        GenderChoice(profile.gender) { onProfile(profile.copy(gender = it)) }
    }
}

@Composable
private fun NumberField(
    value: Int?,
    label: String,
    parse: (String) -> FieldInput,
    hint: String?,
    rangeText: String,
    tag: String,
    onKeep: (Int?) -> Unit,
) {
    var text by rememberSaveable(value) { mutableStateOf(value?.toString() ?: "") }
    var focused by remember { mutableStateOf(false) }
    val input = parse(text)
    val invalid = input == FieldInput.Invalid
    val focus = LocalFocusManager.current
    fun keep() {
        when (input) {
            FieldInput.Empty -> if (value != null) onKeep(null)
            is FieldInput.Valid -> if (input.value != value) onKeep(input.value)
            FieldInput.Invalid -> Unit
        }
    }
    OutlinedTextField(
        value = text,
        onValueChange = { new -> text = new.filter { it.isDigit() }.take(4) },
        label = { Text(label) },
        singleLine = true,
        isError = invalid,
        supportingText = (if (invalid) rangeText else hint)?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged {
                if (focused && !it.isFocused) keep()
                focused = it.isFocused
            }
            .testTag(tag),
    )
}

private fun Gender?.label(): Int = when (this) {
    Gender.MALE -> R.string.gender_male
    Gender.FEMALE -> R.string.gender_female
    null -> R.string.gender_none
}

/** Filter chips like the theme choice. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GenderChoice(gender: Gender?, onGender: (Gender?) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(Gender.MALE, Gender.FEMALE, null).forEach { option ->
            val selected = option == gender
            FilterChip(
                selected = selected,
                onClick = { onGender(option) },
                label = { Text(stringResource(option.label())) },
                leadingIcon = if (selected) {
                    { Icon(painterResource(R.drawable.ic_check), contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else {
                    null
                },
                modifier = Modifier.testTag("gender-${option?.name ?: "NONE"}"),
            )
        }
    }
}
