package click.vpzom.netpad3android

import android.util.Log
import android.util.SparseBooleanArray
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.EditText
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.util.*

class NetpadHandler(device: InputDevice, val addressField: EditText) {
    private val buttons: SparseBooleanArray = SparseBooleanArray()
    private val axes: MutableMap<InputDevice.MotionRange, Int> = HashMap()

    private val socket: DatagramSocket
    private val thread: Thread

    init {
        val startKey = KeyEvent.KEYCODE_0
        val endKey = KeyEvent.getMaxKeyCode()
        val allKeys = IntArray(endKey - startKey) { i -> startKey + i }
        val result = device.hasKeys(*allKeys)
        result.mapIndexed { index, found -> index * 2 + if (found) 1 else 0 }
                .filter { it % 2 == 1 }
                .map { it / 2 }
                .forEach { buttons.put(it, false) }

        device.motionRanges
                .forEach { axes.put(it, 0) }

        socket = DatagramSocket()
        thread = Thread {
            while(true) {
                val data = msg?.toByteArray()
                if (data != null) {
                    synchronized(msg!!) {
                        try {
                            socket.send(DatagramPacket(data, data.size, InetSocketAddress(addressField.text.toString(), 6723)))
                        } catch(ex: Exception) {
                            Log.w("NPHandler", ex)
                        }
                        msg = null
                    }
                }
                Thread.sleep(0)
            }
        }
        thread.start()
    }

    private var msg: String? = null

    private fun send() {
        var buttonValue = Math.pow(2.0, buttons.size().toDouble())
        (0..buttons.size() - 1).forEach {
            if (buttons.valueAt(it)) {
                buttonValue += Math.pow(2.0, it.toDouble())
            }
        }
        msg = axes.values.joinToString(",") + ";" + buttonValue.toInt().toString(16)
        Log.i("NPHandler", msg)
    }

    fun handleMotion(event: MotionEvent) {
        println("{Handling motion")
        axes.keys.forEach {
            axes[it] = ((event.getAxisValue(it.axis) - it.min) / (it.max - it.min) * 65535 - 32767).toInt()
        }
        send()
    }

    fun handleKey(event: KeyEvent, value: Boolean) {
        println("key "+event.keyCode)
        println("{Handling key")
        buttons.put(event.keyCode, value)
        send()
    }
}