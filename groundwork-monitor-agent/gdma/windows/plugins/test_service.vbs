Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3

Function CheckServiceWithAuth( strComputer, strUser,strPassword,strService )
On Error Resume Next
    Dim objWmi, objService, collServices
    Dim strQuery

    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    Set objwmi = objSWbemLocator.ConnectServer _
	(strComputer, "root\cimv2",strUser, strPassword)
    objwmi.Security_.ImpersonationLevel = 3

    strQuery = "select * from Win32_Service"
WScript.Echo strQuery
    set collService = objWmi.ExecQuery( strQuery ) 
WScript.Echo collService
    For Each objService in collService
WScript.Echo objService
	If UCase( objService.Name ) = UCase( strService ) Then
WScript.Echo objService.Name	

            CheckServiceWithAuth = True
            Exit Function
        End If
    Next
    CheckServiceWithAuth = False
End Function



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

	i=CheckServiceWithAuth(strComputer,strService,strUser,strPassword)
WScript.Echo i
	If i=True Then
		WScript.Echo "OK: Service is Running"
		WScript.Quit(intOK)
	ElseIf i=False Then
		WScript.Echo "Critical: Service is Stopped"
		WScript.Quit(intCritical)
	End If
Else
	Wscript.Echo "Usage: cscript \\nologo check_service.vbs ServerName ServiceName [UserName] [Password]"
	WScript.Echo "Usage: [optional parameters]"
	Wscript.quit
End If