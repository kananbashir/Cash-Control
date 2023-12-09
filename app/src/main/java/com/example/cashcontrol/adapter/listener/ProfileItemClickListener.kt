package com.example.cashcontrol.adapter.listener

import com.example.cashcontrol.data.db.entity.Profile

interface ProfileItemClickListener {

    fun onProfileItemClick (profile: Profile, operation: String)

}