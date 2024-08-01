// TODO: Update to match your plugin's package name.
package org.godotengine.plugin.android.arcore

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.Choreographer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
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
    companion object : AppCompatActivity() {
        val TAG = ARCorePlugin::class.java.simpleName
        const val CAMERA_REQUEST_CODE: Int = 100
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

        // @PermissionError
        //Error: It can't override onRequestPermissionsResult, as this plugin inherits from GodotPlugin
        // which seems to not inherit from ActivityCompat.OnRequestPermissionsResultCallback...
        // I was trying to put it in this companion object, but I can't reach the methods outside...
        /*override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            results: IntArray
        ) {
            super.onRequestPermissionsResult(requestCode, permissions, results)
            onRequestPermissionsResult(requestCode, permissions, results)
            if (!hasCameraPermission(activity)) {
                Toast.makeText(activity, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show()
                if (!shouldShowRequestPermissionRationale(activity)) {
                    // Permission denied with checking "Do not ask again".
                    activity?.let { launchPermissionSettings(it) }
                }
                //      finish()
            }
        }*/
    }


    override fun onMainCreate(activity: Activity?): View? {
        super.onMainCreate(activity)

        // We need some checks to figure out if ARCore can be initialized
        // https://developer.android.com/training/permissions/requesting#already-granted

        // Get Camera Permission
        if (activity?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.CAMERA) } != PackageManager.PERMISSION_GRANTED) {
            if (activity != null) {
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
            }
            else {
                Log.v(TAG, "ARCore Session will be created")
                createSession()
                // Todo: Check if ARCore is supported on the device
                // ...
            }
        } else {
            // Permission is granted, create the session
            Log.v(TAG, "ARCore Session will be created")
            createSession()
        }


        return null
    }

    private fun hasCameraPermission(activity: Activity?): Boolean {
        return (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun shouldShowRequestPermissionRationale(activity: Activity?): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.CAMERA)
    }

    private fun launchPermissionSettings(activity: Activity) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.setData(Uri.fromParts("package", activity.packageName, null))
        activity.startActivity(intent)
    }

    fun createSession() {
        session = Session(activity)
        val config = Config(session)

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces

        session!!.configure(config)
        Log.v(TAG, session.toString())

        // Start the frame loop for debug here
        startFrameUpdates()
    }

    override fun onMainPause() {
        super.onMainPause()
        if(session != null) {
            surfaceView!!.onPause()
            session     !!.pause()
        }
    }

    override fun onMainDestroy() {
        super.onMainDestroy()
        session!!.close()
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


    // Called once at the start and upon every reopening of the app after
    // sending it to the background e.g. when installing ARCore from the Play Store
    override fun onMainResume() {
        super.onMainResume()
        if(session != null)  {
            session!!.resume()
        }

        if(surfaceView != null) {
            surfaceView.onResume()
        }
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
