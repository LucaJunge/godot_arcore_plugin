//
// Created by luca on 18.08.24.
//

#include "arcore_interface.h"
#include <godot_cpp/core/class_db.hpp>
#include <godot_cpp/variant/utility_functions.hpp>
#include "utils.h"

using namespace godot;

void ARCoreInterface::_bind_methods() {
    ClassDB::bind_method(D_METHOD("_resume"), &ARCoreInterface::_resume);
    ClassDB::bind_method(D_METHOD("_pause"), &ARCoreInterface::_pause);
    ClassDB::bind_method(D_METHOD("start"), &ARCoreInterface::start);
}

ARCoreInterface::ARCoreInterface() {
    godot::UtilityFunctions::print("Hello from ARCoreInterface constructor!");
    m_ar_session = nullptr;
}

ARCoreInterface::~ARCoreInterface() {
    godot::UtilityFunctions::print("Goodbye from ARCoreInterface destructor!");
}

void ARCoreInterface::_pause() {
    godot::UtilityFunctions::print("ARCoreInterface::_pause");
    ALOGV("ARCoreInterface::_pause");
}

void ARCoreInterface::_resume() {
    godot::UtilityFunctions::print("ARCoreInterface::resume");
    ALOGV("ARCoreInterface::_resume");
}

void ARCoreInterface::start() {
    ALOGV("ARCoreInterface: cpp start");
}