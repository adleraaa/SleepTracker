package com.mistershorr.loginandregistration

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import com.backendless.persistence.DataQueryBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mistershorr.loginandregistration.databinding.ActivitySleepDetailBinding
import com.mistershorr.loginandregistration.databinding.ActivitySleepListBinding
import java.util.Date

class SleepListActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySleepListBinding

    companion object {
        val TAG = "SleepListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepListBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.FloatingActionButtonListActivityAddButton.setOnClickListener{
            addObject()
        }
    }

    override fun onResume() {
        super.onResume()
        loadDataFromBackendless2()
    }
    fun addObject(){
        val intent = Intent(this, SleepDetailActivity::class.java)
        this.startActivity(intent)
    }
    // Functions to temporarily go into the SleepListActivity's onCreate for testing

    // Functions to temporarily go into the SleepListActivity's onCreate for testing

// Functions to temporarily go into the SleepListActivity's onCreate for testing

    fun loadDataFromBackendless2() {
        val userId = Backendless.UserService.CurrentUser().userId
        // need the ownerId to match the objectId of the user
        val whereClause = "ownerId = '$userId'"
        val queryBuilder = DataQueryBuilder.create()
        queryBuilder.whereClause = whereClause
        // include the queryBuilder in the find function
        Backendless.Data.of(Sleep::class.java)
            .find(queryBuilder, object : AsyncCallback<List<Sleep>?> {
                override fun handleResponse(sleepList: List<Sleep>?) {
                    Log.d(TAG, "handleResponse: $sleepList")
                    // this is where you would set up your recyclerView
                    val adapter = SleepAdapter(sleepList as MutableList<Sleep>? ?: mutableListOf())
                    binding.ListActivitySleep.adapter = adapter
                    binding.ListActivitySleep.layoutManager = LinearLayoutManager(this@SleepListActivity)
                }

                override fun handleFault(fault: BackendlessFault) {
                    Log.d(TAG, "handleFault: ${fault.message}")
                }
            })
    }

    fun saveToBackendless() {
        // the real use case will be to read from all the editText
        // fields in the detail activity and then use that info
        // to make the object

        // here, we'll just make up an object
        val sleep = Sleep(
            Date().time, 1711753845000L, Date().time,
            10, "finally a restful night"
        )
        sleep.ownerId = Backendless.UserService.CurrentUser().userId
        // if i do not set the objectId, it will make a new object
//
//        if(){
//            var sleep = Sleep(
//                Date().time, 1711753845000L, Date().time,
//                10, "finally a restful night"
//            )
//        }
//        else{
//
//        }

        // if I do set the objectId to an existing object Id from data table
        // on backendless, it will update the object.

        // include the async callback to save the object here
    }
}