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
        private const val DATABASE_VERSION = 3 // Incremented version for the new table

        const val TABLE_USERS = "Users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USER_NAME = "name"
        const val COLUMN_USER_EMAIL = "email"
        const val COLUMN_USER_PASSWORD = "password_hash"
        const val COLUMN_USER_IS_ADMIN = "is_admin"
        const val COLUMN_USER_IS_ACTIVE = "is_active"

        const val TABLE_LISTINGS = "Listings"

        // NEW: Claims table constants
        const val TABLE_CLAIMS = "claims"
        const val COLUMN_CLAIM_ID = "claim_id"
        const val COLUMN_CLAIM_LISTING_ID = "listing_id"
        const val COLUMN_CLAIMANT_ID = "claimant_id"
        const val COLUMN_CLAIM_DESCRIPTION = "description_provided"
        const val COLUMN_CLAIM_STATUS = "status"
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

        // NEW: Create the Claims table for human-mediated verification
        db.execSQL("""
            CREATE TABLE $TABLE_CLAIMS (
                $COLUMN_CLAIM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CLAIM_LISTING_ID INTEGER,
                $COLUMN_CLAIMANT_ID INTEGER,
                $COLUMN_CLAIM_DESCRIPTION TEXT,
                $COLUMN_CLAIM_STATUS TEXT DEFAULT 'Pending',
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_CLAIM_LISTING_ID) REFERENCES $TABLE_LISTINGS(listing_id),
                FOREIGN KEY($COLUMN_CLAIMANT_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)

        db.execSQL("CREATE INDEX idx_listings_user ON $TABLE_LISTINGS(lister_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            if (!isColumnExists(db, TABLE_USERS, COLUMN_USER_IS_ACTIVE)) {
                db.execSQL("ALTER TABLE $TABLE_USERS ADD COLUMN $COLUMN_USER_IS_ACTIVE INTEGER DEFAULT 1")
            }
        }
        // Handle upgrade to version 3 (adding claims table if it doesn't exist)
        if (oldVersion < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_CLAIMS (
                    $COLUMN_CLAIM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_CLAIM_LISTING_ID INTEGER,
                    $COLUMN_CLAIMANT_ID INTEGER,
                    $COLUMN_CLAIM_DESCRIPTION TEXT,
                    $COLUMN_CLAIM_STATUS TEXT DEFAULT 'Pending',
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY($COLUMN_CLAIM_LISTING_ID) REFERENCES $TABLE_LISTINGS(listing_id),
                    FOREIGN KEY($COLUMN_CLAIMANT_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """)
        }
    }

    // --- Listing Methods ---

    fun insertListing(listing: Listing): Long {
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
        return writableDatabase.insert(TABLE_LISTINGS, null, cv)
    }

    fun getAllAvailableListings(): List<Listing> {
        val list = mutableListOf<Listing>()
        readableDatabase.rawQuery("SELECT * FROM $TABLE_LISTINGS WHERE status = 'Available' ORDER BY listing_id DESC", null).use { cursor ->
            if (cursor.moveToFirst()) {
                do {
                    list.add(mapCursorToListing(cursor))
                } while (cursor.moveToNext())
            }
        }
        return list
    }

    // --- NEW: Handshake/Claim Methods ---

    /**
     * Inserts a new claim request for the poster to review.
     */
    fun insertClaim(listingId: Int, claimantId: Int, answer: String): Long {
        val values = ContentValues().apply {
            put(COLUMN_CLAIM_LISTING_ID, listingId)
            put(COLUMN_CLAIMANT_ID, claimantId)
            put(COLUMN_CLAIM_DESCRIPTION, answer)
            put(COLUMN_CLAIM_STATUS, "Pending")
        }
        return writableDatabase.insert(TABLE_CLAIMS, null, values)
    }

    /**
     * Checks how many pending claims a listing has to show on the Finder's dashboard.
     */
    fun getClaimCountForListing(listingId: Int): Int {
        val cursor = readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_CLAIMS WHERE $COLUMN_CLAIM_LISTING_ID = ? AND $COLUMN_CLAIM_STATUS = 'Pending'",
            arrayOf(listingId.toString())
        )
        var count = 0
        cursor.use {
            if (it.moveToFirst()) count = it.getInt(0)
        }
        return count
    }

    // --- Utility Methods ---

    private fun mapCursorToListing(c: android.database.Cursor): Listing {
        return Listing(
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
    }

    private fun isColumnExists(db: SQLiteDatabase, table: String, column: String): Boolean {
        db.rawQuery("PRAGMA table_info($table)", null).use {
            while (it.moveToNext()) {
                val nameIndex = it.getColumnIndex("name")
                if (nameIndex != -1 && it.getString(nameIndex) == column) return true
            }
        }
        return false
    }

    fun getMyListings(userId: Int): List<Listing> {
        val list = mutableListOf<Listing>()
        readableDatabase.rawQuery("SELECT * FROM $TABLE_LISTINGS WHERE lister_id=?", arrayOf(userId.toString())).use { c ->
            while (c.moveToNext()) list.add(mapCursorToListing(c))
        }
        return list
    }

    fun getListingById(id: Int): Listing? {
        readableDatabase.rawQuery("SELECT * FROM $TABLE_LISTINGS WHERE listing_id=?", arrayOf(id.toString())).use { c ->
            return if (c.moveToFirst()) mapCursorToListing(c) else null
        }
    }

    fun deleteListing(id: Int) = writableDatabase.delete(TABLE_LISTINGS, "listing_id=?", arrayOf(id.toString()))
}