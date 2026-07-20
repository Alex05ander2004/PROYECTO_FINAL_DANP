package com.example.refood.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun QuantityStepper(
    quantity: Int,
    modifier: Modifier = Modifier,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        OutlinedIconButton(
            onClick = onDecrement,
            modifier = Modifier.size(30.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Filled.Remove, contentDescription = "Disminuir cantidad", modifier = Modifier.size(16.dp))
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(34.dp)
        )
        OutlinedIconButton(
            onClick = onIncrement,
            modifier = Modifier.size(30.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            colors = IconButtonDefaults.outlinedIconButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Aumentar cantidad", modifier = Modifier.size(16.dp))
        }
    }
}
