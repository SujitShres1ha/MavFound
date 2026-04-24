package com.example.mavfound.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.mavfound.models.Listing
import com.example.mavfound.models.Order
import com.example.mavfound.models.User
import java.security.MessageDigest

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MavFound.db"
        private const val DATABASE_VERSION = 4

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

        const val TABLE_ORDERS = "orders"
        const val COLUMN_ORDER_DB_ID = "order_db_id"
        const val COLUMN_ORDER_ID = "order_id"
        const val COLUMN_ORDER_LISTING_ID = "listing_id"
        const val COLUMN_ORDER_BUYER_ID = "buyer_id"
        const val COLUMN_ORDER_LISTING_TITLE = "listing_title"
        const val COLUMN_ORDER_AMOUNT = "amount"
        const val COLUMN_ORDER_PAYMENT_DATE = "payment_date"
        const val COLUMN_ORDER_STATUS = "status"
        const val COLUMN_ORDER_HANDOFF_CODE = "handoff_code"

        private const val SEEDED_ADMIN_NAME = "System Admin"
        private const val SEEDED_ADMIN_EMAIL = "admin@mavfound.com"
        private const val SEEDED_ADMIN_PASSWORD = "mavfound123"
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

        db.execSQL("""
            CREATE TABLE $TABLE_ORDERS (
                $COLUMN_ORDER_DB_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_ORDER_ID TEXT UNIQUE,
                $COLUMN_ORDER_LISTING_ID INTEGER,
                $COLUMN_ORDER_BUYER_ID INTEGER,
                $COLUMN_ORDER_LISTING_TITLE TEXT,
                $COLUMN_ORDER_AMOUNT REAL,
                $COLUMN_ORDER_PAYMENT_DATE TEXT,
                $COLUMN_ORDER_STATUS TEXT DEFAULT 'Pending',
                $COLUMN_ORDER_HANDOFF_CODE TEXT,
                FOREIGN KEY($COLUMN_ORDER_LISTING_ID) REFERENCES $TABLE_LISTINGS(listing_id),
                FOREIGN KEY($COLUMN_ORDER_BUYER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
            )
        """)

        ensureSeedAdmin(db)
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
        if (oldVersion < 4) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_ORDERS (
                    $COLUMN_ORDER_DB_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COLUMN_ORDER_ID TEXT UNIQUE,
                    $COLUMN_ORDER_LISTING_ID INTEGER,
                    $COLUMN_ORDER_BUYER_ID INTEGER,
                    $COLUMN_ORDER_LISTING_TITLE TEXT,
                    $COLUMN_ORDER_AMOUNT REAL,
                    $COLUMN_ORDER_PAYMENT_DATE TEXT,
                    $COLUMN_ORDER_STATUS TEXT DEFAULT 'Pending',
                    $COLUMN_ORDER_HANDOFF_CODE TEXT,
                    FOREIGN KEY($COLUMN_ORDER_LISTING_ID) REFERENCES $TABLE_LISTINGS(listing_id),
                    FOREIGN KEY($COLUMN_ORDER_BUYER_ID) REFERENCES $TABLE_USERS($COLUMN_USER_ID)
                )
            """)
        }
        ensureSeedAdmin(db)
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

    fun getVisibleListings(): List<Listing> {
        val list = mutableListOf<Listing>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_LISTINGS WHERE status != 'Delivered' ORDER BY listing_id DESC",
            null
        ).use { cursor ->
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

    fun insertOrder(order: Order): Long {
        val values = ContentValues().apply {
            put(COLUMN_ORDER_ID, order.orderId)
            put(COLUMN_ORDER_LISTING_ID, order.listingId)
            put(COLUMN_ORDER_BUYER_ID, order.buyerId)
            put(COLUMN_ORDER_LISTING_TITLE, order.listingTitle)
            put(COLUMN_ORDER_AMOUNT, order.amount)
            put(COLUMN_ORDER_PAYMENT_DATE, order.paymentDate)
            put(COLUMN_ORDER_STATUS, order.status)
            put(COLUMN_ORDER_HANDOFF_CODE, order.handoffCode)
        }
        return writableDatabase.insert(TABLE_ORDERS, null, values)
    }

    fun getOrdersForUser(userId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_BUYER_ID=? ORDER BY $COLUMN_ORDER_DB_ID DESC",
            arrayOf(userId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                orders.add(mapCursorToOrder(cursor))
            }
        }
        return orders
    }

    fun getOrdersByStatus(status: String): List<Order> {
        val orders = mutableListOf<Order>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_STATUS=? ORDER BY $COLUMN_ORDER_DB_ID DESC",
            arrayOf(status)
        ).use { cursor ->
            while (cursor.moveToNext()) {
                orders.add(mapCursorToOrder(cursor))
            }
        }
        return orders
    }

    fun getOrdersForListing(listingId: Int): List<Order> {
        val orders = mutableListOf<Order>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_LISTING_ID=? ORDER BY $COLUMN_ORDER_DB_ID DESC",
            arrayOf(listingId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                orders.add(mapCursorToOrder(cursor))
            }
        }
        return orders
    }

    fun getOrderByOrderId(orderId: String): Order? {
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_ORDERS WHERE $COLUMN_ORDER_ID=?",
            arrayOf(orderId)
        ).use { cursor ->
            return if (cursor.moveToFirst()) mapCursorToOrder(cursor) else null
        }
    }

    fun getUserNameById(userId: Int): String? {
        readableDatabase.rawQuery(
            "SELECT $COLUMN_USER_NAME FROM $TABLE_USERS WHERE $COLUMN_USER_ID=?",
            arrayOf(userId.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getString(0) else null
        }
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_USERS ORDER BY $COLUMN_USER_ID DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                users.add(
                    User(
                        userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)),
                        name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)),
                        email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_EMAIL)),
                        passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD)),
                        isAdmin = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ADMIN)) == 1,
                        isActive = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_IS_ACTIVE)) == 1
                    )
                )
            }
        }
        return users
    }

    fun updateUserAdmin(userId: Int, name: String, email: String, isAdmin: Boolean, isActive: Boolean): Boolean {
        val duplicateCursor = readableDatabase.rawQuery(
            "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL=? AND $COLUMN_USER_ID!=?",
            arrayOf(email, userId.toString())
        )
        duplicateCursor.use {
            if (it.moveToFirst()) return false
        }

        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, name)
            put(COLUMN_USER_EMAIL, email)
            put(COLUMN_USER_IS_ADMIN, if (isAdmin) 1 else 0)
            put(COLUMN_USER_IS_ACTIVE, if (isActive) 1 else 0)
        }
        return writableDatabase.update(TABLE_USERS, values, "$COLUMN_USER_ID=?", arrayOf(userId.toString())) > 0
    }

    fun deleteUser(userId: Int): Int =
        writableDatabase.delete(TABLE_USERS, "$COLUMN_USER_ID=?", arrayOf(userId.toString()))

    fun getAllListings(): List<Listing> {
        val listings = mutableListOf<Listing>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_LISTINGS ORDER BY listing_id DESC",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                listings.add(mapCursorToListing(cursor))
            }
        }
        return listings
    }

    fun updateListingAdmin(listingId: Int, rewardAmount: Double, status: String): Boolean {
        val values = ContentValues().apply {
            put("reward_amount", rewardAmount)
            put("status", status)
        }
        return writableDatabase.update(TABLE_LISTINGS, values, "listing_id=?", arrayOf(listingId.toString())) > 0
    }

    fun updateOrderStatus(orderId: String, status: String): Int {
        val values = ContentValues().apply {
            put(COLUMN_ORDER_STATUS, status)
        }
        return writableDatabase.update(TABLE_ORDERS, values, "$COLUMN_ORDER_ID=?", arrayOf(orderId))
    }

    fun updateListingStatus(listingId: Int, status: String): Int {
        val values = ContentValues().apply {
            put("status", status)
        }
        return writableDatabase.update(TABLE_LISTINGS, values, "listing_id=?", arrayOf(listingId.toString()))
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

    private fun mapCursorToOrder(c: android.database.Cursor): Order {
        return Order(
            orderDbId = c.getInt(c.getColumnIndexOrThrow(COLUMN_ORDER_DB_ID)),
            orderId = c.getString(c.getColumnIndexOrThrow(COLUMN_ORDER_ID)),
            listingId = c.getInt(c.getColumnIndexOrThrow(COLUMN_ORDER_LISTING_ID)),
            buyerId = c.getInt(c.getColumnIndexOrThrow(COLUMN_ORDER_BUYER_ID)),
            listingTitle = c.getString(c.getColumnIndexOrThrow(COLUMN_ORDER_LISTING_TITLE)),
            amount = c.getDouble(c.getColumnIndexOrThrow(COLUMN_ORDER_AMOUNT)),
            paymentDate = c.getString(c.getColumnIndexOrThrow(COLUMN_ORDER_PAYMENT_DATE)),
            status = c.getString(c.getColumnIndexOrThrow(COLUMN_ORDER_STATUS)),
            handoffCode = c.getString(c.getColumnIndexOrThrow(COLUMN_ORDER_HANDOFF_CODE))
        )
    }

    private fun ensureSeedAdmin(db: SQLiteDatabase) {
        db.rawQuery(
            "SELECT $COLUMN_USER_ID FROM $TABLE_USERS WHERE $COLUMN_USER_EMAIL = ?",
            arrayOf(SEEDED_ADMIN_EMAIL)
        ).use { cursor ->
            if (cursor.moveToFirst()) return
        }

        val values = ContentValues().apply {
            put(COLUMN_USER_NAME, SEEDED_ADMIN_NAME)
            put(COLUMN_USER_EMAIL, SEEDED_ADMIN_EMAIL)
            put(COLUMN_USER_PASSWORD, hashPassword(SEEDED_ADMIN_PASSWORD))
            put(COLUMN_USER_IS_ADMIN, 1)
            put(COLUMN_USER_IS_ACTIVE, 1)
        }
        db.insert(TABLE_USERS, null, values)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun getMyListings(userId: Int): List<Listing> {
        val list = mutableListOf<Listing>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLE_LISTINGS WHERE lister_id=? AND status != 'Delivered'",
            arrayOf(userId.toString())
        ).use { c ->
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
