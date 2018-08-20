REM run this script on the command line with cscript //nologo getmem.vbs

If WScript.Arguments.Count = 1 Then
	strComputer = WScript.Arguments.Item(0)
Else
	Wscript.Echo "Usage: cscript \\nologo getmem.vbs ServerName"
	Wscript.quit
End If

Set wbemServices = GetObject("winmgmts:\\" & strComputer)
Set wbemObjectSet = wbemServices.InstancesOf("Win32_LogicalMemoryConfiguration")

For Each wbemObject In wbemObjectSet
    WScript.Echo "Total Physical Memory (kb): " & wbemObject.TotalPhysicalMemory
Next