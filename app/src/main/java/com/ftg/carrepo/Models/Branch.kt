package com.ftg.carrepo.Models

data class Branch(
    val _id: String,
    val name: String,
    val head_office_id: String,
    val contact_one: ContactInfo,
    val contact_two: ContactInfo,
    val contact_three: ContactInfo,
    val records: Long,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val head_offices: List<HeadOffice>
)
data class ContactInfo(
    val name: String,
    val mobile: String
)
