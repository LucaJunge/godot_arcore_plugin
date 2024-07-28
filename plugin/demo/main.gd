extends CenterContainer

# TODO: Update to match your plugin's name
var _plugin_name = "ARCorePlugin"
var ARCorePlugin

func _ready():
	if Engine.has_singleton(_plugin_name):
		ARCorePlugin = Engine.get_singleton(_plugin_name)
	else:
		printerr("Couldn't find plugin " + _plugin_name)

func _on_Button_pressed():
	if ARCorePlugin:
		# TODO: Update to match your plugin's API
		ARCorePlugin.helloWorld()
