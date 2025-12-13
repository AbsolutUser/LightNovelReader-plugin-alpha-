package com.example.plugin

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import io.nightfish.lightnovelreader.api.plugin.LightNovelReaderPlugin
import io.nightfish.lightnovelreader.api.plugin.Plugin
import io.nightfish.lightnovelreader.api.ui.components.SettingsClickableEntry
import io.nightfish.lightnovelreader.api.ui.components.SettingsSwitchEntry
import io.nightfish.lightnovelreader.api.userdata.UserDataRepositoryApi

@Suppress("unused")
@Plugin(
    version = 1,
    apiVersion = 1,
    name = "Meionovels",
    versionName = "0.1.0",
    author = "Shaif Afriza",
    description = "Meionovels data source",
    updateUrl = ""
)
class ExamplePlugin(
    val userDataRepositoryApi: UserDataRepositoryApi
) : LightNovelReaderPlugin {

    override fun onLoad() {
        Log.i("MeionovelsPlugin", "Plugin loaded successfully")
    }

    @Composable
    override fun PageContent(paddingValues: PaddingValues) {
        val context = LocalContext.current

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val checked by userDataRepositoryApi
                .booleanUserData("TestBooleanUserData")
                .getFlowWithDefault(true)
                .collectAsState(true)

            SettingsSwitchEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                title = "Test Option",
                description = "Toggle example option",
                checked = checked,
                booleanUserData = userDataRepositoryApi.booleanUserData("TestBooleanUserData")
            )

            SettingsClickableEntry(
                modifier = Modifier.background(colorScheme.surfaceContainer),
                title = "Test Click",
                description = "Example action",
                onClick = {
                    Toast.makeText(context, "Clicked", Toast.LENGTH_LONG).show()
                }
            )
        }
    }
}
