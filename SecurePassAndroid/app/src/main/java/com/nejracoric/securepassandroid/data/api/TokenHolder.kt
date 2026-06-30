package com.nejracoric.securepassandroid.data.api

object TokenHolder {
    @Volatile
    var token: String? = null
}
