package com.example.unifiednews.behavior

import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior

class CustomBottomSheetBehavior<V : View> : BottomSheetBehavior<V>() {

    private var draggableHandle: View? = null

    fun setDraggableHandle(handle: View) {
        draggableHandle = handle
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isWithinHandle(event)) {
            return super.onInterceptTouchEvent(parent, child, event)
        }
        return false
    }

    private fun isWithinHandle(event: MotionEvent): Boolean {
        draggableHandle?.let {
            val handleLocation = IntArray(2)
            it.getLocationOnScreen(handleLocation)
            val handleLeft = handleLocation[0]
            val handleRight = handleLeft + it.width
            val handleTop = handleLocation[1]
            val handleBottom = handleTop + it.height

            // Check if the touch event is within the handle's bounds
            return event.rawX >= handleLeft && event.rawX <= handleRight
                    && event.rawY >= handleTop && event.rawY <= handleBottom
        }
        return false
    }
}