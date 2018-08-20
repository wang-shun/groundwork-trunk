' Copyright 2006 GroundWork Open Source Inc.
'
' This program is free software; you can redistribute it and/or
' modify it under the terms of the GNU General Public License
' as published by the Free Software Foundation; version 2
' of the License.
'
' This program is distributed in the hope that it will be useful,
' but WITHOUT ANY WARRANTY; without even the implied warranty of
' MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
' GNU General Public License for more details.
'
'
' Author Dr. Dave Blunt at GroundWork Open Source Inc. (dblunt@groundworkopensource.com)

'**********************
'Error Function
'**********************
Function f_Error(message)
  if(message <> "") then
    Wscript.echo message
	Wscript.Quit(intError)
  end if

  if (err.number<>0) then
  	if err.number=-2147217392 then
  		Wscript.echo "Error! | Error Number : -2147217392 | Description : Invalid Class"
		Wscript.Quit(intError)
	else
    	Wscript.echo "Error! | Error Number : " & err.number & " | Description : " & err.description
		Wscript.Quit(intError)
	end if
  end if
End Function

'**********************
'Help Function
'**********************
Function f_help()
  Dim str
  str="List single properties for all instances of a class via WMI. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript enumerate_wmi.vbs -h <hostname> -c <WMI Class> -p <WMI Property> [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h <hostname>              Target hostname or IP."&vbCrlF
  str=str&"-c <WMI Class>             WMI class."&vbCrlF
  str=str&"-p <WMI Property>          WMI property."&vbCrlF
  str=str&"-user <username>           Account Administrator on Target Machine."&vbCrlF
  str=str&"-pass <password>           Password Account Administrator on Target Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript enumerate_wmi.vbs -h Ser1 -c Win32_Service -p Name [-user Ser1\Administrator -pass password] "
  wscript.echo str
  wscript.quit(intError)
end function

'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer,sClass,sProperty
Dim result
Dim colOS,objOS
Dim objWMIService,objSWbemLocator

'Cons for return val's
Const intOK = 0
Const intError = 3

On Error Resume Next
for i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments( i )
next

if (Err.Number>0) Then
	f_Error("Error:  Couldn't read arguments.")
else 
  if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
    f_help()
  else 
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-C") And (UCase(arg(4))="-P") And (Wscript.Arguments.Count=6) then
		sComputer = arg(1)
		sClass = arg(3)
		sProperty = arg(5)
		sUser="NONE"
		sPassword="NONE"
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-C") And (UCase(arg(4))="-P") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (Wscript.Arguments.Count=10) then
	sComputer = arg(1)
	sClass = arg(3)
	sProperty = arg(5)
	sUser=arg(7)
	sPassword=arg(9)
      else
        f_Error("Error:  Couldn't read arguments.")
      end if
    end if


    if (sUser="NONE") then
	Set objWMIService = GetObject("winmgmts:\\" & sComputer)
    else
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
	Set objWMIService = objSWbemLocator.ConnectServer(sComputer, "root\cimv2", sUser, sPassword )
    end if

    Set colOS = objWMIService.ExecQuery("Select " & sProperty & " From " & sClass)
    if (colOS.count = 0) then
	f_Error("Error:  No row returned from WQL query.")
    end if

    result=""
    For Each objOS In colOS
      result = result & objOS.Properties_(sProperty) & ";"
    Next

    if (Err.Number>0) Then			
	f_Error("Error:  Couldn't understand WMI results.")
    else
	prefix = "OK - " & result
	Wscript.Echo prefix
	WScript.Quit(intOK)
    end if
  end if
end if
