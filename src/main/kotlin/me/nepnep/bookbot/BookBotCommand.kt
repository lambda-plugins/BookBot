package me.nepnep.bookbot

import com.lambda.client.command.ClientCommand
import com.lambda.client.event.SafeExecuteEvent
import com.lambda.client.util.items.itemPayload
import com.lambda.client.util.text.MessageSendHelper
import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.stream.Collectors

// All of the dupe book code came from lambda, which got some of it from EarthComputer, the original credits notice is below

/**
 * @author 0x2E | PretendingToCode
 * @author EarthComputer
 *
 * The characterGenerator is from here: https://github.com/ImpactDevelopment/ImpactIssues/issues/1123#issuecomment-482721273
 * Which was written by EarthComputer for both EvilSourcerer and 0x2E
 */
object BookBotCommand : ClientCommand(
    name = "bookbotplugin",
    description = "Writes to books"
) {
    init {
        literal("dupe") {
            boolean("sign book") { signBookArg ->
                executeSafe {
                    createDupeBook(signBookArg.value)
                }
            }
            executeSafe {
                createDupeBook(false)
            }
        }

        literal("file") {
            boolean("sign book") { signBookArg ->
                greedy("title") { titleArg ->
                    executeSafe {
                        createFileBook(signBookArg.value, titleArg.value)
                    }
                }
            }
        }
    }

    private fun SafeExecuteEvent.createDupeBook(sign: Boolean) {
        val heldItem = player.inventory.getCurrentItem()

        if (heldItem.item is ItemWritableBook) {
            val characterGenerator = Random()
                .ints(0x80, 0x10ffff - 0x800)
                .map { if (it < 0xd800) it else it + 0x800 }

            val joinedPages = characterGenerator
                .limit(50 * 210L)
                .mapToObj { it.toChar().toString() } // this has to be turned into a Char first, otherwise you will get the raw Int value
                .collect(Collectors.joining())

            val pages = NBTTagList()
            val title = if (sign) UUID.randomUUID().toString().substring(0, 5) else ""

            for (page in 0..49) {
                pages.appendTag(NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)))
            }

            if (heldItem.hasTagCompound()) {
                heldItem.tagCompound!!.setTag("pages", pages)
                heldItem.tagCompound!!.setTag("title", NBTTagString(title))
                heldItem.tagCompound!!.setTag("author", NBTTagString(player.name))
            } else {
                heldItem.setTagInfo("pages", pages)
                heldItem.setTagInfo("title", NBTTagString(title))
                heldItem.setTagInfo("author", NBTTagString(player.name))
            }

            itemPayload(heldItem, "MC|BEdit")

            if (sign) {
                itemPayload(heldItem, "MC|BSign")
            }

            MessageSendHelper.sendChatMessage("Dupe book generated.")
        } else {
            MessageSendHelper.sendErrorMessage("You must be holding a writable book.")
        }
    }

    private fun SafeExecuteEvent.createFileBook(sign: Boolean, title: String) {
        val newPage = ":PAGE:"
        val file = File("lambda/book.txt")
        if (file.exists()) {
            val heldItem = player.inventory.getCurrentItem()
            if (heldItem.item is ItemWritableBook) {
                val reader = FileReader(file)
                val content = reader.readText().trim()
                reader.close()

                val pages = NBTTagList()

                var p = 0
                var isFirstIt = true
                val builder = StringBuilder()
                for (char in content) {
                    if (p == 0 && !isFirstIt) {
                        builder.append(newPage)
                    }

                    builder.append(char)
                    p++
                    p %= 210
                    isFirstIt = false
                }

                var pageCount = 1
                for (page in builder.split(newPage)) {
                    pages.appendTag(NBTTagString(page))
                    if (pageCount == 50) {
                        break
                    }
                    pageCount++
                }

                if (heldItem.hasTagCompound()) {
                    heldItem.tagCompound!!.setTag("pages", pages)
                    heldItem.tagCompound!!.setTag("title", NBTTagString(title))
                    heldItem.tagCompound!!.setTag("author", NBTTagString(player.name))
                } else {
                    heldItem.setTagInfo("pages", pages)
                    heldItem.setTagInfo("title", NBTTagString(title))
                    heldItem.setTagInfo("author", NBTTagString(player.name))
                }

                itemPayload(heldItem, "MC|BEdit")

                if (sign) {
                    itemPayload(heldItem, "MC|BSign")
                }

                MessageSendHelper.sendChatMessage("Book written.")
            } else {
                MessageSendHelper.sendChatMessage("You must be holding a writable book.")
            }
        } else {
            MessageSendHelper.sendChatMessage("The file does not exist, create it as lambda/book.txt.")
        }
    }
}