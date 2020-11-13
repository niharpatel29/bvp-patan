package com.bvp.patan.activities.categories

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bvp.patan.R
import com.bvp.patan.adapter.CalendarAdapter
import com.bvp.patan.model.ListItemCalendar
import com.bvp.patan.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.calendar_events.*

class CalendarEvents : AppCompatActivity() {

    companion object {
        private const val TAG = "CalendarEventsTAG"
    }

    private val userListToday = ArrayList<ListItemCalendar>()
    private val userListTomorrow = ArrayList<ListItemCalendar>()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var dbHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_events)

        dbHandler = MyDBHandler(this)

        toolbar()
        setUserListToday()
        setUserListTomorrow()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
        toolbar.overflowIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun setUserListToday() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        userListToday.clear()

        val users = ArrayList<ListItemCalendar>()
        users.addAll(dbHandler.checkBirthdayToday())
        users.addAll(dbHandler.checkAnniversaryToday())

        for (i in users.indices) {
            val userId = users[i].userId
            val name = users[i].name
            val date = users[i].date
            val type = users[i].type

            userListToday.add(ListItemCalendar(userId, name, date, type))
        }

        if (userListToday.isEmpty()) {
            tvNoEventsToday.visibility = View.VISIBLE
        } else {
            tvNoEventsToday.visibility = View.GONE
        }

        calendarAdapter = CalendarAdapter(this, userListToday)
        recyclerView.adapter = calendarAdapter
    }

    private fun setUserListTomorrow() {
        recyclerViewTomorrow.layoutManager = LinearLayoutManager(this)
        userListTomorrow.clear()

        val users = ArrayList<ListItemCalendar>()
        users.addAll(dbHandler.checkBirthdayTomorrow())
        users.addAll(dbHandler.checkAnniversaryTomorrow())

        for (i in users.indices) {
            val userId = users[i].userId
            val name = users[i].name
            val date = users[i].date
            val type = users[i].type

            userListTomorrow.add(ListItemCalendar(userId, name, date, type))
        }

        if (userListTomorrow.isEmpty()) {
            tvNoEventsTomorrow.visibility = View.VISIBLE
        } else {
            tvNoEventsTomorrow.visibility = View.GONE
        }

        calendarAdapter = CalendarAdapter(this, userListTomorrow)
        recyclerViewTomorrow.adapter = calendarAdapter
    }
}