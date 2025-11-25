package com.mrboomdev.v2rayng2.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrboomdev.v2rayng2.ui.FontFamilies

@Composable
fun InfoBox(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(64.dp),
    icon: Painter? = null,
    title: String,
    message: String,
    actions: @Composable InfoBoxActionsScope.() -> Unit = {}
) {
    Box(
        modifier = modifier.padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon?.also {
                Icon(
                    modifier = Modifier
                        .size(112.dp)
                        .padding(bottom = 8.dp),
                    painter = it,
                    contentDescription = null
                )
            }

            Text(
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = FontFamilies.googleSansFlex,
                textAlign = TextAlign.Center,
                text = title
            )

            Text(
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamilies.googleSansFlex,
                textAlign = TextAlign.Center,
                text = message
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                actions(InfoBoxActionsScope)
            }
        }
    }
}

object InfoBoxActionsScope {
    @Composable
    fun action(text: String, onClick: () -> Unit) {
        Button(
            onClick = onClick
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = text
            )
        }
    }
}