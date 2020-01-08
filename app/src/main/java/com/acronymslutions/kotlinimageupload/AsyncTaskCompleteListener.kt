package com.acronymslutions.kotlinimageupload

interface AsyncTaskCompleteListener {
    fun onTaskCompleted(response: String, serviceCode: Int)
}