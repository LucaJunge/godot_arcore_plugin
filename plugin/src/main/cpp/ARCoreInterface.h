//
// Created by luca on 18.08.24.
//

#ifndef ARCOREPLUGIN_ARCOREINTERFACE_H
#define ARCOREPLUGIN_ARCOREINTERFACE_H

#include <godot_cpp/classes/xr_interface_extension.hpp>

namespace godot {
    class ARCoreInterface : public XRInterfaceExtension {
        GDCLASS(ARCoreInterface, XRInterfaceExtension);

    protected:
        static void _bind_methods();

    public:
        ARCoreInterface();
        ~ARCoreInterface();
    };
};

#endif //ARCOREPLUGIN_ARCOREINTERFACE_H
