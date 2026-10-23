package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // "user", "assistant", "system", "action"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val provider: String? = null,
    val model: String? = null,
    val imageUrl: String? = null,
    val actionType: String? = null,
    val actionStatus: String? = null, // "SUCCESS", "FAILED", "PENDING"
    val isStreaming: Boolean = false
)
