extends Node3D

@onready var csgMesh: CSGMesh3D = $CSGMesh3D

# TODO: Update to match your plugin's name
var _plugin_name = "ARCorePlugin"
var ARCorePlugin

func _ready():
	if Engine.has_singleton(_plugin_name):
		ARCorePlugin = Engine.get_singleton(_plugin_name)
	else:
		printerr("Couldn't find plugin " + _plugin_name)
		
	$Camera3D.look_at(Vector3(0, 0, 0))

func _on_Button_pressed():
	if ARCorePlugin:
		# TODO: Update to match your plugin's API
		ARCorePlugin.helloWorld()
	
func _process(delta):
	if ARCorePlugin:
		var hitResultDictionary = ARCorePlugin.getHitResultsArray()
		csgMesh.position.x = hitResultDictionary["tx"]
		csgMesh.position.y = hitResultDictionary["ty"]
		csgMesh.position.z = hitResultDictionary["tz"]

