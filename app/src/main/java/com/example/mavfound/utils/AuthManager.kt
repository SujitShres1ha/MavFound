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
            put(DatabaseHelper.COLUMN_USER_NAME, name)
            put(DatabaseHelper.COLUMN_USER_EMAIL, email)
            put(DatabaseHelper.COLUMN_USER_PASSWORD, hashedPassword)
            put(DatabaseHelper.COLUMN_USER_IS_ADMIN, 0) // 0 for regular user
            put(DatabaseHelper.COLUMN_USER_IS_ACTIVE, 1) // 1 for active
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
            "${DatabaseHelper.COLUMN_USER_EMAIL} = ? AND ${DatabaseHelper.COLUMN_USER_PASSWORD} = ?",
            arrayOf(email, hashedPassword),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            val isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ACTIVE)) == 1

            if (isActive) {
                loggedInUser = User(
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                    email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
                    passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)),
                    isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ADMIN)) == 1,
                    isActive = true
                )
            }
        }

        cursor.close()
        db.close()
        return loggedInUser
    }
}
