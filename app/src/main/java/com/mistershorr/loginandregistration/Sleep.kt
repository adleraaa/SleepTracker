package com.mistershorr.loginandregistration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sleep(
    var bedMillis: Long = System.currentTimeMillis(),
    var sleepDateMillis: Long = System.currentTimeMillis(),
    var wakeMillis: Long = System.currentTimeMillis(),
    val quality: Int = 5,
    val notes: String? = null,
    var ownerId: String? = null,
    var objectId: String? = null
) : Parcelable
