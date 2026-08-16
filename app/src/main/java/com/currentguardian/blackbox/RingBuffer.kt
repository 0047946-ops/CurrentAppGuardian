package com.currentguardian.blackbox

import java.util.ArrayDeque

class RingBuffer<T>(
    private val capacity: Int
) {

    private val deque = ArrayDeque<T>()

    @Synchronized
    fun add(value: T) {

        if (deque.size >= capacity) {
            deque.removeFirst()
        }

        deque.addLast(value)
    }

    @Synchronized
    fun snapshot(): List<T> {
        return deque.toList()
    }

    @Synchronized
    fun clear() {
        deque.clear()
    }

    @Synchronized
    fun size(): Int {
        return deque.size
    }
}
