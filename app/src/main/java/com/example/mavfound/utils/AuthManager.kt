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

    fun getUserById(userId: Int): User? {
        val db = dbHelper.readableDatabase
        var user: User? = null

        val cursor = db.query(
            DatabaseHelper.TABLE_USERS,
            null,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            user = User(
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)),
                passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)),
                isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ADMIN)) == 1,
                isActive = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_IS_ACTIVE)) == 1
            )
        }

        cursor.close()
        db.close()
        return user
    }

    fun updateUserProfile(
        userId: Int,
        name: String,
        email: String,
        newPassword: String,
        currentPassword: String
    ): UpdateProfileResult {
        val db = dbHelper.writableDatabase
        val currentUser = getUserById(userId) ?: return UpdateProfileResult.UserNotFound

        if (hashPassword(currentPassword) != currentUser.passwordHash) {
            return UpdateProfileResult.InvalidCurrentPassword
        }

        val emailCursor = db.query(
            DatabaseHelper.TABLE_USERS,
            arrayOf(DatabaseHelper.COLUMN_USER_ID),
            "${DatabaseHelper.COLUMN_USER_EMAIL} = ? AND ${DatabaseHelper.COLUMN_USER_ID} != ?",
            arrayOf(email, userId.toString()),
            null, null, null
        )
        emailCursor.use {
            if (it.moveToFirst()) {
                db.close()
                return UpdateProfileResult.EmailAlreadyUsed
            }
        }

        val values = ContentValues().apply {
            put(DatabaseHelper.COLUMN_USER_NAME, name)
            put(DatabaseHelper.COLUMN_USER_EMAIL, email)
            put(
                DatabaseHelper.COLUMN_USER_PASSWORD,
                if (newPassword.isBlank()) currentUser.passwordHash else hashPassword(newPassword)
            )
        }

        val updated = db.update(
            DatabaseHelper.TABLE_USERS,
            values,
            "${DatabaseHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId.toString())
        )
        db.close()

        return if (updated > 0) UpdateProfileResult.Success else UpdateProfileResult.UpdateFailed
    }

    sealed class UpdateProfileResult {
        data object Success : UpdateProfileResult()
        data object InvalidCurrentPassword : UpdateProfileResult()
        data object EmailAlreadyUsed : UpdateProfileResult()
        data object UserNotFound : UpdateProfileResult()
        data object UpdateFailed : UpdateProfileResult()
    }
}
