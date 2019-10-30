package com.bindlish.colourmemory.utils

fun executeInThread(function: () -> Unit) {
    Thread(function).start()
}