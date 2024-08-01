// TODO: Update to match your plugin's package name.
package org.godotengine.plugin.android.arcore

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import android.view.Choreographer
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import org.godotengine.godot.Godot
import org.godotengine.godot.gl.GLSurfaceView
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot

/*
* Currently you have to start the app twice, as the permission flow is currently not working,
* search for @PermissionError in this file for explanation.
* Start the app once for accepting the permissions and a second time for creating the ARCore session
* and (trying to) update the frame
* */

class ARCorePlugin(godot: Godot): GodotPlugin(godot) {

    companion object {
        val TAG = ARCorePlugin::class.java.simpleName
        var requestARCoreInstall: Boolean = false
        const val CAMERA_REQUEST_CODE: Int = 100
        //
        var session : Session? = null
        var choreographer = Choreographer.getInstance()
        private val surfaceView: GLSurfaceView? = null

        init {
            try {
                Log.v(TAG, "Loading ${BuildConfig.GODOT_PLUGIN_NAME} library")
                System.loadLibrary(BuildConfig.GODOT_PLUGIN_NAME)
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Unable to load ${BuildConfig.GODOT_PLUGIN_NAME} shared library")
            }
        }
    }

    override fun onMainCreate(activity: Activity?): View? {
        super.onMainCreate(activity)

        // Check if all requirements for installation are met => (XR mode), Camera access and ARCore support
        if(arCoreRequirementsSatisfied()) {
            startARCore()
        } else {
            // This currently executes directly, it doesn't wait for arCoreRequirementsSatisfied()
            Toast.makeText(activity, "Please accept the camera permission for ARCore to work", Toast.LENGTH_LONG).show()
        }

        return null
    }

    // Called once at the start and upon every reopening of the app after
    // sending it to the background e.g. when installing ARCore from the Play Store
    override fun onMainResume() {
        Log.v(TAG, "onMainResume")
        super.onMainResume()

        try {
            when(ArCoreApk.getInstance().requestInstall(activity, requestARCoreInstall)) {
                ArCoreApk.InstallStatus.INSTALL_REQUESTED -> requestARCoreInstall = false
                else -> Unit
            }
        } catch (e: UnavailableUserDeclinedInstallationException) {
            Toast.makeText(activity, "Please install ARCore to use this app", Toast.LENGTH_LONG).show()
        } catch (e: UnavailableDeviceNotCompatibleException) {
            Toast.makeText(activity, "Please install ARCore to use this app", Toast.LENGTH_LONG).show()
        }

        /*if(session != null)  {
            session!!.resume()
        }

        if(surfaceView != null) {
            surfaceView.onResume()
        }*/
    }

    override fun onMainPause() {
        super.onMainPause()
        if(session != null) {
            surfaceView!!.onPause()
            session!!.pause()
        }
    }

    override fun onMainDestroy() {
        super.onMainDestroy()
        session!!.close()
    }

    private fun arCoreRequirementsSatisfied(): Boolean {
        // From old plugin: do we still need this mode check?
        /*if(XRMode.ARCORE != xrMode) {
         * return false
        }*/

        when {
            ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission
            }
            ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA) -> {
                Toast.makeText(activity, "You need to install ARCore to use this app.", Toast.LENGTH_LONG).show()
            }
            else -> {
                // You can directly ask for the permission
                ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            }
        }

        if(!hasCameraPermission(activity)) {
            return false
        }

        // ToDo: also handle the other cases like ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD
        // Assuming up-to-date and installed ARCore for now
        if(ArCoreApk.getInstance().checkAvailability(activity) != ArCoreApk.Availability.SUPPORTED_INSTALLED) {
            return false
        }

        return true
    }

    fun startARCore() {
    Log.v(TAG, "in startARCore")
        // Create a session here etc.
        session = createSession()   
    }

    override fun onMainRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>?,
        grantResults: IntArray?
    ) {
        super.onMainRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if(grantResults!!.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(TAG, "ARCore: Camera permission was granted")
                    // Permission is granted. Continue the action or workflow in your app
                    // TODO: How to continue the workflow here?
                    startARCore()
                }
            } else -> {
                Toast.makeText(activity, "This app requires the Camera permission to work", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun hasCameraPermission(activity: Activity?): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    // Creates a session and configures it (https://developers.google.com/ar/develop/java/session-config)
    fun createSession(): Session {
        var session = Session(activity)
        val config = Config(session)

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces

        session!!.configure(config)
        Log.v(TAG, session.toString())

        // Start the frame loop for debug here
        //startFrameUpdates()
        return session
    }

    private fun startFrameUpdates() {
        choreographer.postFrameCallback { frameTimeNanoSeconds ->
            updateARCoreFrame()

            choreographer.postFrameCallback { this }
        }
    }

    private fun updateARCoreFrame() {
        val frame: Frame
        try {
            frame = session!!.update()
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available during onDrawFrame", e)
            return
        }
        val camera: Camera = frame.getCamera()
        Log.v(TAG, frame.toString())
        Log.v(TAG, camera.toString())
    }

    override fun getPluginName() = BuildConfig.GODOT_PLUGIN_NAME

    override fun getPluginGDExtensionLibrariesPaths() = setOf("res://addons/${BuildConfig.GODOT_PLUGIN_NAME}/plugin.gdextension")

    /**
     * Example showing how to declare a native method that uses GDExtension C++ bindings and is
     * exposed to gdscript.
     *
     * Print a 'Hello World' message to the logcat.
     */
    @UsedByGodot
    private external fun helloWorld()   
}
