package com.lemon.mcdevmanager.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.ui.theme.AppTheme
import com.lemon.mcdevmanager.ui.theme.TextBlack
import com.lemon.mcdevmanager.ui.theme.TextWhite

@Composable
fun BottomNameInput(
    hint: String? = null,
    label: String,
    onConfirm: (String) -> Unit,
    isShow: Boolean
) {
    var input by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isShow){
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = TextBlack.copy(alpha = 0.4f))
            )
        }
        AnimatedVisibility(
            visible = isShow,
            modifier = Modifier.align(Alignment.BottomCenter).imePadding(),
            enter = slideIn { fullSize -> IntOffset(0, fullSize.height) },
            exit = slideOut { fullSize -> IntOffset(0, fullSize.height) }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card)
            ) {
                Column(Modifier.fillMaxWidth()) {
                    if (!hint.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = hint,
                            modifier = Modifier.fillMaxWidth(),
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            color = AppTheme.colors.hintColor
                        )
                    }
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = TextFieldDefaults.colors(
                            cursorColor = AppTheme.colors.primaryColor,
                            focusedIndicatorColor = AppTheme.colors.primaryColor,
                            unfocusedIndicatorColor = AppTheme.colors.secondaryColor,
                            focusedTextColor = AppTheme.colors.textColor,
                            unfocusedTextColor = AppTheme.colors.textColor,
                            unfocusedContainerColor = AppTheme.colors.card,
                            focusedContainerColor = AppTheme.colors.card,
                            focusedLabelColor = AppTheme.colors.primaryColor,
                            unfocusedLabelColor = AppTheme.colors.hintColor
                        ),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Ascii),
                        label = {
                            Text(
                                text = label,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    )
                    Button(
                        onClick = { onConfirm(input) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.primaryColor,
                            contentColor = AppTheme.colors.textColor
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(text = "确定", modifier = Modifier.padding(4.dp), color = TextWhite)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun PreviewBottomNameInput() {
    BottomNameInput(
        hint = "输入助记名称",
        label = "名称",
        onConfirm = {},
        isShow = true
    )
}