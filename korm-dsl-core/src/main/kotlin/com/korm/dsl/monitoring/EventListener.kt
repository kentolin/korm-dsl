package com.korm.dsl.monitoring

import java.util.concurrent.CopyOnWriteArrayList

/**
 * Listener for monitoring events
 */
interface EventListener {
    /**
     * Handle a monitoring event
     */
    fun onEvent(event: MonitoringEvent)
}

/**
 * Event bus for distributing monitoring events to listeners
 */
class EventBus {
    private val listeners = CopyOnWriteArrayList<EventListener>()

    /**
     * Register a listener
     */
    fun addListener(listener: EventListener) {
        listeners.add(listener)
    }

    /**
     * Unregister a listener
     */
    fun removeListener(listener: EventListener) {
        listeners.remove(listener)
    }

    /**
     * Remove all listeners
     */
    fun clearListeners() {
        listeners.clear()
    }

    /**
     * Publish an event to all listeners
     */
    fun publish(event: MonitoringEvent) {
        listeners.forEach { listener ->
            try {
                listener.onEvent(event)
            } catch (e: Exception) {
                // Don't let listener errors break the system
                System.err.println("Error in event listener: ${e.message}")
            }
        }
    }

    /**
     * Get current listener count
     */
    fun listenerCount(): Int = listeners.size

    companion object {
        /**
         * Global event bus instance
         */
        val global = EventBus()
    }
}

/**
 * Abstract base class for event listeners
 */
abstract class AbstractEventListener : EventListener {

    /**
     * Filter to only handle specific event types
     */
    protected open fun shouldHandle(event: MonitoringEvent): Boolean = true

    override fun onEvent(event: MonitoringEvent) {
        if (shouldHandle(event)) {
            handleEvent(event)
        }
    }

    /**
     * Handle the event (called only if shouldHandle returns true)
     */
    protected abstract fun handleEvent(event: MonitoringEvent)
}

/**
 * Event listener that only handles specific event types
 */
abstract class TypedEventListener<T : MonitoringEvent>(
    private val eventClass: Class<T>
) : AbstractEventListener() {

    override fun shouldHandle(event: MonitoringEvent): Boolean {
        return eventClass.isInstance(event)
    }

    override fun handleEvent(event: MonitoringEvent) {
        @Suppress("UNCHECKED_CAST")
        handleTypedEvent(event as T)
    }

    /**
     * Handle the typed event
     */
    protected abstract fun handleTypedEvent(event: T)
}

/**
 * Query event listener
 */
abstract class QueryEventListener : TypedEventListener<QueryEvent>(QueryEvent::class.java)

/**
 * Connection event listener
 */
abstract class ConnectionEventListener : TypedEventListener<ConnectionEvent>(ConnectionEvent::class.java)

/**
 * Transaction event listener
 */
abstract class TransactionEventListener : TypedEventListener<TransactionEvent>(TransactionEvent::class.java)

/**
 * Migration event listener
 */
abstract class MigrationEventListener : TypedEventListener<MigrationEvent>(MigrationEvent::class.java)

/**
 * Batch event listener
 */
abstract class BatchEventListener : TypedEventListener<BatchEvent>(BatchEvent::class.java)
