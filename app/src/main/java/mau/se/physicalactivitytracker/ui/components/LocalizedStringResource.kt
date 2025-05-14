package mau.se.physicalactivitytracker.ui.components

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale


@SuppressLint("LocalContextConfigurationRead")
@Composable
fun localizedStringResource(
    @StringRes resId: Int,
    languageCode: String,
    vararg formatArgs: Any
): String {
    val context = LocalContext.current
    return remember(resId, languageCode) {
        val locale = Locale(languageCode)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        val localizedContext = context.createConfigurationContext(config)
        if (formatArgs.isEmpty()) {
            localizedContext.resources.getString(resId)
        } else {
            localizedContext.resources.getString(resId, *formatArgs)
        }
    }
}