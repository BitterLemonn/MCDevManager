package com.orhanobut.logger

import com.orhanobut.logger.Logger.ASSERT
import com.orhanobut.logger.Logger.DEBUG
import com.orhanobut.logger.Logger.ERROR
import com.orhanobut.logger.Logger.INFO
import com.orhanobut.logger.Logger.NET
import com.orhanobut.logger.Logger.VERBOSE
import com.orhanobut.logger.Logger.WARN
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Source
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

internal class LoggerPrinter : Printer {
    /**
     * Provides one-time used tag for the log message
     */
    private val localTag = ThreadLocal<String>()

    private val logAdapters: MutableList<LogAdapter> = ArrayList()

    override fun t(tag: String?): Printer {
        if (tag != null) {
            localTag.set(tag)
        }
        return this
    }

    override fun d(message: String, vararg args: Any?) {
        log(DEBUG, null, message, *args)
    }

    override fun d(`object`: Any?) {
        log(DEBUG, null, Utils.toString(`object`))
    }

    override fun e(message: String, vararg args: Any?) {
        e(null, message, *args)
    }

    override fun e(throwable: Throwable?, message: String, vararg args: Any?) {
        log(ERROR, throwable, message, *args)
    }

    override fun w(message: String, vararg args: Any?) {
        log(WARN, null, message, *args)
    }

    override fun i(message: String, vararg args: Any?) {
        log(INFO, null, message, *args)
    }

    override fun v(message: String, vararg args: Any?) {
        log(VERBOSE, null, message, *args)
    }

    override fun n(message: String, vararg args: Any?) {
        log(NET, null, message, *args)
    }

    override fun wtf(message: String, vararg args: Any?) {
        log(ASSERT, null, message, *args)
    }

    override fun json(json: String?) {
        var json = json
        if (Utils.isEmpty(json)) {
            d("Empty/Null json content")
            return
        }
        try {
            json = json!!.trim { it <= ' ' }
            if (json.startsWith("{")) {
                val jsonObject = JSONObject(json)
                val message = jsonObject.toString(JSON_INDENT)
                d(message)
                return
            }
            if (json.startsWith("[")) {
                val jsonArray = JSONArray(json)
                val message = jsonArray.toString(JSON_INDENT)
                d(message)
                return
            }
            e("Invalid Json")
        } catch (e: JSONException) {
            e("Invalid Json")
        }
    }

    override fun xml(xml: String?) {
        if (Utils.isEmpty(xml)) {
            d("Empty/Null xml content")
            return
        }
        try {
            val xmlInput: Source = StreamSource(StringReader(xml))
            val xmlOutput = StreamResult(StringWriter())
            val transformer = TransformerFactory.newInstance().newTransformer()
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
            transformer.transform(xmlInput, xmlOutput)
            d(xmlOutput.writer.toString().replaceFirst(">".toRegex(), ">\n"))
        } catch (e: TransformerException) {
            e("Invalid xml")
        }
    }

    @Synchronized
    override fun log(
        priority: Int,
        tag: String?,
        message: String?,
        throwable: Throwable?
    ) {
        var message = message
        if (throwable != null && message != null) {
            message += " : " + Utils.getStackTraceString(throwable)
        }
        if (throwable != null && message == null) {
            message = Utils.getStackTraceString(throwable)
        }
        if (Utils.isEmpty(message)) {
            message = "Empty/NULL log message"
        }

        for (adapter in logAdapters) {
            if (adapter.isLoggable(priority, tag)) {
                adapter.log(priority, tag, message!!)
            }
        }
    }

    override fun clearLogAdapters() {
        logAdapters.clear()
    }

    override fun addAdapter(adapter: LogAdapter) {
        logAdapters.add(Utils.checkNotNull(adapter))
    }

    /**
     * This method is synchronized in order to avoid messy of logs' order.
     */
    @Synchronized
    private fun log(
        priority: Int,
        throwable: Throwable?,
        msg: String,
        vararg args: Any?
    ) {
        Utils.checkNotNull(msg)

        val tag = tag
        val message = createMessage(msg, *args)
        log(priority, tag, message, throwable)
    }

    private val tag: String?
        /**
         * @return the appropriate tag based on local or global
         */
        get() {
            val tag = localTag.get()
            if (tag != null) {
                localTag.remove()
                return tag
            }
            return null
        }

    private fun createMessage(message: String, vararg args: Any?): String {
        return if (args == null || args.size == 0) message else String.format(message, *args)
    }

    companion object {
        /**
         * It is used for json pretty print
         */
        private const val JSON_INDENT = 2
    }
}
