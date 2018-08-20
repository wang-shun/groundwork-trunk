' check_counter.vbs
'
' Copyright 2004-2018 GroundWork Inc. (www.gwos.com)
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

' Get Perfmon Counter via WMI
Function CheckCounter( strComputer, strClass, strProperty )
    On Error Resume Next
    Dim objWMIService, colOS, objOS
    Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
    if (Err.Number <> 0) then
	f_Error("Getting WMI object failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    Set colOS = objWMIService.ExecQuery("Select " & strProperty & " From " & strClass)
    if (Err.Number <> 0) then
	f_Error("Query failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    for Each objOS In colOS
	if strComp( objOS.Properties_(strProperty), "", 0 ) = 0 then
	    if (Err.Number <> 0) then
		f_Error("Property comparison failed with error number " & Err.Number & ":  " & Err.Description)
	    else
		f_Error("Found empty/missing value for property " & strProperty)
	    end if
	else
	    CheckCounter = objOS.Properties_(strProperty)
	    exit function
	end if
    next
    if (Err.Number <> 0) then
	f_Error("Walking the query results failed with error number " & Err.Number & ":  " & Err.Description)
    else
	f_Error("Cannot find value for property " & strProperty)
    end if
End Function

' Get Perfmon Counter via WMI on Remote Machine
Function CheckCounterEx( strComputer, strClass, strProperty, strUser, strPassword )
    On Error Resume Next
    Dim objWMIService, colOS, objOS, objSWbemLocator

    ' Check on the Local Computer.  I can't say I understand why you would
    ' check locally for a value supposedly found on a remote machine.
    Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
    if (Err.Number <> 0) then
	f_Error("Getting WMI object failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    ' We must retrieve the Name explicitly to ensure that it is available for later reference as the objOS.Name value.
    Set colOS = objWMIService.ExecQuery("Select Name, " & strProperty & " From " & strClass)
    if (Err.Number <> 0) then
	f_Error("Query failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    for Each objOS In colOS
	if strComp( objOS.Name, strComputer, 1 ) = 0 then
	    if strComp( objOS.Properties_(strProperty), "", 0 ) = 0 then
		if (Err.Number <> 0) then
		    f_Error("Property comparison failed with error number " & Err.Number & ":  " & Err.Description)
		else
		    f_Error("Found empty/missing value for property " & strProperty)
		end if
	    else
		' If objOS.Properties_(strProperty) were a collection and not just a scalar value,
		' we might need to branch and use this instead:
		' CheckCounterEx = objOS.Properties_(strProperty)(".")
		CheckCounterEx = objOS.Properties_(strProperty)
		exit Function
	    end if
	end if
    next

    ' We must destroy the object we created above, because it won't be destroyed if
    ' the call to assign it below fails.  That would leave us still referencing the
    ' original copy of this object, which would mean we would be fetching values for
    ' the local machine, not the remote machine, and not even knowing it.
    Set objWMIService = Nothing

    ' Check on the Remote Computer.
    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    if (Err.Number <> 0) then
	f_Error("Creating WMI object failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    Set objWMIService = objSWbemLocator.ConnectServer( strComputer, "root\cimv2", strUser, strPassword )
    if (Err.Number <> 0) then
	f_Error("Connecting to host " & strComputer & " failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    ' Double-check connectivity.
    if objWMIService Is Nothing then
	f_Error("Could not connect to host " & strComputer)
    end if
    Set colOS = objWMIService.ExecQuery("select " & strProperty & " From " & strClass)
    if (Err.Number <> 0) then
	f_Error("Query failed with error number " & Err.Number & ":  " & Err.Description)
    end if
    for Each objOS In colOS
	if strComp( objOS.Properties_(strProperty), "", 0 ) = 0 then
	    if (Err.Number <> 0) then
		f_Error("Property comparison failed with error number " & Err.Number & ":  " & Err.Description)
	    else
		f_Error("Found empty/missing value for property " & strProperty)
	    end if
	else
	    CheckCounterEx = objOS.Properties_(strProperty)
	    exit function
	end if
    next

    if (Err.Number <> 0) then
	f_Error("Walking the query results failed with error number " & Err.Number & ":  " & Err.Description)
    else
	f_Error("Cannot find value for property " & strProperty)
    end if
End Function

'**********************
'** Error Subroutine
'**********************
Function f_Error(strMessage)
    Wscript.Echo "Error!  " & strMessage
    Wscript.Quit(intError)
End Function

Function f_help()
    Dim str
    str="This script checks a PerfMon counter via WMI.  If your Local Machine has the same Administrator"&vbCrlF
    str=str&"account and password as the Remote Machine, you don't have to specify the last two parameters."&vbCrlF
    str=str&vbCrlF
    str=str&"All options must be specified in exactly the order shown.  All options must be"&vbCrlF
    str=str&"present except for possibly the -user and -pass options, as just described."&vbCrlF
    str=str&vbCrlF
    str=str&"The current implementation always assumes that a higher counter value is worse,"&vbCrlF
    str=str&"when interpreting the warning and critical thresholds."&vbCrlF
    str=str&vbCrlF
    str=str&"cscript //nologo check_counter.vbs -h hostname -class WMI_class -prop Property"&vbCrlF
    str=str&"    -w warning_level -c critical_level [-user username -pass password]"&vbCrlF
    str=str&vbCrlF
    str=str&"-h hostname         Host name; ""."" may be used as a generic alias for the current machine."&vbCrlF
    str=str&"-class WMI_class    WMI Class, e.g., Win32_PerfRawData_PerfOS_Memory."&vbCrlF
    str=str&"-prop Property      WMI Propery, e.g., PagesPersec."&vbCrlF
    str=str&"-w warning_level    Warning threshold."&vbCrlF
    str=str&"-c critical_level   Critical threshold."&vbCrlF
    str=str&"-user username      Account Administrator on Remote Machine."&vbCrlF
    str=str&"-pass password      Password for Account Administrator on Remote Machine."&vbCrlF
    str=str&vbCrlF
    str=str&"Example:"&vbCrlF
    str=str&vbCrlF
    str=str&"cscript //nologo check_counter.vbs -h . -class Win32_PerfRawData_PerfOS_Memory"&vbCrlF
    str=str&"    -prop PagesPersec -w 100000 -c 200000 [-user .\Administrator -pass password] "
    Wscript.Echo str
End Function

'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer
Dim sClass
Dim sProperty
Dim result

' Constants for return values
Const intOK       = 0
Const intWarning  = 1
Const intCritical = 2
Const intError    = 3

On Error Resume Next
for i = 0 to WScript.Arguments.Count-1
    arg(i) = WScript.Arguments( i )
next

' We use explicit line-continuation characters here even where modern VB doc says you should not need them.
' That's because we need to be portable to older versions of VB where the modern doc does not apply.
if (Err.Number <> 0) then
    f_Error("Error number " & Err.Number & ":  " & Err.Description)
elseif Wscript.Arguments.Count=1 and (UCase(arg(0))="-H" Or UCase(arg(0))="--HELP") then
    f_help()
elseif _
	Wscript.Arguments.Count=10 And _
	UCase(arg(0))="-H"         And _
	UCase(arg(2))="-CLASS"     And _
	UCase(arg(4))="-PROP"      And _
	UCase(arg(6))="-W"         And _
	UCase(arg(8))="-C" _
    then
    ' Case 1, probably a local machine (no credentials supplied)
    sComputer = arg(1)
    sClass    = arg(3)
    sProperty = arg(5)
    result    = CheckCounter( sComputer, sClass, sProperty )
    if (Err.Number <> 0) then
	f_Error("Error number " & Err.Number & ":  " & Err.Description)
    elseif (CLng(result) < CLng(arg(7))) then
	prefix = "OK - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	WScript.Quit(intOK)
    elseif (CLng(result) < CLng(arg(9))) then
	prefix = "Warning - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	Wscript.Quit(intWarning)
    else
	prefix = "Critical - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	Wscript.Quit(intCritical)
    end if
elseif _
	Wscript.Arguments.Count=14 And _
	UCase(arg(0))="-H"         And _
	UCase(arg(2))="-CLASS"     And _
	UCase(arg(4))="-PROP"      And _
	UCase(arg(6))="-W"         And _
	UCase(arg(8))="-C"         And _
	UCase(arg(10))="-USER"     And _
	UCase(arg(12))="-PASS" _
    then
    ' Case 2, probably a remote machine (credentials supplied)
    sComputer = arg(1)
    sClass    = arg(3)
    sProperty = arg(5)
    result    =  CheckCounterEx( sComputer, sClass, sProperty, arg(11), arg(13) )
    if (Err.Number <> 0) then
	f_Error("Error number " & Err.Number & ":  " & Err.Description)
    elseif (CLng(result) < CLng(arg(7))) then
	prefix = "OK - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	WScript.Quit(intOK)
    elseif (CLng(result) < CLng(arg(9))) then
	prefix = "Warning - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	Wscript.Quit(intWarning)
    else
	prefix = "Critical - "
	Wscript.Echo prefix & sProperty & " = " & result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(7)) & ";" & CLng(arg(9)) & ";;"
	Wscript.Quit(intCritical)
    end if
else
    f_Error("Bad usage; try ""cscript //nologo check_counter.vbs --help""")
end if
