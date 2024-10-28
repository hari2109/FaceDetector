package com.billionhearts.facedetector

import android.database.Cursor
import android.util.Log
import android.view.View

@Suppress("NOTHING_TO_INLINE")
inline fun View.visible() {
    visibility = View.VISIBLE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.invisible() {
    visibility = View.INVISIBLE
}

@Suppress("NOTHING_TO_INLINE")
inline fun View.gone() {
    visibility = View.GONE
}

inline fun Cursor.isCursorUsable(cursor: Cursor?): Boolean {
    try {
        return (cursor != null && !cursor.isClosed && cursor.count > 0)
    } catch (e: Exception) {
        Log.e("CursorUsable", e.message ?: "")
    }
    return false
}