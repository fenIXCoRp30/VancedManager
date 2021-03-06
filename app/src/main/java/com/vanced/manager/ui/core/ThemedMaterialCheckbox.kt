package com.vanced.manager.ui.core

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.checkbox.MaterialCheckBox
import com.vanced.manager.R
import com.vanced.manager.utils.Extensions.getDefaultPrefs
import com.vanced.manager.utils.Extensions.lifecycleOwner
import com.vanced.manager.utils.ThemeHelper.accentColor
import com.vanced.manager.utils.ThemeHelper.defAccentColor

class ThemedMaterialCheckbox @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : MaterialCheckBox(context, attributeSet, R.attr.checkboxStyle) {
    init {
        buttonTintList = ColorStateList.valueOf(context.getDefaultPrefs().getInt("manager_accent_color", defAccentColor))
    }
}