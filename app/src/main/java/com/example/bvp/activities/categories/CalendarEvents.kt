package com.example.bvp.activities.categories

import android.graphics.PorterDuff
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.R
import com.example.bvp.adapter.CalendarAdapter
import com.example.bvp.model.ListItemCalendar
import com.example.bvp.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.calendar_events.*

class CalendarEvents : AppCompatActivity() {

    companion object {
        private const val TAG = "CalendarEventsTAG"
    }

    private val userList = ArrayList<ListItemCalendar>()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var dbHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.calendar_events)

        dbHandler = MyDBHandler(this)

        toolbar()
        setUserList()
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

    private fun setUserList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList.clear()

        val users = ArrayList<ListItemCalendar>()
        users.addAll(dbHandler.checkBirthdayToday())
        users.addAll(dbHandler.checkAnniversaryToday())

        for (i in users.indices) {
            val userId = users[i].userId
            val name = users[i].name
            val date = users[i].date
            val type = users[i].type

            userList.add(ListItemCalendar(userId, name, date, type))
        }

        if (userList.isEmpty()) {
            tvNoEvents.visibility = View.VISIBLE
        } else {
            tvNoEvents.visibility = View.GONE
        }

        calendarAdapter = CalendarAdapter(this, userList)
        recyclerView.adapter = calendarAdapter
    }
}