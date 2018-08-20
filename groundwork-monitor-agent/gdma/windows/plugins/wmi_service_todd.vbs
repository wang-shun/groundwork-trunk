Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3


If WScript.Arguments.Count = 2 Then

	strComputer = WScript.Arguments.Item(0)
	strService = WScript.Arguments.Item(1)
	Set oInstance = GetObject("winmgmts:{impersonationLevel=impersonate}//" &_ 
        strComputer & "/root/cimv2:Win32_Service=" & chr(34) & strService & chr(34))

	If oInstance.Properties_("State").value = "Stopped" Then
		WScript.Echo "Critical: Service is Stopped"
		WScript.Quit(intCritical)
	ElseIf oInstance.Properties_("State").value = "Running" Then
		WScript.Echo "OK: Service is Running"
		WScript.Quit(intOK)
	End If

ElseIf WScript.Arguments.Count = 4 Then
	strComputer = WScript.Arguments.Item(0)
	strService = WScript.Arguments.Item(1)
	strUser	= WScript.Arguments.Item(2)
	strPassword = WScript.Arguments.Item(3)

		WScript.Echo "Username and password not implemented yet."
		WScript.Quit(intCritical)
Else
	Wscript.Echo "Usage: cscript \\nologo check_service.vbs ServerName ServiceName [UserName] [Password]"
	WScript.Echo "Usage: [optional parameters]"
	Wscript.quit
End If