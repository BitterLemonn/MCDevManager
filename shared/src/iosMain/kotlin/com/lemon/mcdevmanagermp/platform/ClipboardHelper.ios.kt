package com.lemon.mcdevmanagermp.platform

import platform.UIKit.UIPasteboard

actual fun copyTextToClipboard(text: String) {
    UIPasteboard.generalPasteboard().string = text
}
