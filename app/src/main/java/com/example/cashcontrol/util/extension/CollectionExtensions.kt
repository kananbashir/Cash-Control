package com.example.cashcontrol.util.extension

fun <E> List<E>.concatenateCategories (): String {
    var concatenatedText = ""
    for (i in this.indices) {
        if (this.size-1 != i) {
            concatenatedText += "- ${this[i]}\n"
        } else {
            concatenatedText += "- ${this[i]}"
        }
    }

    return concatenatedText
}