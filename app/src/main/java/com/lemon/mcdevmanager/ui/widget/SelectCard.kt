package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun SelectCard(
    leftName: String,
    rightName: String,
    onClickLeft: () -> Unit = {},
    onClickRight: () -> Unit = {}
) {
    var nowSelected by remember{ mutableIntStateOf(0) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.card
        ),
        shape = RoundedCornerShape(8.dp)
    ) {

    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun SelectCardPreview() {
    SelectCard(
        leftName = "PE",
        rightName = "PC"
    )
}