On Error Resume Next

Const wbemFlagReturnImmediately = &h10
Const wbemFlagForwardOnly = &h20

If WScript.Arguments.Count = 1 Then

   strComputer = WScript.Arguments.Item(0)
Else
   WScript.Echo "Usage: cscript //nologo show_drives.vbs [servername]"
   Wscript.quit
End If

Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\CIMV2")
Set colItems = objWMIService.ExecQuery("SELECT * FROM Win32_LogicalDisk", "WQL", _
                                          wbemFlagReturnImmediately + wbemFlagForwardOnly)

Dim LocalDisks

For Each objItem In colItems
   If objItem.DriveType = 3 Then
	If LocalDisks = "" Then
		LocalDisks = objItem.Name
	Else
		LocalDisks = LocalDisks & " " & objItem.Name
	End If
   End If
Next

WScript.Echo LocalDisks
