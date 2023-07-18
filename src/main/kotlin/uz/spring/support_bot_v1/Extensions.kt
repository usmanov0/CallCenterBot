package uz.spring.support_bot_v1

fun Boolean.runIfTrue(func: () -> Unit) {
    if (this) func()
}

fun Boolean.runIfFalse(func: () -> Unit) {
    if (!this) func()
}