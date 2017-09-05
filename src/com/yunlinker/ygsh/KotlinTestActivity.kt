package com.yunlinker.ygsh

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class KotlinTestActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_test)

        val button = findViewById(R.id.button) as Button
        button.setOnClickListener {
            Toast.makeText(this, """I'm
                |a
                |Kotlin
                |Activity""".trimMargin(), Toast.LENGTH_LONG).show()
        }
    }
}
