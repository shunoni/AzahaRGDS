// Copyright 2023-2026 Citra Emulator Project / Azahar Emulator Project
// Licensed under GPLv3 or any later version
// Refer to the LICENSE.txt file included.

package org.citra.citra_emu.display

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import org.citra.citra_emu.NativeLibrary
import org.citra.citra_emu.R
import org.citra.citra_emu.features.settings.model.BooleanSetting
import org.citra.citra_emu.features.settings.model.IntListSetting
import org.citra.citra_emu.features.settings.model.IntSetting
import org.citra.citra_emu.features.settings.utils.SettingsFile
import org.citra.citra_emu.utils.EmulationMenuSettings

class ScreenAdjustmentUtil(
    private val context: Context,
    private val windowManager: WindowManager
) {
    fun swapScreen(save:Boolean) {
        val isEnabled = !EmulationMenuSettings.swapScreens
        val displayRotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display.rotation
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }
        EmulationMenuSettings.swapScreens = isEnabled
        NativeLibrary.swapScreens(
            isEnabled,
            displayRotation
        )
        if(save) {
            BooleanSetting.SWAP_SCREEN.boolean = isEnabled
            SettingsFile.saveCurrentGameSetting(BooleanSetting.SWAP_SCREEN)
        }
    }

    fun cycleLayouts() {
        val landscapeLayoutsToCycle = IntListSetting.LAYOUTS_TO_CYCLE.list
        val landscapeValues =
            if (landscapeLayoutsToCycle.isNotEmpty()) {
                landscapeLayoutsToCycle.toIntArray()
            } else {
                context.resources.getIntArray(
                    R.array.landscapeValues
                )
            }
        val portraitValues = context.resources.getIntArray(R.array.portraitValues)

        if (NativeLibrary.isPortraitMode()) {
            val currentLayout = IntSetting.PORTRAIT_SCREEN_LAYOUT.int
            val pos = portraitValues.indexOf(currentLayout)
            val layoutOption = portraitValues[(pos + 1) % portraitValues.size]
            changePortraitOrientation(layoutOption)
        } else {
            val currentLayout = IntSetting.SCREEN_LAYOUT.int
            val pos = landscapeValues.indexOf(currentLayout)
            val layoutOption = landscapeValues[(pos + 1) % landscapeValues.size]
            changeScreenOrientation(layoutOption)
        }
    }

    fun changePortraitOrientation(layoutOption: Int) {
        IntSetting.PORTRAIT_SCREEN_LAYOUT.int = layoutOption
        SettingsFile.saveCurrentGameSetting(IntSetting.PORTRAIT_SCREEN_LAYOUT)
        NativeLibrary.reloadSettings()
        NativeLibrary.updateFramebuffer(NativeLibrary.isPortraitMode())
    }

    fun changeScreenOrientation(layoutOption: Int) {
        IntSetting.SCREEN_LAYOUT.int = layoutOption
        SettingsFile.saveCurrentGameSetting(IntSetting.SCREEN_LAYOUT)
        NativeLibrary.reloadSettings()
        NativeLibrary.updateFramebuffer(NativeLibrary.isPortraitMode())
    }

    fun changeSecondaryOrientation(layoutOption: Int) {
        IntSetting.SECONDARY_DISPLAY_LAYOUT.int = layoutOption
        SettingsFile.saveCurrentGameSetting(IntSetting.SECONDARY_DISPLAY_LAYOUT)
        NativeLibrary.reloadSettings()
        NativeLibrary.updateFramebuffer(NativeLibrary.isPortraitMode())
    }

    fun enableSecondaryDisplay(layoutOption: Int) {
        BooleanSetting.ENABLE_SECONDARY_DISPLAY.boolean = true
        SettingsFile.saveCurrentGameSetting(BooleanSetting.ENABLE_SECONDARY_DISPLAY)
        changeSecondaryOrientation(layoutOption)
    }

    fun disableSecondaryDisplay() {
        BooleanSetting.ENABLE_SECONDARY_DISPLAY.boolean = false
        SettingsFile.saveCurrentGameSetting(BooleanSetting.ENABLE_SECONDARY_DISPLAY)
    }

    fun changeActivityOrientation(orientationOption: Int) {
        val activity = context as? Activity ?: return
        IntSetting.ORIENTATION_OPTION.int = orientationOption
        SettingsFile.saveCurrentGameSetting(IntSetting.ORIENTATION_OPTION)
        activity.requestedOrientation = orientationOption
    }

    fun toggleScreenUpright() {
        val uprightBoolean = BooleanSetting.UPRIGHT_SCREEN.boolean
        BooleanSetting.UPRIGHT_SCREEN.boolean = !uprightBoolean
        SettingsFile.saveCurrentGameSetting(BooleanSetting.UPRIGHT_SCREEN)
        NativeLibrary.reloadSettings()
        NativeLibrary.updateFramebuffer(NativeLibrary.isPortraitMode())
    }
}
