#include <jni.h>

#include <godot_cpp/variant/utility_functions.hpp>

#include "utils.h"
#include "arcore_plugin_wrapper.h"

#undef JNI_PACKAGE_NAME
// TODO: Update to match plugin's package name
#define JNI_PACKAGE_NAME org_godotengine_plugin_android_arcore

#undef JNI_CLASS_NAME
#define JNI_CLASS_NAME ARCorePlugin

extern "C" {
    JNIEXPORT void JNICALL JNI_METHOD(helloWorld)(JNIEnv *env, jobject) {
        godot::UtilityFunctions::print("Hello GDExtension World!");
    }

    JNIEXPORT void JNICALL JNI_METHOD(initializeWrapper)(JNIEnv *env, jobject activity) {
        ARCorePluginWrapper::initialize_wrapper(env, activity);
    }

    JNIEXPORT void JNICALL JNI_METHOD(uninitializeWrapper)(JNIEnv *env, jobject activity) {
        ARCorePluginWrapper::uninitialize_wrapper(env);
    }
};
