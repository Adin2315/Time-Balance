package com.adindaapriliawahyupp_231111015.timebalance.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.adindaapriliawahyupp_231111015.timebalance.notification.CustomRingtone
import com.adindaapriliawahyupp_231111015.timebalance.notification.NotificationSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DBAdapter(context: Context) {

    companion object {
        private const val DATABASE_NAME = "timebalance.db"
        private const val DATABASE_VERSION = 1
        private const val TAG = "DBAdapter"

        // Table: users
        private const val TABLE_USERS = "users"
        private const val USER_ID = "_id"
        private const val USER_EMAIL = "email"
        private const val USER_PASSWORD = "password"
        private const val USER_USERNAME = "username"
        private const val USER_PHOTO = "photo"  // BLOB (optional)
        private const val USER_PHONE = "phone"
        private const val USER_BIO = "bio"

        private const val DATABASE_CREATE_USERS = """
            CREATE TABLE $TABLE_USERS (
                $USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $USER_EMAIL TEXT NOT NULL,
                $USER_PASSWORD TEXT NOT NULL,
                $USER_USERNAME TEXT NOT NULL,
                $USER_PHOTO BLOB,
                $USER_PHONE TEXT,
                $USER_BIO TEXT,
                created_at TEXT DEFAULT (datetime('now', 'localtime')),
                updated_at TEXT DEFAULT (datetime('now', 'localtime'))
            );
        """

        // Table: categories
        private const val TABLE_CATEGORIES = "categories"
        private const val CATEGORY_ID = "_id"
        private const val CATEGORY_NAME = "name"
        private const val CATEGORY_ICON = "icon"  // store as String resource name

        private const val DATABASE_CREATE_CATEGORIES = """
            CREATE TABLE $TABLE_CATEGORIES (
                $CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $CATEGORY_NAME TEXT NOT NULL UNIQUE,
                $CATEGORY_ICON TEXT
            );
        """

        // Table: schedules
        private const val TABLE_SCHEDULES = "schedules"
        private const val SCHEDULE_ID = "_id"
        private const val SCHEDULE_TITLE = "title"
        private const val SCHEDULE_DESCRIPTION = "description"
        private const val SCHEDULE_TIME = "time"
        private const val SCHEDULE_DATE = "date"
        private const val SCHEDULE_CATEGORY_ID = "category_id"  // foreign key
        private const val SCHEDULE_IS_STARRED = "is_starred"
        private const val SCHEDULE_IS_NOTIFICATION_ENABLED = "is_notification_enabled"
        private const val SCHEDULE_STATUS = "status"

        private const val DATABASE_CREATE_SCHEDULES = """
            CREATE TABLE $TABLE_SCHEDULES (
                $SCHEDULE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $SCHEDULE_TITLE TEXT NOT NULL,
                $SCHEDULE_DESCRIPTION TEXT,
                $SCHEDULE_TIME TEXT,
                $SCHEDULE_DATE TEXT,
                $SCHEDULE_CATEGORY_ID INTEGER,
                $SCHEDULE_IS_STARRED INTEGER DEFAULT 0,
                $SCHEDULE_IS_NOTIFICATION_ENABLED INTEGER DEFAULT 1,
                $SCHEDULE_STATUS TEXT DEFAULT 'pending',  -- 'pending', 'completed', 'missed'
                FOREIGN KEY($SCHEDULE_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($CATEGORY_ID)
            );
        """

        // Notification Settings Table
        private const val TABLE_NOTIFICATION_SETTINGS = "notification_settings"
        private const val COLUMN_ID = "id"
        private const val COLUMN_IS_ENABLED = "is_enabled"
        private const val COLUMN_IS_VIBRATION_ENABLED = "is_vibration_enabled"
        private const val COLUMN_IS_SOUND_ENABLED = "is_sound_enabled"
        private const val COLUMN_RINGTONE_TYPE = "ringtone_type"
        private const val COLUMN_CUSTOM_RINGTONE_URI = "custom_ringtone_uri"
        private const val COLUMN_VOLUME = "volume"

        private const val DATABASE_CREATE_NOTIFICATION_SETTINGS = """
            CREATE TABLE $TABLE_NOTIFICATION_SETTINGS (
                $COLUMN_ID INTEGER PRIMARY KEY,
                $COLUMN_IS_ENABLED INTEGER DEFAULT 1,
                $COLUMN_IS_VIBRATION_ENABLED INTEGER DEFAULT 1,
                $COLUMN_IS_SOUND_ENABLED INTEGER DEFAULT 1,
                $COLUMN_RINGTONE_TYPE TEXT DEFAULT 'default',
                $COLUMN_CUSTOM_RINGTONE_URI TEXT DEFAULT '',
                $COLUMN_VOLUME INTEGER DEFAULT 50
            );
        """

        // Custom Ringtones Table
        private const val TABLE_CUSTOM_RINGTONES = "custom_ringtones"
        private const val COLUMN_RINGTONE_ID = "id"
        private const val COLUMN_RINGTONE_NAME = "name"
        private const val COLUMN_RINGTONE_URI = "uri"
        private const val COLUMN_DATE_ADDED = "date_added"

        private const val DATABASE_CREATE_CUSTOM_RINGTONES = """
            CREATE TABLE $TABLE_CUSTOM_RINGTONES (
                $COLUMN_RINGTONE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_RINGTONE_NAME TEXT NOT NULL,
                $COLUMN_RINGTONE_URI TEXT NOT NULL,
                $COLUMN_DATE_ADDED INTEGER NOT NULL
            );
        """
    }

    private val dbHelper = DatabaseHelper(context)
    private lateinit var db: SQLiteDatabase

    private inner class DatabaseHelper(ctx: Context) :
        SQLiteOpenHelper(ctx, DATABASE_NAME, null, DATABASE_VERSION) {

        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(DATABASE_CREATE_USERS)
            db.execSQL(DATABASE_CREATE_CATEGORIES)
            db.execSQL(DATABASE_CREATE_SCHEDULES)
            db.execSQL(DATABASE_CREATE_NOTIFICATION_SETTINGS)
            db.execSQL(DATABASE_CREATE_CUSTOM_RINGTONES)

            // Dummy Users
            db.execSQL("INSERT INTO $TABLE_USERS ($USER_EMAIL, $USER_USERNAME, $USER_PASSWORD) VALUES ('admin@timebalance.com', 'admin', 'admin123');")


            // Create 5 dummy categories
            db.execSQL(
                ((("INSERT INTO $TABLE_CATEGORIES").toString() + " (" + CATEGORY_NAME).toString() + ", " + CATEGORY_ICON).toString() + ") VALUES " +
                        "('Work', 'ic_work'), " +
                        "('Meals', 'ic_meals'), " +
                        "('Study', 'ic_study'), " +
                        "('Exercise', 'ic_exercise'), " +
                        "('Personal', 'ic_personal');"
            )


// Get current date and time as base
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val today = dateFormat.format(Date(now))


// Breakfast (7:00)
            db.execSQL(
                ((((((((("INSERT INTO $TABLE_SCHEDULES").toString() + " (" +
                        SCHEDULE_TITLE).toString() + ", " +
                        SCHEDULE_DESCRIPTION).toString() + ", " +
                        SCHEDULE_TIME).toString() + ", " +
                        SCHEDULE_DATE).toString() + ", " +
                        SCHEDULE_CATEGORY_ID).toString() + ", " +
                        SCHEDULE_IS_STARRED).toString() + ", " +
                        SCHEDULE_IS_NOTIFICATION_ENABLED).toString() + ", " +
                        SCHEDULE_STATUS).toString() + ") VALUES " +
                        "('Breakfast', 'Start your day with a healthy meal', '07:00', '" + today + "', 2, 0, 1, 'pending');"
            )


// Morning Work (9:00)
            db.execSQL(
                ((((((((("INSERT INTO $TABLE_SCHEDULES").toString() + " (" +
                        SCHEDULE_TITLE).toString() + ", " +
                        SCHEDULE_DESCRIPTION).toString() + ", " +
                        SCHEDULE_TIME).toString() + ", " +
                        SCHEDULE_DATE).toString() + ", " +
                        SCHEDULE_CATEGORY_ID).toString() + ", " +
                        SCHEDULE_IS_STARRED).toString() + ", " +
                        SCHEDULE_IS_NOTIFICATION_ENABLED).toString() + ", " +
                        SCHEDULE_STATUS).toString() + ") VALUES " +
                        "('Work Session', 'Morning work tasks', '09:00', '" + today + "', 1, 1, 1, 'pending');"
            )


// Lunch (12:30)
            db.execSQL(
                ((((((((("INSERT INTO $TABLE_SCHEDULES").toString() + " (" +
                        SCHEDULE_TITLE).toString() + ", " +
                        SCHEDULE_DESCRIPTION).toString() + ", " +
                        SCHEDULE_TIME).toString() + ", " +
                        SCHEDULE_DATE).toString() + ", " +
                        SCHEDULE_CATEGORY_ID).toString() + ", " +
                        SCHEDULE_IS_STARRED).toString() + ", " +
                        SCHEDULE_IS_NOTIFICATION_ENABLED).toString() + ", " +
                        SCHEDULE_STATUS).toString() + ") VALUES " +
                        "('Lunch', 'Midday meal break', '12:30', '" + today + "', 2, 0, 1, 'pending');"
            )


// Afternoon Work (14:00)
            db.execSQL(
                ((((((((("INSERT INTO $TABLE_SCHEDULES").toString() + " (" +
                        SCHEDULE_TITLE).toString() + ", " +
                        SCHEDULE_DESCRIPTION).toString() + ", " +
                        SCHEDULE_TIME).toString() + ", " +
                        SCHEDULE_DATE).toString() + ", " +
                        SCHEDULE_CATEGORY_ID).toString() + ", " +
                        SCHEDULE_IS_STARRED).toString() + ", " +
                        SCHEDULE_IS_NOTIFICATION_ENABLED).toString() + ", " +
                        SCHEDULE_STATUS).toString() + ") VALUES " +
                        "('Work Session', 'Afternoon work tasks', '14:00', '" + today + "', 1, 1, 1, 'pending');"
            )


// Dinner (19:00)
            db.execSQL(
                ((((((((("INSERT INTO $TABLE_SCHEDULES").toString() + " (" +
                        SCHEDULE_TITLE).toString() + ", " +
                        SCHEDULE_DESCRIPTION).toString() + ", " +
                        SCHEDULE_TIME).toString() + ", " +
                        SCHEDULE_DATE).toString() + ", " +
                        SCHEDULE_CATEGORY_ID).toString() + ", " +
                        SCHEDULE_IS_STARRED).toString() + ", " +
                        SCHEDULE_IS_NOTIFICATION_ENABLED).toString() + ", " +
                        SCHEDULE_STATUS).toString() + ") VALUES " +
                        "('Dinner', 'Evening meal', '19:00', '" + today + "', 2, 0, 1, 'pending');"
            )

            val defaultSettings = ContentValues().apply {
                put(COLUMN_ID, 1)
                put(COLUMN_IS_ENABLED, 1)
                put(COLUMN_IS_VIBRATION_ENABLED, 1)
                put(COLUMN_IS_SOUND_ENABLED, 1)
                put(COLUMN_RINGTONE_TYPE, "default")
                put(COLUMN_CUSTOM_RINGTONE_URI, "")
                put(COLUMN_VOLUME, 50)
            }
            db.insert(TABLE_NOTIFICATION_SETTINGS, null, defaultSettings)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            Log.w(TAG, "Upgrading database from version $oldVersion to $newVersion")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATION_SETTINGS")
            db.execSQL("DROP TABLE IF EXISTS $TABLE_CUSTOM_RINGTONES")
            onCreate(db)
        }
    }

    fun open() {
        db = dbHelper.writableDatabase
    }

    fun close() {
        dbHelper.close()
    }

    // ===== USER =====
    fun registerUser(email: String, username: String, password: String): Boolean {
        return try {
            val values = ContentValues().apply {
                put(USER_EMAIL, email)
                put(USER_USERNAME, username)
                put(USER_PASSWORD, password)
            }
            db.insert(TABLE_USERS, null, values) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun loginUser(email: String, password: String): Boolean {
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $USER_EMAIL = ? AND $USER_PASSWORD = ?",
            arrayOf(email, password)
        )
        val success = cursor.count > 0
        cursor.close()
        return success
    }

    fun checkUser(email: String, password: String): Int? {
        val cursor = db.rawQuery(
            "SELECT * FROM users WHERE email = ? AND password = ?",
            arrayOf(email, password)
        )

        val userId: Int? = if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
        } else {
            null
        }

        cursor.close()
        return userId
    }


    // ===== CATEGORY =====
    fun insertCategory(name: String, iconResourceName: String?): Boolean {
        return try {
            val values = ContentValues().apply {
                put(CATEGORY_NAME, name)
                put(CATEGORY_ICON, iconResourceName)
            }
            db.insert(TABLE_CATEGORIES, null, values) != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun getAllCategories(): List<Map<String, String?>> {
        val categories = mutableListOf<Map<String, String?>>()
        val cursor = db.rawQuery("SELECT $CATEGORY_ID, $CATEGORY_NAME, $CATEGORY_ICON FROM $TABLE_CATEGORIES", null)
        if (cursor.moveToFirst()) {
            do {
                categories.add(
                    mapOf(
                        "id" to cursor.getInt(cursor.getColumnIndexOrThrow(CATEGORY_ID)).toString(),
                        "name" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_NAME)),
                        "icon" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ICON))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return categories
    }

    fun getCategoryIdByName(name: String): Int {
        val cursor = db.rawQuery("SELECT _id FROM categories WHERE name = ?", arrayOf(name))
        val id = if (cursor.moveToFirst()) cursor.getInt(0) else -1
        cursor.close()
        return id
    }

    // ===== SCHEDULE =====
    fun insertSchedule(
        title: String,
        description: String,
        time: String,
        date: String,
        categoryId: Int,
        isStarred: Boolean,
        isNotificationEnabled: Boolean,
        status: String = "pending"  // Default status
    ): Boolean {
        return try {
            val values = ContentValues().apply {
                put(SCHEDULE_TITLE, title)
                put(SCHEDULE_DESCRIPTION, description)
                put(SCHEDULE_TIME, time)
                put(SCHEDULE_DATE, date)
                put(SCHEDULE_CATEGORY_ID, categoryId)
                put(SCHEDULE_IS_STARRED, if (isStarred) 1 else 0)
                put(SCHEDULE_IS_NOTIFICATION_ENABLED, if (isNotificationEnabled) 1 else 0)
                put(SCHEDULE_STATUS, status)
            }
            db.insert(TABLE_SCHEDULES, null, values) != -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting schedule", e)
            false
        }
    }

    fun getAllSchedules(): List<Map<String, Any?>> {
        val schedules = mutableListOf<Map<String, Any?>>()
        val query = """
        SELECT s.*, c.$CATEGORY_NAME, c.$CATEGORY_ICON 
        FROM $TABLE_SCHEDULES s 
        LEFT JOIN $TABLE_CATEGORIES c 
        ON s.$SCHEDULE_CATEGORY_ID = c.$CATEGORY_ID
    """.trimIndent()

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val schedule = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_ID)),
                    "title" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_TITLE)),
                    "description" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_DESCRIPTION)),
                    "time" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_TIME)),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_DATE)),
                    "categoryName" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_NAME)),
                    "categoryIcon" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ICON)),
                    "isStarred" to (cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_IS_STARRED)) == 1),
                    "isNotificationEnabled" to (cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_IS_NOTIFICATION_ENABLED)) == 1),
                    "status" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_STATUS))
                )
                schedules.add(schedule)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return schedules
    }

    fun getUserById(userId: Int): Map<String, Any?>? {
        var userData: Map<String, Any?>? = null
        val cursor = db.rawQuery("SELECT * FROM users WHERE _id = ?", arrayOf(userId.toString()))
        if (cursor.moveToFirst()) {
            userData = mapOf(
                "id" to cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                "email" to cursor.getString(cursor.getColumnIndexOrThrow("email")),
                "username" to cursor.getString(cursor.getColumnIndexOrThrow("username")),
                "bio" to cursor.getString(cursor.getColumnIndexOrThrow("bio")),
                "phone" to cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                "photo" to cursor.getBlob(cursor.getColumnIndexOrThrow("photo")),
                "created_at" to cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            )
        }
        cursor.close()
        return userData
    }

    fun updateUserProfile(
        userId: Int,
        username: String,
        email: String,
        phone: String?,
        bio: String?,
        photo: ByteArray?
    ): Boolean {
        val values = ContentValues().apply {
            put("username", username)
            put("email", email)
            put("phone", phone)
            put("bio", bio)
            if (photo != null) {
                put("photo", photo)
            }
            put("updated_at", System.currentTimeMillis().toString())
        }

        val result = db.update(
            "users",
            values,
            "_id = ?",
            arrayOf(userId.toString())
        )

        return result > 0
    }

    fun updateUserPassword(userId: Int, newPassword: String): Boolean {
        return try {
            val values = ContentValues().apply {
                put(USER_PASSWORD, hashPassword(newPassword))
                put("update_at", System.currentTimeMillis().toString())
            }
            db?.update(
                TABLE_USERS,
                values,
                "$COLUMN_ID = ?",
                arrayOf(userId.toString())
            ) ?: 0 > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating password", e)
            false
        }
    }

    private fun hashPassword(password: String): String {
        // Implement proper password hashing (e.g., using BCrypt)
        // This is a simple example - use a proper hashing algorithm in production
        return password.hashCode().toString()
    }

    fun updateScheduleStar(scheduleId: Int, isStarred: Boolean): Boolean {
        return try {
            open()
            val values = ContentValues().apply {
                put("is_starred", if (isStarred) 1 else 0)
            }
            val rows = db.update("schedules", values, "_id = ?", arrayOf(scheduleId.toString()))
            close()
            rows > 0
        } catch (e: Exception) {
            close()
            false
        }
    }

    fun updateScheduleData(scheduleId: Int, title: String, description: String, date: String, time: String, categoryId: Int, isStarred: Boolean, isNotificationEnabled: Boolean): Boolean {
        return try {
            open()
            val values = ContentValues().apply {
                put("title", title)
                put("description", description)
                put("date", date)
                put("time", time)
                put("category_id", categoryId)
                put("is_starred", if (isStarred) 1 else 0)
                put("is_notification_enabled", if (isNotificationEnabled) 1 else 0)
            }
            val rows = db.update("schedules", values, "_id = ?", arrayOf(scheduleId.toString()))
            close()
            rows > 0
        } catch (e: Exception) {
            close()
            false
        }
    }

    fun deleteSchedule(scheduleId: Int): Boolean {
        return try {
            open()
            val rows = db.delete("schedules", "_id = ?", arrayOf(scheduleId.toString()))
            close()
            rows > 0
        } catch (e: Exception) {
            close()
            false
        }
    }

    fun getNotificationSettings(): NotificationSettings {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_NOTIFICATION_SETTINGS,
            null,
            "$COLUMN_ID = ?",
            arrayOf("1"),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val settings = NotificationSettings(
                isEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_ENABLED)) == 1,
                isVibrationEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_VIBRATION_ENABLED)) == 1,
                isSoundEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SOUND_ENABLED)) == 1,
                ringtoneType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_TYPE)),
                customRingtoneUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CUSTOM_RINGTONE_URI)),
                volume = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VOLUME))
            )
            cursor.close()
            settings
        } else {
            cursor.close()
            NotificationSettings() // Return default settings
        }
    }

    fun updateNotificationSettings(settings: NotificationSettings): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_ENABLED, if (settings.isEnabled) 1 else 0)
            put(COLUMN_IS_VIBRATION_ENABLED, if (settings.isVibrationEnabled) 1 else 0)
            put(COLUMN_IS_SOUND_ENABLED, if (settings.isSoundEnabled) 1 else 0)
            put(COLUMN_RINGTONE_TYPE, settings.ringtoneType)
            put(COLUMN_CUSTOM_RINGTONE_URI, settings.customRingtoneUri)
            put(COLUMN_VOLUME, settings.volume)
        }

        val rowsAffected = db.update(
            TABLE_NOTIFICATION_SETTINGS,
            values,
            "$COLUMN_ID = ?",
            arrayOf("1")
        )

        return rowsAffected > 0
    }

    // Custom Ringtones Methods
    fun insertCustomRingtone(ringtone: CustomRingtone): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RINGTONE_NAME, ringtone.name)
            put(COLUMN_RINGTONE_URI, ringtone.uri)
            put(COLUMN_DATE_ADDED, ringtone.dateAdded)
        }

        return db.insert(TABLE_CUSTOM_RINGTONES, null, values)
    }

    fun getAllCustomRingtones(): List<CustomRingtone> {
        val ringtones = mutableListOf<CustomRingtone>()
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_CUSTOM_RINGTONES,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE_ADDED DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val ringtone = CustomRingtone(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_NAME)),
                    uri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_URI)),
                    dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE_ADDED))
                )
                ringtones.add(ringtone)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return ringtones
    }

    fun getCustomRingtoneById(id: Int): CustomRingtone? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            TABLE_CUSTOM_RINGTONES,
            null,
            "$COLUMN_RINGTONE_ID = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )

        return if (cursor.moveToFirst()) {
            val ringtone = CustomRingtone(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_NAME)),
                uri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RINGTONE_URI)),
                dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DATE_ADDED))
            )
            cursor.close()
            ringtone
        } else {
            cursor.close()
            null
        }
    }

    fun updateCustomRingtone(ringtone: CustomRingtone): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_RINGTONE_NAME, ringtone.name)
            put(COLUMN_RINGTONE_URI, ringtone.uri)
            put(COLUMN_DATE_ADDED, ringtone.dateAdded)
        }

        val rowsAffected = db.update(
            TABLE_CUSTOM_RINGTONES,
            values,
            "$COLUMN_RINGTONE_ID = ?",
            arrayOf(ringtone.id.toString())
        )

        return rowsAffected > 0
    }

    fun deleteCustomRingtone(id: Int): Boolean {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(
            TABLE_CUSTOM_RINGTONES,
            "$COLUMN_RINGTONE_ID = ?",
            arrayOf(id.toString())
        )

        return rowsAffected > 0
    }

    fun deleteAllCustomRingtones(): Boolean {
        val db = dbHelper.writableDatabase
        val rowsAffected = db.delete(TABLE_CUSTOM_RINGTONES, null, null)
        return rowsAffected > 0
    }

    fun getCustomRingtoneCount(): Int {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CUSTOM_RINGTONES", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun updateScheduleStatus(scheduleId: Int, status: String): Boolean {
        return try {
            val values = ContentValues().apply {
                put(SCHEDULE_STATUS, status)
            }
            db.update(
                TABLE_SCHEDULES,
                values,
                "$SCHEDULE_ID = ?",
                arrayOf(scheduleId.toString())
            ) > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating schedule status", e)
            false
        }
    }

    fun getSchedulesByStatus(status: String): List<Map<String, Any?>> {
        val schedules = mutableListOf<Map<String, Any?>>()
        val query = """
        SELECT s.*, c.$CATEGORY_NAME, c.$CATEGORY_ICON 
        FROM $TABLE_SCHEDULES s 
        LEFT JOIN $TABLE_CATEGORIES c 
        ON s.$SCHEDULE_CATEGORY_ID = c.$CATEGORY_ID
        WHERE s.$SCHEDULE_STATUS = ?
    """.trimIndent()

        val cursor = db.rawQuery(query, arrayOf(status))
        if (cursor.moveToFirst()) {
            do {
                val schedule = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_ID)),
                    "title" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_TITLE)),
                    "description" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_DESCRIPTION)),
                    "time" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_TIME)),
                    "date" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_DATE)),
                    "categoryName" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_NAME)),
                    "categoryIcon" to cursor.getString(cursor.getColumnIndexOrThrow(CATEGORY_ICON)),
                    "isStarred" to (cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_IS_STARRED)) == 1),
                    "isNotificationEnabled" to (cursor.getInt(cursor.getColumnIndexOrThrow(SCHEDULE_IS_NOTIFICATION_ENABLED)) == 1),
                    "status" to cursor.getString(cursor.getColumnIndexOrThrow(SCHEDULE_STATUS))
                )
                schedules.add(schedule)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return schedules
    }

    fun updateMissedSchedules(): Int {
        val currentDateTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentDate = dateFormat.format(Date(currentDateTime))
        val currentTime = timeFormat.format(Date(currentDateTime))

        val values = ContentValues().apply {
            put(SCHEDULE_STATUS, "missed")
        }

        // Update schedules where date is in past or date is today but time is in past
        return db.update(
            TABLE_SCHEDULES,
            values,
            "(date < ? OR (date = ? AND time < ?)) AND status = 'pending'",
            arrayOf(currentDate, currentDate, currentTime)
        )
    }
}