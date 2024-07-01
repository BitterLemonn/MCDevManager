package com.lemon.mcdevmanager.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.mcdevmanager.R
import com.lemon.mcdevmanager.ui.theme.AppTheme

@Composable
fun SearchBarWidget(
    searchStr: String = "",
    onSearchStrChange: (String) -> Unit = {},
    onSearch: () -> Unit = {}
) {
    OutlinedTextField(
        value = searchStr,
        onValueChange = onSearchStrChange,
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        textStyle = TextStyle(fontSize = 16.sp),
        colors = TextFieldDefaults.textFieldColors(
            textColor = AppTheme.colors.textColor,
            backgroundColor = Color.Transparent,
            focusedIndicatorColor = AppTheme.colors.primaryColor,
            unfocusedIndicatorColor = AppTheme.colors.primaryColor
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        leadingIcon = {
            Image(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "search",
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.lighting(
                    add = Color.Transparent,
                    multiply = AppTheme.colors.textColor
                )
            )
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(40.dp)
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = rememberRipple(),
                        onClick = onSearch
                    )
            ) {
                Text(
                    text = "搜索",
                    color = AppTheme.colors.primaryColor,
                    fontSize = 16.sp,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch() }
        )
    )

}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun SearchBarWidgetPreview() {
    var searchStr by remember { mutableStateOf("test") }
    SearchBarWidget(
        searchStr = searchStr,
        onSearchStrChange = { searchStr = it }
    )
}