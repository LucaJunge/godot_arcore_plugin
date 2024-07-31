// TODO: Update to match your plugin's package name.
package org.godotengine.plugin.android.arcore

import android.util.Log
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.UsedByGodot

import android.app.Activity
import android.view.View
import com.google.ar.core.ArCoreApk

class ARCorePlugin(godot: Godot): GodotPlugin(godot) {

    companion object {
        val TAG = ARCorePlugin::class.java.simpleName
            
        init {
            try {
                Log.v(TAG, "Loading ${BuildConfig.GODOT_PLUGIN_NAME} library")
                System.loadLibrary(BuildConfig.GODOT_PLUGIN_NAME)
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Unable to load ${BuildConfig.GODOT_PLUGIN_NAME} shared library")
            }
        }
    }

    override fun onMainCreate(activity: Activity): View? {
        val apk = ArCoreApk.getInstance()
        Log.v(TAG, "ARCoreApk instance: $apk")
        return null
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
