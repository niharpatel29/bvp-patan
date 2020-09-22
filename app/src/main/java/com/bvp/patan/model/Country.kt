package com.bvp.patan.model

data class Country(
    val id: Int,
    val name: String,
    val phoneCode: Int,
    val sortname: String
) {
    override fun toString(): String {
        return name
    }
}