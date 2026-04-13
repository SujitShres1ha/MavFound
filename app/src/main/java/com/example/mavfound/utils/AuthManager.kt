package com.example.mavfound.utils

import android.content.ContentValues
import android.content.Context
import com.example.mavfound.database.DatabaseHelper
import com.example.mavfound.models.User
import java.security.MessageDigest

class AuthManager(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun registerUser(name: String, email: String, plainPassword: String): Boolean {
        val db = dbHelper.writableDatabase
        val hashedPassword = hashPassword(plainPassword)

        val values = ContentValues().apply {
            put("name", name)
            put("email", email)
            put("password_hash", hashedPassword)
            put("is_admin", 0) // 0 for regular user
            put("is_active", 1) // 1 for active
        }

        // db.insert returns -1 if there's an error (e.g., UNIQUE constraint failure on email)
        val result = db.insert(DatabaseHelper.TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    /**
     * Attempts to log in a user.
     * Returns a User object if successful, or null if credentials fail or the account is banned.
     */
    fun loginUser(email: String, plainPassword: String): User? {
        val db = dbHelper.readableDatabase
        val hashedPassword = hashPassword(plainPassword)
        var loggedInUser: User? = null

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null, // Select all columns
            "email = ? AND password_hash = ?",
            arrayOf(email, hashedPassword),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val isActive = cursor.getInt(cursor.getColumnIndexOrThrow("is_active")) == 1

            if (isActive) {
                loggedInUser = User(
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    name = cursor.getString(cursor.getColumnIndexOrThrow("name")),
                    email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    passwordHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash")),
                    isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow("is_admin")) == 1,
                    isActive = true
                )
            }
        }

        cursor.close()
        db.close()
        return loggedInUser
    }
}