package com.example.placemarks.models

import com.example.placemarks.PlacedMark

interface PlacemarkStore {
    fun findAll(): List<PlacedMark>
    fun create(placemark: PlacedMark)
    fun update(placemark: PlacedMark): Boolean
    fun delete(id: Long): Boolean
    fun findOne(id: Long): PlacedMark?
}
