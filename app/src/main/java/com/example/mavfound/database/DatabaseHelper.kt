package com.example.mavfound.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mavfound.models.Listing

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MavFound.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_USERS = "Users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password_hash"
        const val COLUMN_USER_IS_ADMIN = "is_admin"
        const val COLUMN_USER_IS_ACTIVE = "is_active"

        const val TABLE_LISTINGS = "Listings"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USER_NAME TEXT,
                $COLUMN_USER_EMAIL TEXT UNIQUE,
                $COLUMN_USER_PASSWORD TEXT,
                $COLUMN_USER_IS_ADMIN INTEGER DEFAULT 0,
                $COLUMN_USER_IS_ACTIVE INTEGER DEFAULT 1
            )
        """)

        db.execSQL("""
            CREATE TABLE $TABLE_LISTINGS (
                listing_id INTEGER PRIMARY KEY AUTOINCREMENT,
                lister_id INTEGER,
                title TEXT,
                description TEXT,
                location TEXT,
                date_time TEXT,
                image_path TEXT,
                reward_amount REAL,
                security_question TEXT,
                security_answer TEXT,
                status TEXT DEFAULT 'Available'
            )
        """)

        db.execSQL("""
            CREATE INDEX idx_listings_user ON $TABLE_LISTINGS(lister_id)
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            if (!isColumnExists(db, TABLE_USERS, COLUMN_USER_IS_ACTIVE)) {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_IS_ACTIVE INTEGER DEFAULT 1")
            }
        }
    }

    private fun isColumnExists(db: SQLiteDatabase, table: String, column: String): Boolean {
        val cursor = db.rawQuery("PRAGMA table_info($table)", null)
        cursor.use {
            while (it.moveToNext()) {
                val nameIndex = it.getColumnIndex("name")
                if (nameIndex != -1 && it.getString(nameIndex) == column) {
                    return true
                }
            }
        }
        return false
    }

    fun insertListing(listing: Listing): Long {
        return writableDatabase.use { db ->
            val cv = ContentValues().apply {
                put("lister_id", listing.listerId)
                put("title", listing.title)
                put("description", listing.description)
                put("location", listing.location)
                put("date_time", listing.dateTime)
                put("image_path", listing.imagePath)
                put("reward_amount", listing.rewardAmount)
                put("security_question", listing.securityQuestion)
                put("security_answer", listing.securityAnswer)
                put("status", listing.status)
            }
            db.insert(TABLE_LISTINGS, null, cv)
        }
    }

    fun getMyListings(userId: Int): List<Listing> {
        val list = mutableListOf<Listing>()

        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_LISTINGS WHERE lister_id=?",
            arrayOf(userId.toString())
        ).use { c ->
            while (c.moveToNext()) {
                list.add(
                    Listing(
                        c.getInt(c.getColumnIndexOrThrow("listing_id")),
                        c.getInt(c.getColumnIndexOrThrow("lister_id")),
                        c.getString(c.getColumnIndexOrThrow("title")),
                        c.getString(c.getColumnIndexOrThrow("description")),
                        c.getString(c.getColumnIndexOrThrow("location")),
                        c.getString(c.getColumnIndexOrThrow("date_time")),
                        c.getString(c.getColumnIndexOrThrow("image_path")),
                        c.getDouble(c.getColumnIndexOrThrow("reward_amount")),
                        c.getString(c.getColumnIndexOrThrow("security_question")),
                        c.getString(c.getColumnIndexOrThrow("security_answer")),
                        c.getString(c.getColumnIndexOrThrow("status"))
                    )
                )
            }
        }
        return list
    }

    fun getListingById(id: Int): Listing? {
        return readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_LISTINGS WHERE listing_id=?",
            arrayOf(id.toString())
        ).use { c ->
            if (c.moveToFirst()) {
                Listing(
                    c.getInt(0),
                    c.getInt(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getDouble(7),
                    c.getString(8),
                    c.getString(9),
                    c.getString(10)
                )
            } else null
        }
    }

    fun deleteListing(id: Int) =
        writableDatabase.delete(TABLE_LISTINGS, "listing_id=?", arrayOf(id.toString()))
}
