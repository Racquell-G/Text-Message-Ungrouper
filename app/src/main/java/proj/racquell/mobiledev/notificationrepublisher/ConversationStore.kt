object ConversationStore {
    private val map = LinkedHashMap<String, ArrayDeque<String>>() // newest first

    fun add(conversationId: String, msg: String, max: Int = 8): List<String> {
        val dq = map.getOrPut(conversationId) { ArrayDeque() }
        dq.addFirst(msg)
        while (dq.size > max) dq.removeLast()
        return dq.toList()
    }
}
