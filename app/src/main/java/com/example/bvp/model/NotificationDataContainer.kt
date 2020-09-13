package com.example.bvp.model

import com.example.bvp.R

data class NotificationDataContainer(
    val title: String,
    val message: String,
    val id: Int = 999,
    val icon: Int = R.drawable.ic_baseline_check_circle_filled,
    val color: Int = R.color.colorPrimary
)