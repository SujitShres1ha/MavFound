package com.example.mavfound.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MavFound.db"
        private const val DATABASE_VERSION = 1

        // Table Names
        const val TABLE_USERS = "Users"
        const val TABLE_LISTINGS = "Listings"
        const val TABLE_CLAIMS = "Claims"
        const val TABLE_PAYMENTS = "Payments"
        const val TABLE_REPORTS = "Reports"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Create Users Table
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                is_admin INTEGER DEFAULT 0,
                is_active INTEGER DEFAULT 1
            )
        """.trimIndent()

        // 2. Create Listings Table
        val createListingsTable = """
            CREATE TABLE $TABLE_LISTINGS (
                listing_id INTEGER PRIMARY KEY AUTOINCREMENT,
                lister_id INTEGER,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                location TEXT NOT NULL,
                date_time TEXT NOT NULL,
                image_path TEXT,
                reward_amount REAL NOT NULL,
                security_question TEXT NOT NULL,
                security_answer TEXT NOT NULL,
                status TEXT DEFAULT 'Available',
                FOREIGN KEY(lister_id) REFERENCES $TABLE_USERS(user_id)
            )
        """.trimIndent()

        // 3. Create Claims Table
        val createClaimsTable = """
            CREATE TABLE $TABLE_CLAIMS (
                claim_id INTEGER PRIMARY KEY AUTOINCREMENT,
                listing_id INTEGER,
                searcher_id INTEGER,
                claim_date TEXT NOT NULL,
                exchange_code TEXT,
                status TEXT DEFAULT 'Pending',
                FOREIGN KEY(listing_id) REFERENCES $TABLE_LISTINGS(listing_id),
                FOREIGN KEY(searcher_id) REFERENCES $TABLE_USERS(user_id)
            )
        """.trimIndent()

        // 4. Create Payments Table
        val createPaymentsTable = """
            CREATE TABLE $TABLE_PAYMENTS (
                payment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                claim_id INTEGER,
                amount REAL NOT NULL,
                payment_date TEXT NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY(claim_id) REFERENCES $TABLE_CLAIMS(claim_id)
            )
        """.trimIndent()

        // 5. Create Reports Table
        val createReportsTable = """
            CREATE TABLE $TABLE_REPORTS (
                report_id INTEGER PRIMARY KEY AUTOINCREMENT,
                reporter_id INTEGER,
                target_user_id INTEGER,
                listing_id INTEGER,
                reason TEXT NOT NULL,
                status TEXT DEFAULT 'Pending',
                FOREIGN KEY(reporter_id) REFERENCES $TABLE_USERS(user_id),
                FOREIGN KEY(target_user_id) REFERENCES $TABLE_USERS(user_id),
                FOREIGN KEY(listing_id) REFERENCES $TABLE_LISTINGS(listing_id)
            )
        """.trimIndent()

        // Execute the SQL
        db.execSQL(createUsersTable)
        db.execSQL(createListingsTable)
        db.execSQL(createClaimsTable)
        db.execSQL(createPaymentsTable)
        db.execSQL(createReportsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop older tables if they existed
        db.execSQL("DROP TABLE IF EXISTS $TABLE_REPORTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLAIMS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LISTINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        // Create tables again
        onCreate(db)
    }
}