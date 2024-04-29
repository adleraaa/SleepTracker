package com.mistershorr.loginandregistration

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.backendless.Backendless
import com.backendless.async.callback.AsyncCallback
import com.backendless.exceptions.BackendlessFault
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


class SleepAdapter (var sleepList: MutableList<Sleep>) : RecyclerView.Adapter<SleepAdapter.ViewHolder>() {

    companion object {
        val TAG = "SleepAdapter"
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewDate : TextView
        val textViewHours: TextView
        val textViewDuration: TextView
        val layout : ConstraintLayout
        val ratingBarQuality : RatingBar

        init {
            textViewDate = view.findViewById(R.id.textView_itemSleep_date)
            textViewDuration = view.findViewById(R.id.textView_itemSleep_duration)
            textViewHours = view.findViewById(R.id.textView_itemSleep_hours)
            layout = view.findViewById(R.id.layout_itemSleep)
            ratingBarQuality = view.findViewById(R.id.ratingBar_itemSleep_sleepQuality)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sleep, parent, false)
        val holder = ViewHolder(view)
        return holder
    }

    override fun onBindViewHolder(holder: SleepAdapter.ViewHolder, position: Int) {
        val sleepData = sleepList[position]
        val context = holder.layout.context

        // the rest of your code that assigns values to the various textviews and ratingbars, etc.

        val formatter = DateTimeFormatter.ofPattern("yyy-MM-dd")
        val sleepDate = LocalDateTime.ofEpochSecond(sleepData.sleepDateMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val bedDate = LocalDateTime.ofEpochSecond(sleepData.bedMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))
        val wakeDate = LocalDateTime.ofEpochSecond(sleepData.wakeMillis/1000, 0,
            ZoneId.systemDefault().rules.getOffset(Instant.now()))

        holder.textViewDate.text = formatter.format(sleepDate)

        val formatter1 = DateTimeFormatter.ofPattern("HH:mm")

        holder.textViewHours.text= formatter1.format(bedDate) + "-" + formatter1.format(wakeDate)


        holder.ratingBarQuality.rating = sleepData.quality / 2.0f

        val totalMillis =  (sleepData.wakeMillis - sleepData.bedMillis)
        val hours = totalMillis / 1000 / 60 / 60
        val minutes = totalMillis / 1000 / 60 % 60

        holder.textViewDuration.text = hours.toString() + ":" + minutes.toString()

        holder.layout.setOnClickListener {
            val intent = Intent(context, SleepDetailActivity::class.java)
            intent.putExtra(SleepDetailActivity.EXTRA_SLEEP, sleepData)
            // make an intent to launch the DetailActivity
            // put the sleepData as an extra to bring over to that activity
            context.startActivity(intent)

        }

        holder.layout.isLongClickable = true
        holder.layout.setOnLongClickListener {
            val popMenu = PopupMenu(context, holder.textViewDuration)
            popMenu.inflate(R.menu.menu_sleep_list_context)
            popMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.menu_sleeplist_delete -> {
                        deleteFromBackendless(position)
                        true
                    }
                    else -> true
                }
            }
            popMenu.show()
            true

        }
    }




    private fun deleteFromBackendless(position: Int) {
        Log.d("SleepAdapter", "deleteFromBackendless: Trying to delete ${sleepList[position]}")

        // put in the code to delete the item using the callback from Backendless
        // in the handleResponse, we'll need to also delete the item from the sleepList
        // and make sure that the recyclerview is updated
        Backendless.Data.of(Sleep::class.java).remove(sleepList[position],
            object : AsyncCallback<Long?> {
                override fun handleResponse(response: Long?) {
                    // Contact has been deleted. The response is the
                    // time in milliseconds when the object was deleted
                    sleepList.removeAt(position)
                    notifyDataSetChanged()
                }

                override fun handleFault(fault: BackendlessFault) {
                    // an error has occurred, the error code can be
                    // retrieved with fault.getCode()
                }
            })


    }


    override fun getItemCount(): Int {
        return sleepList.size
    }

}