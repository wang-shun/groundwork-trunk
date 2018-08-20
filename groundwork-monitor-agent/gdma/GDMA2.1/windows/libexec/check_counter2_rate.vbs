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

' Get Perfmon Counter via WMI
Function CheckCounter(strComputer,strClass,strWhere,strProperty)
	On Error Resume Next
	Dim objWMIService, colOS,objOS
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select " & strProperty & " From " & strClass & " WHERE Name='" & strWhere & "'")
	if (colOS.count = 0) then
	  f_Error()
	end if
	for Each objOS In colOS
	    OldValue = objOS.Properties_(strProperty)
	next
	WScript.sleep 1000
	Set colOS = objWMIService.ExecQuery("Select " & strProperty & " From " & strClass & " WHERE Name='" & strWhere & "'")
	for Each objOS In colOS
	    NewValue = objOS.Properties_(strProperty)
	next
	CheckCounter = NewValue - OldValue
End Function



' Get Perfmon Counter via WMI on Remote Machine
Function CheckCounterEx(strComputer,strClass,strWhere,strProperty,strUser,strPassword)
	On Error Resume Next	
	Dim objWMIService, colOS,objOS,objSWbemLocator		
	

	' Check for Remote Computer	
	
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")	
	Set objWMIService = objSWbemLocator.ConnectServer _
	(strComputer, "root\cimv2" , strUser, strPassword )	
	Set colOS = objWMIService.ExecQuery("Select " & strProperty & " From " & strClass & " WHERE Name='" & strWhere & "'")
	if (colOS.count = 0) then
	  f_Error()
	end if
	for Each objOS In colOS
	    OldValue = objOS.Properties_(strProperty)
	next
	WScript.sleep 1000
	Set colOS = objWMIService.ExecQuery("Select " & strProperty & " From " & strClass & " WHERE Name='" & strWhere & "'")
	for Each objOS In colOS
	    NewValue = objOS.Properties_(strProperty)
	next
	CheckCounterEx = NewValue - OldValue
End Function


'**********************
'Error Function
'**********************
Function f_Error()
  Wscript.echo "Error!"
  Wscript.Quit(intError)
End Function

Function f_help()
  Dim str
  str="Check WMI Property. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript check_counter2.vbs -h hostname -class WMI_class -where clause -prop Property -w warning_level -c critical_level [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h hostname                 Host name."&vbCrlF
  str=str&"-class WMI_class            WMI Class, e.g. Win32_PerfRawData_PerfOS_Memory."&vbCrlF
  str=str&"-where clause               WHERE clause, e.g. _Total."&vbCrlF
  str=str&"-prop Property              WMI Propery, e.g. PagesPerSec."&vbCrlF
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level           Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript check_counter2.vbs -h Ser1 -class Win32_PerfRawData_PerfOS_Memory -where Name='_Total' -prop PagesPerSec -w 100000 -c 200000 [-user Ser1\Administrator -pass password] "
  wscript.echo str
end function


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

'Cons for return val's
Const intOK = 0
Const intWarning = 1
Const intCritical = 2
Const intError = 3

On Error Resume Next
for i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments( i )
next

if (Err.Number>0) Then
	f_Error()
else 
  if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
    f_help()
  else 
  ' Check for Case 1, local machine
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-CLASS") And (UCase(arg(4))="-WHERE") And (UCase(arg(6))="-PROP") And (UCase(arg(8))="-W") And (UCase(arg(10))="-C") And (Wscript.Arguments.Count=12) then
		sComputer = arg(1)
		sClass = arg(3)
		sWhere = arg(5)
		sProperty = arg(7)
		result = CheckCounter(sComputer,sClass,sWhere,sProperty)
      if (Err.Number > 0) Then
	f_Error()
      else
        if (CLng(result) < CLng(arg(9))) then
          prefix = "OK - "
          Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
          WScript.Quit(intOK)
        else 
          if (CLng(result) < CLng(arg(11))) then
            prefix = "Warning - "
	    Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
            Wscript.Quit(intWarning)
          else 
            prefix = "Critical - "
	    Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
            Wscript.Quit(intCritical)
          end if
        end if        
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-CLASS") And (UCase(arg(4))="-WHERE") And (UCase(arg(6))="-PROP") And (UCase(arg(8))="-W") And (UCase(arg(10))="-C") And (UCase(arg(12))="-USER") And (UCase(arg(14))="-PASS") And (Wscript.Arguments.Count=16) then
        sComputer = arg(1)
	sClass = arg(3)
	sWhere = arg(5)
	sProperty = arg(7)     
        result =  CheckCounterEx(sComputer,sClass,sWhere,sProperty, arg(13) , arg(15))
        if (Err.Number>0) Then			
			f_Error()
        else
			if (CLng(result) < CLng(arg(9))) then
				prefix = "OK - "
				Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
				WScript.Quit(intOK)
			else 
				if (CLng(result) < CLng(arg(11))) then
					prefix = "Warning - "
					Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
					Wscript.Quit(intWarning)
				else 
					prefix = "Critical - "
					Wscript.Echo prefix & sProperty & " = "& result & " | '" & sProperty & "'=" & result & ";" & CLng(arg(9)) & ";" & CLng(arg(11)) & ";;"
					Wscript.Quit(intCritical)
				end if        
			end if
        end if
      else
        f_Error()
      end if
    end if    
  end if
end if