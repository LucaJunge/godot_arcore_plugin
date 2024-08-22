extends CenterContainer

# TODO: Update to match your plugin's name
var _plugin_name = "ARCorePlugin"
var ARCorePlugin

func _ready():
	if Engine.has_singleton(_plugin_name):
		ARCorePlugin = Engine.get_singleton(_plugin_name)

		# This sets up access to the environment and godot classes
		ARCorePlugin.initializeWrapper()
	else:
		printerr("Couldn't find plugin " + _plugin_name)


func _on_start_ar_button_pressed():
	print("ARCoreInterfaceInstance")
	ARCoreInterfaceInstance.start()
	# This should be named "ARCoreInterface" but there is a name clash with the registered class "ARCoreInterface"
	#ARCoreInterfaceInstance.start()
