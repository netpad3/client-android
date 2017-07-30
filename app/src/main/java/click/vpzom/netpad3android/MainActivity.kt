package click.vpzom.netpad3android

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.InputDevice
import android.view.MotionEvent
import android.widget.EditText

class MainActivity : AppCompatActivity() {

    private lateinit var addressField : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        addressField = findViewById(R.id.editText) as EditText
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        println("MOTION")
        if ((event.source and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) {
            getHandler(event.device).handleMotion(event)
            return true
        }
        else {
            println("Not gamepad, "+event.source)
        }
        return super.onGenericMotionEvent(event)
    }

    private val handlers = SparseArray<NetpadHandler>()

    private fun getHandler(device: InputDevice): NetpadHandler {
        val id = device.id
        var tr = handlers.get(id)
        if(tr == null) {
            tr = NetpadHandler(device, addressField)
            handlers.put(id, tr)
        }
        return tr
    }
}