package com.orhanobut.logger

import android.os.Handler
import android.os.Looper
import android.os.Message
import java.io.File
import java.io.FileWriter
import java.io.IOException

/**
 * Abstract class that takes care of background threading the file log operation on Android.
 * implementing classes are free to directly perform I/O operations there.
 *
 *
 * Writes all logs to the disk with CSV format.
 */
class DiskLogStrategy(handler: Handler) : LogStrategy {
    private val handler = Utils.checkNotNull(handler)

    override fun log(level: Int, tag: String?, message: String) {
        Utils.checkNotNull(message)

        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        // TODO 从这里写入日志
        handler.sendMessage(handler.obtainMessage(level, message))
    }

    internal class WriteHandler(
        looper: Looper,
        folder: String,
        fileName: String,
        private val maxFileSize: Int
    ) : Handler(
        Utils.checkNotNull(looper)
    ) {
        private val folder = Utils.checkNotNull(folder)
        private var mFileName = "mcDevMng_log.log"

        init {
            mFileName = fileName
        }

        override fun handleMessage(msg: Message) {
            val content = msg.obj as String

            var fileWriter: FileWriter? = null
            val logFile = getLogFile(folder, mFileName)

            try {
                fileWriter = FileWriter(logFile, true)
                writeLog(fileWriter, content)
                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) { /* fail silently */
                    }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            Utils.checkNotNull(fileWriter)
            Utils.checkNotNull(content)

            fileWriter.append(content)
        }

        private fun getLogFile(folderName: String, fileName: String): File {
            Utils.checkNotNull(folderName)
            Utils.checkNotNull(fileName)

            val folder = File(folderName)
            if (!folder.exists()) {
                //TODO: What if folder is not created, what happens then?
                folder.mkdirs()
            }

            //            File existingFile = null;

            //            int newFileCount = 0;
            val newFile = File(folder, fileName)

            //            while (newFile.exists()) {
//                existingFile = newFile;
//                newFileCount++;
//                newFile = new File(folder, mFileName);
//            }

//            if (existingFile != null) {
//                if (existingFile.length() >= maxFileSize) {
//                    return newFile;
//                }
//                return existingFile;
//            }
            return newFile
        }
    }
}
