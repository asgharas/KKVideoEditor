package com.asgharas.kkvideoeditor

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform