package org.setu.placemarks

class PlacedMarkList {

    private val marks = ArrayList<PlacedMark>()
    private var lastId = 0L

    fun findAll(): List<PlacedMark> {
        return marks
    }

    fun create(mark: PlacedMark) {
        mark.id = ++lastId
        marks.add(mark)
    }

    fun findOne(id: Long): PlacedMark? {
        return marks.find { it.id == id }
    }

    fun update(mark: PlacedMark): Boolean {
        val index = marks.indexOfFirst { it.id == mark.id }

        return if (index != -1) {
            marks[index] = mark
            true
        } else {
            false
        }
    }

    fun delete(id: Long): Boolean {
        val mark = findOne(id)

        return if (mark != null) {
            marks.remove(mark)
            true
        } else {
            false
        }
    }
}