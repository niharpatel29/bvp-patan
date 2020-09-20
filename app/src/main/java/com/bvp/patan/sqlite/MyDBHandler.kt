package com.bvp.patan.sqlite

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.bvp.patan.R
import com.bvp.patan.model.ListItemCalendar
import com.bvp.patan.model.UserModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

var DATABASE_VERSION: Int = 1
const val DATABASE_NAME: String = "bvppatan.db"
const val TABLE_NAME: String = "users"

const val COLUMN_USER_ID: String = "user_id"
const val COLUMN_MOBILE_PRIMARY: String = "mobile_primary"
const val COLUMN_FIRSTNAME: String = "firstname"
const val COLUMN_MIDDLENAME: String = "middlename"
const val COLUMN_LASTNAME: String = "lastname"
const val COLUMN_MOBILE_SECONDARY: String = "mobile_secondary"
const val COLUMN_EMAIL: String = "email"
const val COLUMN_DOB: String = "dob"
const val COLUMN_ANNIVERSARY: String = "anniversary"
const val COLUMN_BLOODGROUP: String = "bloodgroup"
const val COLUMN_GENDER: String = "gender"
const val COLUMN_COUNTRY: String = "country"
const val COLUMN_STATE: String = "state"
const val COLUMN_CITY: String = "city"
const val COLUMN_ZIPCODE: String = "zipcode"
const val COLUMN_RESIDENTIAL_ADDRESS: String = "residential_address"
const val COLUMN_POSITION: String = "position"

class MyDBHandler(val context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val TAG = "MyDBHandlerTAG"
    }

    private val defaultValue: String = ""

    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE $TABLE_NAME (" +
                "$COLUMN_USER_ID INTEGER PRIMARY KEY NOT NULL, " +
                "$COLUMN_MOBILE_PRIMARY TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_FIRSTNAME TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_MIDDLENAME TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_LASTNAME TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_MOBILE_SECONDARY TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_EMAIL TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_DOB TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_ANNIVERSARY TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_BLOODGROUP TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_GENDER TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_COUNTRY TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_STATE TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_CITY TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_ZIPCODE TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_RESIDENTIAL_ADDRESS TEXT DEFAULT '$defaultValue', " +
                "$COLUMN_POSITION TEXT DEFAULT '$defaultValue');"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun isUserExist(userId: String?): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USER_ID = $userId"

        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()

        val count = cursor.count
        cursor.close()
        db.close()

        return count > 0
    }

    // add new user to the database
    fun addUser(model: UserModel) {
        val values = ContentValues()
        values.put(COLUMN_USER_ID, model.userId)
        values.put(COLUMN_MOBILE_PRIMARY, model.mobilePrimary)
        values.put(COLUMN_FIRSTNAME, model.firstname)
        values.put(COLUMN_MIDDLENAME, model.middlename)
        values.put(COLUMN_LASTNAME, model.lastname)
        values.put(COLUMN_MOBILE_SECONDARY, model.mobileSecondary)
        values.put(COLUMN_EMAIL, model.email)
        values.put(COLUMN_DOB, model.dob)
        values.put(COLUMN_ANNIVERSARY, model.anniversary)
        values.put(COLUMN_BLOODGROUP, model.bloodgroup)
        values.put(COLUMN_GENDER, model.gender)
        values.put(COLUMN_COUNTRY, model.country)
        values.put(COLUMN_STATE, model.state)
        values.put(COLUMN_CITY, model.city)
        values.put(COLUMN_ZIPCODE, model.zipcode)
        values.put(COLUMN_RESIDENTIAL_ADDRESS, model.residentialAddress)
        values.put(COLUMN_POSITION, model.position)

        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun updateUserDetails(model: UserModel) {
        val values = ContentValues()
        values.put(COLUMN_USER_ID, model.userId)
        values.put(COLUMN_MOBILE_PRIMARY, model.mobilePrimary)
        values.put(COLUMN_FIRSTNAME, model.firstname)
        values.put(COLUMN_MIDDLENAME, model.middlename)
        values.put(COLUMN_LASTNAME, model.lastname)
        values.put(COLUMN_MOBILE_SECONDARY, model.mobileSecondary)
        values.put(COLUMN_EMAIL, model.email)
        values.put(COLUMN_DOB, model.dob)
        values.put(COLUMN_ANNIVERSARY, model.anniversary)
        values.put(COLUMN_BLOODGROUP, model.bloodgroup)
        values.put(COLUMN_GENDER, model.gender)
        values.put(COLUMN_COUNTRY, model.country)
        values.put(COLUMN_STATE, model.state)
        values.put(COLUMN_CITY, model.city)
        values.put(COLUMN_ZIPCODE, model.zipcode)
        values.put(COLUMN_RESIDENTIAL_ADDRESS, model.residentialAddress)
        values.put(COLUMN_POSITION, model.position)

        val db = writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_USER_ID = ${model.userId}", null)
        db.close()
    }

    fun checkBirthdayToday(): ArrayList<ListItemCalendar> {
        val users = ArrayList<ListItemCalendar>()

        val simpleDateFormat = SimpleDateFormat("dd-MMM", Locale.getDefault())
        val systemDate = simpleDateFormat.format(Calendar.getInstance().time)

        val db = readableDatabase
        val query =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_DOB LIKE '$systemDate%'"

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))
                val firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME))
                val lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME))
                val dob = cursor.getString(cursor.getColumnIndex(COLUMN_DOB))

                val name = "$firstName $lastName"
                val type = context.getString(R.string.type_birthday)

                users.add(ListItemCalendar(userId, name, dob, type))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }

    fun checkAnniversaryToday(): ArrayList<ListItemCalendar> {
        val users = ArrayList<ListItemCalendar>()

        val simpleDateFormat = SimpleDateFormat("dd-MMM", Locale.getDefault())
        val systemDate = simpleDateFormat.format(Calendar.getInstance().time)

        val db = readableDatabase
        val query =
            "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ANNIVERSARY LIKE '$systemDate%'"

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))
                val firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME))
                val lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME))
                val anniversary = cursor.getString(cursor.getColumnIndex(COLUMN_ANNIVERSARY))

                val name = "$firstName $lastName"
                val type = context.getString(R.string.type_anniversary)

                users.add(ListItemCalendar(userId, name, anniversary, type))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }

    fun getAllUsers(): List<UserModel> {
        val users = ArrayList<UserModel>()

        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME ORDER BY $COLUMN_FIRSTNAME ASC"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))
                val mobilePrimary = cursor.getString(cursor.getColumnIndex(COLUMN_MOBILE_PRIMARY))
                val firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME))
                val middleName = cursor.getString(cursor.getColumnIndex(COLUMN_MIDDLENAME))
                val lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME))
                val mobileSecondary =
                    cursor.getString(cursor.getColumnIndex(COLUMN_MOBILE_SECONDARY))
                val email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))
                val dob = cursor.getString(cursor.getColumnIndex(COLUMN_DOB))
                val anniversary = cursor.getString(cursor.getColumnIndex(COLUMN_ANNIVERSARY))
                val bloodgroup = cursor.getString(cursor.getColumnIndex(COLUMN_BLOODGROUP))
                val gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER))
                val country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY))
                val state = cursor.getString(cursor.getColumnIndex(COLUMN_STATE))
                val city = cursor.getString(cursor.getColumnIndex(COLUMN_CITY))
                val zipcode = cursor.getString(cursor.getColumnIndex(COLUMN_ZIPCODE))
                val residentialAddress =
                    cursor.getString(cursor.getColumnIndex(COLUMN_RESIDENTIAL_ADDRESS))
                val position = cursor.getString(cursor.getColumnIndex(COLUMN_POSITION))

                users.add(
                    UserModel(
                        userId,
                        mobilePrimary,
                        firstName,
                        middleName,
                        lastName,
                        mobileSecondary,
                        email,
                        dob,
                        anniversary,
                        bloodgroup,
                        gender,
                        country,
                        state,
                        city,
                        zipcode,
                        residentialAddress,
                        position
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return users
    }

    // get details for specific user
    fun getUserDetails(mUserId: String): UserModel {
        var userDetails: UserModel? = null

        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_USER_ID = $mUserId"

        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndex(COLUMN_USER_ID))
                val mobilePrimary = cursor.getString(cursor.getColumnIndex(COLUMN_MOBILE_PRIMARY))
                val firstName = cursor.getString(cursor.getColumnIndex(COLUMN_FIRSTNAME))
                val middleName = cursor.getString(cursor.getColumnIndex(COLUMN_MIDDLENAME))
                val lastName = cursor.getString(cursor.getColumnIndex(COLUMN_LASTNAME))
                val mobileSecondary =
                    cursor.getString(cursor.getColumnIndex(COLUMN_MOBILE_SECONDARY))
                val email = cursor.getString(cursor.getColumnIndex(COLUMN_EMAIL))
                val dob = cursor.getString(cursor.getColumnIndex(COLUMN_DOB))
                val anniversary = cursor.getString(cursor.getColumnIndex(COLUMN_ANNIVERSARY))
                val bloodgroup = cursor.getString(cursor.getColumnIndex(COLUMN_BLOODGROUP))
                val gender = cursor.getString(cursor.getColumnIndex(COLUMN_GENDER))
                val country = cursor.getString(cursor.getColumnIndex(COLUMN_COUNTRY))
                val state = cursor.getString(cursor.getColumnIndex(COLUMN_STATE))
                val city = cursor.getString(cursor.getColumnIndex(COLUMN_CITY))
                val zipcode = cursor.getString(cursor.getColumnIndex(COLUMN_ZIPCODE))
                val residentialAddress =
                    cursor.getString(cursor.getColumnIndex(COLUMN_RESIDENTIAL_ADDRESS))
                val position = cursor.getString(cursor.getColumnIndex(COLUMN_POSITION))

                userDetails = UserModel(
                    userId,
                    mobilePrimary,
                    firstName,
                    middleName,
                    lastName,
                    mobileSecondary,
                    email,
                    dob,
                    anniversary,
                    bloodgroup,
                    gender,
                    country,
                    state,
                    city,
                    zipcode,
                    residentialAddress,
                    position
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return userDetails!!
    }

    // clear database
    fun clearDatabase() {
        val db = writableDatabase
        db.execSQL("DELETE FROM $TABLE_NAME")
        db.close()
    }
}