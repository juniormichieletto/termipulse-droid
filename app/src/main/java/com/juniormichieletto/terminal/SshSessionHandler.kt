package com.juniormichieletto.terminal

import com.juniormichieletto.data.SshProfile
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SessionEvent {
    data class OutputReceived(val text: String, val isInput: Boolean = false) : SessionEvent()
    data class StatusChanged(val isConnected: Boolean, val message: String, val isLongJobRunning: Boolean = false) : SessionEvent()
    data class DirectoryChanged(val newPath: String) : SessionEvent()
    data class ErrorOccurred(val error: String) : SessionEvent()
}

class SshSessionHandler(
    val tabId: String,
    val profile: SshProfile,
    private val scope: CoroutineScope
) {
    private val _events = MutableSharedFlow<SessionEvent>(replay = 50)
    val events: SharedFlow<SessionEvent> = _events.asSharedFlow()

    private var jschSession: Session? = null
    private var channelShell: ChannelShell? = null
    private var inputStream: InputStream? = null
    private var outputStream: OutputStream? = null

    private var readJob: Job? = null
    private var longJob: Job? = null

    var currentDir: String = profile.defaultDirectory
        private set

    var isConnected: Boolean = false
        private set

    var isLongJobRunning: Boolean = false
        private set

    fun connect() {
        scope.launch(Dispatchers.IO) {
            _events.emit(SessionEvent.StatusChanged(false, "Connecting to ${profile.host}:${profile.port}...", false))

            if (profile.isSandbox) {
                delay(300)
                isConnected = true
                _events.emit(SessionEvent.StatusChanged(true, "Connected (Local Sandbox)", false))
                emitInitialSandboxBanner()
            } else {
                try {
                    val jsch = JSch()
                    if (profile.authType == "KEY" && profile.passwordOrKey.isNotBlank()) {
                        val keyBytes = profile.passwordOrKey.toByteArray()
                        jsch.addIdentity("custom_key", keyBytes, null, null)
                    }

                    val session = jsch.getSession(profile.username, profile.host, profile.port)
                    if (profile.authType == "PASSWORD" && profile.passwordOrKey.isNotBlank()) {
                        session.setPassword(profile.passwordOrKey)
                    }

                    val config = java.util.Properties()
                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    session.timeout = 10000

                    session.connect()
                    jschSession = session

                    val channel = session.openChannel("shell") as ChannelShell
                    channelShell = channel
                    inputStream = channel.inputStream
                    outputStream = channel.outputStream

                    channel.connect(5000)
                    isConnected = true
                    _events.emit(SessionEvent.StatusChanged(true, "Connected to ${profile.host}", false))

                    startSshReadLoop()
                } catch (e: Exception) {
                    isConnected = false
                    val errorMsg = e.localizedMessage ?: "Connection failed"
                    _events.emit(SessionEvent.ErrorOccurred("SSH Connect Error: $errorMsg"))
                    _events.emit(SessionEvent.StatusChanged(false, "Disconnected ($errorMsg)", false))

                    // Auto-fallback suggestion banner in output
                    _events.emit(SessionEvent.OutputReceived("\u001B[31m[ERROR]\u001B[0m Unable to reach ${profile.host}:${profile.port} directly."))
                    _events.emit(SessionEvent.OutputReceived("Detail: $errorMsg"))
                    _events.emit(SessionEvent.OutputReceived("\u001B[36m[TIP]\u001B[0m You can test commands in the \u001B[1mLocal Terminal Sandbox\u001B[0m or edit host settings."))
                }
            }
        }
    }

    private fun startSshReadLoop() {
        readJob = scope.launch(Dispatchers.IO) {
            val buffer = ByteArray(4096)
            val stream = inputStream ?: return@launch

            try {
                while (isActive && isConnected) {
                    val available = stream.available()
                    if (available > 0) {
                        val read = stream.read(buffer, 0, minOf(available, buffer.size))
                        if (read > 0) {
                            val text = String(buffer, 0, read)
                            _events.emit(SessionEvent.OutputReceived(text))
                        }
                    } else {
                        delay(50)
                    }
                }
            } catch (e: Exception) {
                if (isConnected) {
                    _events.emit(SessionEvent.OutputReceived("\r\n\u001B[31m[Session Disconnected]\u001B[0m ${e.message}"))
                    isConnected = false
                    _events.emit(SessionEvent.StatusChanged(false, "Disconnected", false))
                }
            }
        }
    }

    fun sendCommand(command: String) {
        scope.launch(Dispatchers.IO) {
            val trimmed = command.trim()
            val promptText = "\u001B[32m${profile.username}@${profile.host}\u001B[0m:\u001B[34m$currentDir\u001B[0m$ $trimmed"
            _events.emit(SessionEvent.OutputReceived(promptText, isInput = true))

            if (profile.isSandbox || !isConnected) {
                handleSandboxCommand(trimmed)
            } else {
                try {
                    val out = outputStream
                    if (out != null) {
                        out.write((trimmed + "\n").toByteArray())
                        out.flush()
                    } else {
                        handleSandboxCommand(trimmed)
                    }
                } catch (e: Exception) {
                    _events.emit(SessionEvent.OutputReceived("\u001B[31mWrite Error:\u001B[0m ${e.message}"))
                }
            }
        }
    }

    private suspend fun emitInitialSandboxBanner() {
        val banner = """
[1;32m╔══════════════════════════════════════════════════════════════════════╗
║                TermiPulse Local Terminal Sandbox                     ║
║              Android SSH Client & Command Emulator                   ║
╚══════════════════════════════════════════════════════════════════════╝[0m
[36mSystem:[0m Linux termipulse-node 6.1.0-android #1 SMP PREEMPT x86_64
[36mActive User:[0m [1m${profile.username}[0m (uid=1000)
[36mType [1;33m'help'[0m [36mto list built-in commands or [1;33m'stress-test'[0m [36mto start a background long job.[0m
""".trimIndent()
        _events.emit(SessionEvent.OutputReceived(banner))
        emitPrompt()
    }

    private suspend fun emitPrompt() {
        val prompt = "\u001B[32m${profile.username}@${profile.host}\u001B[0m:\u001B[34m$currentDir\u001B[0m$ "
        _events.emit(SessionEvent.OutputReceived(prompt))
    }

    private suspend fun handleSandboxCommand(cmd: String) {
        val parts = cmd.split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) {
            emitPrompt()
            return
        }

        val mainCmd = parts[0].lowercase()
        val args = parts.drop(1)

        when (mainCmd) {
            "help" -> {
                val helpMsg = """
[1;36mTermiPulse Built-in Commands:[0m
  [33mhelp[0m                     Show this quick command reference
  [33mls[0m [path]                 List directory contents
  [33mpwd[0m                      Print current working directory
  [33mcd[0m <dir>                  Change directory
  [33mwhoami[0m                   Display active user name
  [33muname -a[0m                 Display kernel & host architecture
  [33mdate[0m                     Display current server timestamp
  [33muptime[0m                   Show uptime and load average
  [33mtop[0m / [33mhtop[0m               Display live process monitor snapshot
  [33mdocker ps[0m                List active container instances
  [33mdf -h[0m                    Show disk partition space
  [33mgit status[0m               Check local git repository status
  [33mpython[0m [script]           Execute python script runner
  [33mstress-test[0m / [33mbuild-job[0m   Start a persistent background job (kept active in Foreground Service)
  [33mcancel-job[0m                Stop any active background long job
  [33mclear[0m                    Clear screen log buffer
  [33mecho[0m [text]               Print text to terminal
""".trimIndent()
                _events.emit(SessionEvent.OutputReceived(helpMsg))
            }

            "pwd" -> {
                _events.emit(SessionEvent.OutputReceived(currentDir))
            }

            "cd" -> {
                val target = args.firstOrNull() ?: "~"
                currentDir = when (target) {
                    "~" -> "~"
                    ".." -> if (currentDir.contains("/")) currentDir.substringBeforeLast("/") else "~"
                    else -> if (currentDir == "~") "~/$target" else "$currentDir/$target"
                }
                _events.emit(SessionEvent.DirectoryChanged(currentDir))
            }

            "whoami" -> {
                _events.emit(SessionEvent.OutputReceived(profile.username))
            }

            "uname" -> {
                _events.emit(SessionEvent.OutputReceived("Linux termipulse-node 6.8.0-31-generic #31-Ubuntu SMP PREEMPT_DYNAMIC x86_64 gnu/linux"))
            }

            "date" -> {
                val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US)
                _events.emit(SessionEvent.OutputReceived(sdf.format(Date())))
            }

            "uptime" -> {
                _events.emit(SessionEvent.OutputReceived(" 23:48:12 up 14 days,  6:22,  3 users,  load average: 0.14, 0.28, 0.35"))
            }

            "ls" -> {
                val lsOutput = """
[1;34mbin[0m/  [1;34mconfig[0m/  [1;32mdeploy.sh[0m*  [1;34mdocker-compose.yml[0m  [1;34mlogs[0m/  [1;32mmain.py[0m*  [36mREADME.md[0m
""".trimIndent()
                _events.emit(SessionEvent.OutputReceived(lsOutput))
            }

            "top", "htop" -> {
                val htopArt = """
[1;36mtop - 23:48:15 up 14 days, 6:22, 3 users, load average: 0.18, 0.22, 0.30[0m
Tasks: [1m142 total[0m, [1;32m  1 running[0m, [1m141 sleeping[0m
%Cpu(s): [1;32m 3.2 us[0m, [1;36m 1.1 sy[0m, [1m 0.0 ni[0m, [1;30m95.4 id[0m, [1;33m 0.3 wa[0m
MiB Mem : [1;32m 15920.4 total[0m, [1;33m  4812.1 free[0m, [1;36m  6102.8 used[0m
MiB Swap: [1;32m  4096.0 total[0m, [1;33m  4096.0 free[0m, [1;36m     0.0 used[0m

[1;37;44m  PID USER      PR  NI    VIRT    RES    SHR S  %CPU  %MEM     TIME+ COMMAND       [0m
 1240 termipul  20   0  892100 124000  42100 S   4.3   0.8   12:45.10 python3 main.py
  842 root      20   0 1420100 312000  89100 S   2.1   1.9  142:10.02 dockerd
 2104 termipul  20   0   14200   3400   2800 R   0.7   0.0    0:00.12 top
    1 root      20   0  168200  12400   8200 S   0.0   0.1    1:02.40 systemd
""".trimIndent()
                _events.emit(SessionEvent.OutputReceived(htopArt))
            }

            "docker" -> {
                if (args.firstOrNull() == "ps") {
                    val dockerOutput = """
[1mCONTAINER ID   IMAGE               COMMAND                  CREATED        STATUS        PORTS                  NAMES[0m
a8f1e29c011b   postgres:16-alpine  "docker-entrypoint.s…"   2 days ago     Up 2 days     0.0.0.0:5432->5432/tcp  termipulse-db
7b311fa8922c   redis:7-alpine      "docker-entrypoint.s…"   2 days ago     Up 2 days     0.0.0.0:6379->6379/tcp  termipulse-cache
992c10aef12d   nginx:alpine        "/docker-entrypoint.…"   2 days ago     Up 2 days     0.0.0.0:80->80/tcp      termipulse-proxy
""".trimIndent()
                    _events.emit(SessionEvent.OutputReceived(dockerOutput))
                } else {
                    _events.emit(SessionEvent.OutputReceived("Docker engine v26.1.1 (Client/Server operational)"))
                }
            }

            "df" -> {
                val dfOutput = """
[1mFilesystem     1K-blocks      Used Available Use% Mounted on[0m
/dev/root      101582232  38192012  58173836  40% /
tmpfs            8151244         0   8151244   0% /dev/shm
/dev/sda1         523248      6210    517038   2% /boot/efi
""".trimIndent()
                _events.emit(SessionEvent.OutputReceived(dfOutput))
            }

            "git" -> {
                val gitOutput = """
[1mOn branch main[0m
Your branch is up to date with 'origin/main'.

Changes not staged for commit:
  (use "git add <file>..." to update what will be committed)
  [31mmodified:   src/server/cluster.kt[0m
  [31mmodified:   config/production.env[0m

no changes added to commit (use "git add" and/or "git commit -a")
""".trimIndent()
                _events.emit(SessionEvent.OutputReceived(gitOutput))
            }

            "clear" -> {
                _events.emit(SessionEvent.OutputReceived("\u001B[2J\u001B[H"))
            }

            "echo" -> {
                _events.emit(SessionEvent.OutputReceived(args.joinToString(" ")))
            }

            "stress-test", "build-job" -> {
                startSimulatedLongJob(mainCmd)
            }

            "cancel-job" -> {
                if (isLongJobRunning) {
                    longJob?.cancel()
                    isLongJobRunning = false
                    _events.emit(SessionEvent.StatusChanged(true, "Connected", false))
                    _events.emit(SessionEvent.OutputReceived("\r\n[31m[JOB CANCELLED][0m Background long job terminated by user."))
                } else {
                    _events.emit(SessionEvent.OutputReceived("No long job currently running."))
                }
            }

            else -> {
                _events.emit(SessionEvent.OutputReceived("bash: $mainCmd: command not found. Type \u001B[33m'help'\u001B[0m for available commands."))
            }
        }

        if (!isLongJobRunning) {
            emitPrompt()
        }
    }

    private fun startSimulatedLongJob(jobType: String) {
        if (isLongJobRunning) {
            scope.launch {
                _events.emit(SessionEvent.OutputReceived("\u001B[33m[WARN]\u001B[0m A long job is already running in this tab. Type 'cancel-job' to abort."))
            }
            return
        }

        isLongJobRunning = true
        scope.launch {
            _events.emit(SessionEvent.StatusChanged(true, "Job Running ($jobType)", true))
            _events.emit(SessionEvent.OutputReceived("\r\n[1;32m[STARTING LONG JOB: $jobType][0m Session will persist in Background Foreground Service..."))

            longJob = scope.launch(Dispatchers.IO) {
                val totalSteps = 20
                for (step in 1..totalSteps) {
                    if (!isActive) break
                    delay(1200)

                    val percent = (step * 100) / totalSteps
                    val bar = "█".repeat(step) + "░".repeat(totalSteps - step)
                    val statusText = "[36m[$jobType][0m Step $step/$totalSteps [$bar] $percent% | Executing task compilation..."
                    _events.emit(SessionEvent.OutputReceived(statusText))
                }

                if (isActive) {
                    isLongJobRunning = false
                    _events.emit(SessionEvent.StatusChanged(true, "Connected (Job Finished)", false))
                    _events.emit(SessionEvent.OutputReceived("\r\n[1;32m[JOB COMPLETED SUCCESSFULLY][0m Task finished cleanly with exit code 0."))
                    emitPrompt()
                }
            }
        }
    }

    fun sendCtrlC() {
        scope.launch {
            if (isLongJobRunning) {
                longJob?.cancel()
                isLongJobRunning = false
                _events.emit(SessionEvent.StatusChanged(true, "Connected", false))
                _events.emit(SessionEvent.OutputReceived("^C\r\n[31m[SIGINT][0m Process interrupted."))
                emitPrompt()
            } else {
                _events.emit(SessionEvent.OutputReceived("^C"))
                emitPrompt()
            }
        }
    }

    fun disconnect() {
        isConnected = false
        isLongJobRunning = false
        readJob?.cancel()
        longJob?.cancel()

        scope.launch(Dispatchers.IO) {
            try {
                channelShell?.disconnect()
                jschSession?.disconnect()
            } catch (_: Exception) {}
            _events.emit(SessionEvent.StatusChanged(false, "Disconnected", false))
        }
    }
}
