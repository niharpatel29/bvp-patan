package com.bvp.patan.admin

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bvp.patan.R
import com.bvp.patan.adapter.UserListForAdminAdapter
import com.bvp.patan.model.ListItemUsers
import com.bvp.patan.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.user_list.*

class UserList : AppCompatActivity() {

    companion object {
        private const val TAG = "UserListTAG"
    }

    private val userList = ArrayList<ListItemUsers>()
    private lateinit var usersAdapter: UserListForAdminAdapter
    private lateinit var dbHandler: MyDBHandler
    private lateinit var type: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_list)

        dbHandler = MyDBHandler(this)
        type = intent.getStringExtra("type")!!

        toolbar()
        setUserList()
        search()
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
        if (type == getString(R.string.type_delete_user)) {
            supportActionBar?.title = getString(R.string.delete_user)
        }
        if (type == getString(R.string.type_make_admin)) {
            supportActionBar?.title = getString(R.string.make_admin)
        }
        if (type == getString(R.string.type_modify_user)) {
            supportActionBar?.title = getString(R.string.modify_user_profile)
        }
        toolbar.navigationIcon?.setColorFilter(
            resources.getColor(R.color.colorWhite),
            PorterDuff.Mode.SRC_ATOP
        )
    }

    private fun setUserList() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        userList.clear()

        val users = dbHandler.getAllUsers()
        for (i in users.indices) {
            val userId = users[i].userId
            val name = "${users[i].firstname} ${users[i].lastname}"
            val position = users[i].position

            userList.add(ListItemUsers(userId, name, position))
        }

        usersAdapter = UserListForAdminAdapter(this, type, userList)
        recyclerView.adapter = usersAdapter
    }

    private fun search() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                filter(s.toString())
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
            }
        })
    }

    private fun filter(text: String) {
        val temp = ArrayList<ListItemUsers>()
        for (data in userList) {
            if (data.name.contains(text, true)) {
                temp.add(data)
            }
        }
        // update recyclerview
        usersAdapter.updateList(temp)
    }
}