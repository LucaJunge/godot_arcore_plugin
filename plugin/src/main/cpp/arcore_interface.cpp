//
// Created by luca on 11.08.24.
//

#include "utils.h"

#include "arcore_interface.h"
#include "arcore_plugin_wrapper.h"

using namespace godot;

// ClassDB::bind_method(D_METHOD("name"), &ARCoreInterface::methodname));
void ARCoreInterface::_bind_methods() {
    ClassDB::bind_method(D_METHOD("_resume"), &ARCoreInterface::_resume);
    ClassDB::bind_method(D_METHOD("_pause"), &ARCoreInterface::_pause);
}

ARCoreInterface::ARCoreInterface() {
    // Initialize variables here
    ALOGV("ARCorePlugin C++: Constructor");
}

ARCoreInterface::~ARCoreInterface() {
    ALOGV("ARCorePlugin C++: Destructor");
}