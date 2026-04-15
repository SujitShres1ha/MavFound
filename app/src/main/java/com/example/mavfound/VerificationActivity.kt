package com.example.mavfound

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class VerificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verification)

        val etAnswer = findViewById<EditText>(R.id.etAnswer)
        val btnSubmitAnswer = findViewById<Button>(R.id.btnSubmitAnswer)
        val tvResult = findViewById<TextView>(R.id.tvResult)

        btnSubmitAnswer.setOnClickListener {
            val userAnswer = etAnswer.text.toString().trim()

            if (userAnswer.equals("blue", ignoreCase = true)) {
                tvResult.text = "Claim submitted successfully. Ownership verified."
            } else {
                tvResult.text = "Verification failed. Answer does not match."
            }
        }
    }
}

