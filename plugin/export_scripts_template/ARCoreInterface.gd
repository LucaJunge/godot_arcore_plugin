@tool
extends Node

# This is the autoload that is set up when enabling the plugin
# It also communicates between ARCore and Godot
var arcore : ARCoreInterface

func _enter_tree():
	# Get a reference to the c++ ARCoreInterface
	arcore = ARCoreInterface.new()

	if arcore:
		XRServer.add_interface(arcore)

func _exit_tree():
	if arcore:
		XRServer.remove_interface(arcore)
		arcore = null

# Every function here can be called in other .gd scripts with `ARCoreInterfaceInstance.method_name()`

func get_interface():
	return arcore

func start():
	if arcore.initialize():
		get_viewport().use_xr = true

func get_tracking_status():
	return arcore.get_tracking_status()