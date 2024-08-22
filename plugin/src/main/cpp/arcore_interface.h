//
// Created by luca on 18.08.24.
//

#ifndef ARCOREPLUGIN_ARCORE_INTERFACE_H
#define ARCOREPLUGIN_ARCORE_INTERFACE_H

#include "include/arcore_c_api.h"
#include <godot_cpp/classes/xr_interface_extension.hpp>

namespace godot {
    class ARCoreInterface : public XRInterfaceExtension {
        GDCLASS(ARCoreInterface, XRInterfaceExtension);

    protected:
        static void _bind_methods();

    public:
        ARCoreInterface();
        ~ARCoreInterface();
        void _resume();
        void _pause();
        void start();

    private:
        ArSession *m_ar_session;
        //ArFrame *m_ar_frame;
    };
};

#endif //ARCOREPLUGIN_ARCORE_INTERFACE_H
