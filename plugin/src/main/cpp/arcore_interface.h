//
// Created by luca on 11.08.24.
//

#ifndef ARCOREPLUGIN_ARCORE_INTERFACE_H
#define ARCOREPLUGIN_ARCORE_INTERFACE_H

#include "godot_cpp/classes/xr_interface_extension.hpp"
#include "include/arcore_c_api.h"

namespace godot {
    class ARCoreInterface : public XRInterfaceExtension {
        GDCLASS(ARCoreInterface, XRInterfaceExtension);

    public:
        enum InitStatus {
            NOT_INITIALISED,
            START_INITIALISE,
            INITIALISED,
            INITIALISE_FAILED
        };

        static void _bind_methods();

        ARCoreInterface();
        virtual ~ARCoreInterface();

        void _resume();

        void _pause();
    };
}

#endif //ARCOREPLUGIN_ARCORE_INTERFACE_H
