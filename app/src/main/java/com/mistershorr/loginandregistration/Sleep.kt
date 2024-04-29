package com.mistershorr.loginandregistration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Sleep(
    var bedMillis: Long = System.currentTimeMillis(),
    var sleepDateMillis: Long = System.currentTimeMillis(),
    var wakeMillis: Long = System.currentTimeMillis(),
    var quality: Int = 5,
    var notes: String? = null,
    var ownerId: String? = null,
    var objectId: String? = null
) : Parcelable
