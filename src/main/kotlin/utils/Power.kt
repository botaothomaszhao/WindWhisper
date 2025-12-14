package moe.tachyon.windwhisper.utils

import io.ktor.server.application.ApplicationStopPreparing
import io.ktor.utils.io.InternalAPI
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import moe.tachyon.windwhisper.console.AnsiStyle.Companion.RESET
import moe.tachyon.windwhisper.console.SimpleAnsiColor.Companion.CYAN
import moe.tachyon.windwhisper.console.SimpleAnsiColor.Companion.PURPLE
import moe.tachyon.windwhisper.logger.WindWhisperLogger
import moe.tachyon.windwhisper.mainJob
import moe.tachyon.windwhisper.server
import java.io.File
import kotlin.system.exitProcess

@Suppress("unused")
object Power
{
    @JvmField
    val logger = WindWhisperLogger.getLogger()
    private var isShutdown: Int? = null

    @JvmStatic
    fun shutdown(code: Int, cause: String = "unknown"): Nothing
    {
        synchronized(Power)
        {
            if (isShutdown != null) exitProcess(isShutdown!!)
            isShutdown = code
        }
        logger.warning("${PURPLE}Server is shutting down: ${CYAN}$cause${RESET}")
        val server = server
        if (server != null) logger.warning("Failed to stop Ktor: ")
        {
            server.monitor.raise(ApplicationStopPreparing, server.environment)
            server.engine.stop()
            @OptIn(InternalAPI::class)
            runBlocking { server.disposeAndJoin() }
            logger.info("Ktor is stopped.")
        }
        val mainJob = mainJob
        if (mainJob != null)
        {
            logger.info("Waiting for MainJob to complete...")
            logger.warning("Failed to stop MainJob")
            {
                runBlocking { mainJob.cancelAndJoin() }
            }.onSuccess()
            {
                logger.info("MainJob is stopped.")
            }
        }
        startShutdownHook(code)
        exitProcess(code)
    }

    @JvmStatic
    private fun startShutdownHook(code: Int)
    {
        val hook = Thread()
        {
            try
            {
                Thread.sleep(3000)
                logger.severe("检测到程序未退出，尝试强制终止")
                Runtime.getRuntime().halt(code)
            }
            catch (e: InterruptedException)
            {
                Thread.currentThread().interrupt()
            }
        }
        hook.isDaemon = true
        hook.start()
    }

    @JvmStatic
    fun init() = runCatching()
    {
        val javaVersion = System.getProperty("java.specification.version")
        if (javaVersion != "17")
        {
            logger.severe("Java version is $javaVersion, but SubQuiz requires Java 17.")
            shutdown(1, "Java version is $javaVersion, but SubQuiz requires Java 17.")
        }

        val file = File(this.javaClass.protectionDomain.codeSource.location.toURI())
        val lst = file.lastModified()
        val thread = Thread()
        {
            try
            {
                while (true)
                {
                    Thread.sleep(1000)
                    val newLst = file.lastModified()
                    if (newLst != lst)
                    {
                        logger.warning("检测到文件 ${file.name} 已被修改，程序将自动关闭")
                        shutdown(0, "File ${file.name} modified")
                    }
                }
            }
            catch (e: InterruptedException)
            {
                Thread.currentThread().interrupt()
            }
        }
        thread.isDaemon = true
        thread.start()
    }.onFailure()
    {
        logger.severe("启动监视器失败", it)
        shutdown(1, "Failed to start monitoring")
    }
}