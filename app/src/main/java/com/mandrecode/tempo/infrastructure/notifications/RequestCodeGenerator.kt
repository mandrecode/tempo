package com.mandrecode.tempo.infrastructure.notifications

/**
 * Generates safe Int request codes from Long IDs for PendingIntents and notification IDs.
 * Uses modulo to prevent Int overflow when Room auto-generated Long IDs exceed Int.MAX_VALUE.
 * Ranges are partitioned by entity type to avoid collisions:
 * - Tasks:         0 -   999,999
 * - Habits:  1,000,000 - 1,999,999
 * - Chains:  2,000,000 - 2,999,999
 * - Live Activity: 3,000,000 - 3,999,999
 */
object RequestCodeGenerator {
    private const val RANGE_SIZE = 1_000_000
    private const val TASK_OFFSET = 0
    private const val HABIT_OFFSET = RANGE_SIZE
    private const val CHAIN_OFFSET = 2 * RANGE_SIZE
    private const val LIVE_ACTIVITY_OFFSET = 3 * RANGE_SIZE

    fun forTask(taskId: Long): Int = TASK_OFFSET + (taskId % RANGE_SIZE).toInt()

    fun forHabit(habitId: Long): Int = HABIT_OFFSET + (habitId % RANGE_SIZE).toInt()

    fun forHabitChain(chainId: Long): Int = CHAIN_OFFSET + (chainId % RANGE_SIZE).toInt()

    fun forLiveActivity(chainId: Long): Int = LIVE_ACTIVITY_OFFSET + (chainId % RANGE_SIZE).toInt()
}
