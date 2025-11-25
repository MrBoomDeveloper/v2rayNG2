package com.mrboomdev.v2rayng2.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.exclude(
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) = PaddingValues(
    start = if(!start) calculateLeftPadding(LocalLayoutDirection.current) else 0.dp,
    top = if(!top) calculateTopPadding() else 0.dp,
    end = if(!end) calculateRightPadding(LocalLayoutDirection.current) else 0.dp,
    bottom = if(!bottom) calculateBottomPadding() else 0.dp
)

@Composable
fun PaddingValues.only(
    start: Boolean = false,
    top: Boolean = false,
    end: Boolean = false,
    bottom: Boolean = false
) = PaddingValues(
    start = if(start) calculateLeftPadding(LocalLayoutDirection.current) else 0.dp,
    top = if(top) calculateTopPadding() else 0.dp,
    end = if(end) calculateRightPadding(LocalLayoutDirection.current) else 0.dp,
    bottom = if(bottom) calculateBottomPadding() else 0.dp
)

@Composable
fun PaddingValues.onlyHorizontal() = PaddingValues(
    start = calculateLeftPadding(LocalLayoutDirection.current),
    end = calculateRightPadding(LocalLayoutDirection.current)
)

@Composable
fun PaddingValues.onlyVertical() = PaddingValues(
    top = calculateTopPadding(),
    bottom = calculateBottomPadding()
)

@get:Composable
val WindowInsets.left get() = with(LocalDensity.current) {
    getLeft(this, LocalLayoutDirection.current).toDp()
}

@get:Composable
val WindowInsets.right get() = with(LocalDensity.current) {
    getRight(this, LocalLayoutDirection.current).toDp()
}

@get:Composable
val WindowInsets.top get() = with(LocalDensity.current) {
    getTop(LocalDensity.current).toDp()
}

@get:Composable
val WindowInsets.bottom get() = with(LocalDensity.current) {
    getBottom(LocalDensity.current).toDp()
}