package com.example.placemarks

interface OnMarkListener {

    fun onMarkClick(mark: PlacedMark)

    fun onMarkLongClick(mark: PlacedMark): Boolean

}

