' Copyright 2004-2005 GroundWork OpenSource Solutions
'
' This program is free software; you can redistribute it and/or
' modify it under the terms of the GNU General Public License
' as published by the Free Software Foundation; either version 2
' of the License, or (at your option) any later version.
'
' This program is distributed in the hope that it will be useful,
' but WITHOUT ANY WARRANTY; without even the implied warranty of
' MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
' GNU General Public License for more details.
'
'
' Authors:
'     Caine Hörr of GroundWork Open Source Solutions  
'     Todd Smith of GroundWork Open Source Solutions 


' * * * * * * * * * * DISPLAY RESULTS WITHOUT AUTHENTICATION * * * * * * * * * * 
Function withoutAuth() On Error Resume Next
  strComputer = WScript.Arguments.Item(0)
  strService = WScript.Arguments.Item(1)
  
  Set objectInstance = GetObject("winmgmts:{impersonationLevel=impersonate}//" & strComputer & "/root/cimv2:Win32_Service=" & chr(34) & strService & chr(34))

  If objectInstance.Properties_("State").value = "Stopped" Then
    WScript.Echo "Critical: Service is Stopped"
    WScript.Quit(intCritical)
  ElseIf objectInstance.Properties_("State").value = "Running" Then
    WScript.Echo "OK: Service is Running"
    WScript.Quit(intOK)
  End If
End Function


' * * * * * * * * * * DISPLAY RESULTS WITH AUTHENTICATION * * * * * * * * * * 
Function withAuth() On Error Resume Next
  strComputer = WScript.Arguments.Item(0)
  strService = WScript.Arguments.Item(1)
  strUser = WScript.Arguments.Item(2)
  strPassword = WScript.Arguments.Item(3)
  
  Dim objWmi, objService, collService
  Dim strQuery

  Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
  Set objwmi = objSWbemLocator.ConnectServer (strComputer, "root\cimv2", strUser, strPassword)
  objwmi.Security_.ImpersonationLevel = 3

  strQuery = "SELECT DisplayName,State,StartMode FROM Win32_Service WHERE NAME='" & strService & "'"
  set collService = objWmi.ExecQuery(strQuery) 

  For Each objService In collService
    If objService.State = "Stopped" Then
      WScript.Echo "Critical: Service is Stopped"
      WScript.Quit(intCritical)
    ElseIf objService.State = "Running" Then
      WScript.Echo "OK: Service is Running"
      WScript.Quit(intOK)
    End If
  Next
End Function


' * * * * * * * * * * DISPLAY ONLINE HELP * * * * * * * * * * 
Function HelpSyntax()
  Wscript.Echo "Usage: cscript //nologo check_service.vbs ServerName ServiceName [UserName] [Password]"
  WScript.Echo "Usage: [optional parameters]"
End Function


'  * * * * * * * * * * MAIN PROGRAM  * * * * * * * * * * 
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3

If WScript.Arguments.Count = 2 Then
  Call withoutAuth()
ElseIf WScript.Arguments.Count = 4 Then
  Call withAuth()
Else
  Call HelpSyntax()
  Wscript.quit
End If