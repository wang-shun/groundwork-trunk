If WScript.Arguments.Count = 1 Then

	strComputer = WScript.Arguments.Item(0)

	Set cInstances = GetObject("winmgmts:{impersonationLevel=impersonate}//" &_ 
	strComputer & "/root/cimv2:Win32_Service").Instances_

For Each oInstance In cInstances
	WScript.Echo "Name:" & vbTab & vbTab & chr(34) & oInstance.Properties_("Name").Value & chr(34)
	WScript.Echo "DisplayName:" & vbTab & chr(34) & oInstance.Properties_("DisplayName").Value & chr(34)
	WScript.Echo "StartMode:" & vbTab & oInstance.Properties_("StartMode").Value
	WScript.Echo "State:" & vbTab & vbTab & oInstance.Properties_("State").Value
	WScript.Echo
Next
	
Else
	Wscript.Echo "Usage: cscript \\nologo check_service.vbs ServerName"
	Wscript.quit
End If