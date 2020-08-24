package com.example.bvp

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bvp.adapter.CustomAdapter
import com.example.bvp.model.ListItem
import com.example.bvp.sqlite.MyDBHandler
import kotlinx.android.synthetic.main.user_directory.*

class UserDirectory : AppCompatActivity() {

    companion object {
        private const val TAG = "UserDirectoryTAG"
    }

    private val userList = ArrayList<ListItem>()
    private lateinit var customAdapter: CustomAdapter
    private lateinit var dbHandler: MyDBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_directory)

        dbHandler = MyDBHandler(this)

        toolbar()
        setUserList()
        search()
    }

    private fun toolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
            val image = R.drawable.default_image
            val name = "${users[i].firstname} ${users[i].lastname}"
            val position = users[i].position

            userList.add(ListItem(userId, image, name, position))
        }

        customAdapter = CustomAdapter(this, userList)
        recyclerView.adapter = customAdapter
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
        val temp = ArrayList<ListItem>()
        for (data in userList) {
            if (data.name.contains(text, true)) {
                temp.add(data)
            }
        }
        // update recyclerview
        customAdapter.updateList(temp)
    }
}