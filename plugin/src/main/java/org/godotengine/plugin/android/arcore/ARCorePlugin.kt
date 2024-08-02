// TODO: Update to match your plugin's package name.
package org.godotengine.plugin.android.arcore

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.Choreographer
import android.view.Display
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Camera
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.TextureNotSetException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException
import org.godotengine.godot.Godot
import org.godotengine.godot.gl.GLSurfaceView
import org.godotengine.godot.gl.GodotRenderer
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ARCorePlugin(godot: Godot): GodotPlugin(godot) {

    /**
     * The surfaceView is passed to the Sample Renderer in HelloArActivity.java...
     * We need to call setRenderer (and pass a renderer?)
     * Change the pixel format?
     * For now: setRenderMode to on-demand?
     */

    companion object {
        val TAG = ARCorePlugin::class.java.simpleName
        var requestARCoreInstall: Boolean = false
        const val CAMERA_REQUEST_CODE: Int = 100
        var session : Session? = null
        private var surfaceView: GLSurfaceView? = null
        private var hasSetTextureNames: Boolean = false
        private var cameraTextureId: Int = -1
        private var textureBuffer: IntBuffer? = null
        private var viewportWidth: Int = -1
        private var viewportHeight: Int = -1
        private var viewportChanged: Boolean = false

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
        //windowManager = activity!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        surfaceView = GLSurfaceView(activity)
        // I need to give a GLSurfaceView.Renderer and somehow, implementing the interface
        // in an own class does not seem to be accepted...
        surfaceView!!.setRenderer(GodotRenderer())

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
        super.onMainResume()

        // Note that order matters - see the note in onPause(), the reverse applies here.
        if(session != null) {
            session!!.resume()
            surfaceView!!.onResume()
        }

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
    }

    override fun onMainPause() {
        super.onMainPause()
        if(session != null) {
            // Note that the order matters - GLSurfaceView is paused first so that it does not try
            // to query the session. If Session is paused before GLSurfaceView, GLSurfaceView may
            // still call session.update() and get a SessionPausedException.
            surfaceView!!.onPause()
            session!!.pause()
        }
    }

    override fun onMainDestroy() {
        if(session != null) {
            // Explicitly close ARCore Session to release native resources.
            // Review the API reference for important considerations before calling close() in apps with
            // more complicated lifecycle requirements:
            // https://developers.google.com/ar/reference/java/arcore/reference/com/google/ar/core/Session#close()
            session!!.close()
            session = null
        }
        super.onMainDestroy()
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
        // Create a session to use
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
                    // TODO: How to correctly continue the workflow here? For now just starting ARCore
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

        return session
    }

    override fun onGLSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onGLSurfaceChanged(gl, width, height)
        onSurfaceChanged(width, height)
    }

    override fun onGLSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onGLSurfaceCreated(gl, config)
        textureBuffer = IntBuffer.wrap(intArrayOf(100*100))
        gl!!.glGenTextures(1, textureBuffer )
        gl.glBindTexture(GL10.GL_TEXTURE_2D, cameraTextureId)
    }

    fun updateSessionIfNeeded(session: Session) {
        if (viewportChanged) {
            //val displayRotation: Int = display.rotation
            // hardcoded values for the display width and height here for now...
            session.setDisplayGeometry(0, 1080, 2400)
            viewportChanged = false
        }
    }

    override fun onGLDrawFrame(gl: GL10?) {

        // Do we call the super up here or later?
        super.onGLDrawFrame(gl)

        if(session == null) {
            return
        }

        updateSessionIfNeeded(session!!)

        if (!hasSetTextureNames) {
            session!!.setCameraTextureNames(intArrayOf(cameraTextureId))
            hasSetTextureNames = true;
        }

        var frame: Frame;

        try {
            var displayRotation: Int = 0 //display.rotation
            session!!.setDisplayGeometry(displayRotation, viewportWidth, viewportHeight)

            frame = session!!.update()

            var hitResultList: List<HitResult>  = frame.hitTest(100.0F, 100.0F)

            if(hitResultList.isNotEmpty()) {
                var nearestHit = hitResultList[0]

                var distanceToCamera: Float = nearestHit.distance
                var pose: Pose = nearestHit.hitPose
                Log.v(TAG, "Distance from camera to hit: $distanceToCamera")
                Log.v(TAG, "Pose of the hit: $pose")
            }

        } catch(e: CameraNotAvailableException) {
            Log.e(TAG, "ARCorePlugin: Camera was not available during onGLDrawFrame", e)
        } catch(e: TextureNotSetException) {
            Log.v(TAG, "ARCorePlugin: The texture was not set")
        }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        viewportWidth = width
        viewportHeight = height
        viewportChanged = true
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
