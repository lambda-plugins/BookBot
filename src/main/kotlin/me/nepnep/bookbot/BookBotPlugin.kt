package me.nepnep.bookbot

import com.lambda.client.plugin.api.Plugin

@Suppress("unused")
internal object BookBotPlugin: Plugin() {
    override fun onLoad() {
        commands.add(BookBotCommand)
    }
}