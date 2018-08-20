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
' Author ITSP Company (info@itspco.com)

' Get Memory Pages. Input: Computer Name
Dim strResult
Function CheckService(strComputer , Service)		
	On Error Resume Next
	Dim objWMIService, colOS , objOS , Sql
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
	If Service = "*" then
		Sql = "Select DisplayName,State From Win32_Service"
	Else
		Service = "'" & Service & "'"
		Sql = "Select DisplayName,State From Win32_Service Where DisplayName=" & Service
	End If
	Set colOS = objWMIService.ExecQuery(Sql)
	if colOS.count = 0 then
		StrResult = strResult & "Service " & Service & " does not exist" & ";;"
	End If
	for Each objOS In colOS		
		StrResult = strResult & objOS.DisplayName & " is " & objOS.State & ";;"
	next	
End Function

' Get Memory Pages. Input Computer Name + User Name + Password
Function CheckServiceEx( strComputer , Service , strUser , strPassword)	
	On Error Resume Next	
	
	Dim objWMIService, colOS , objOS , objSWbemLocator , Sql
	' Check for Local Computer.
	Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select Name From Win32_ComputerSystem")
	for Each objOS In colOS					
		if strComp ( objOS.Name , strComputer , 1) = 0 then			
			CheckService "." , Service
			exit Function
		end if
	next
	' Check for Remote Computer	
	If Service = "*" then
		Sql = "Select DisplayName,State From Win32_Service"
	Else
		Service = "'" & Service & "'"
		Sql = "Select DisplayName,State From Win32_Service Where DisplayName=" & Service		
	End If
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")    
	Set objWMIService = objSWbemLocator.ConnectServer _
	(strComputer, "root\cimv2" , strUser, strPassword )		
	Set colOS = objWMIService.ExecQuery(Sql)
	if colOS.count = 0 then
		StrResult = strResult & "Service " & Service & "is not exist" & ";;"
	End If
	for Each objOS In colOS		
		StrResult = strResult & objOS.DisplayName & " is " & objOS.State & ";;"		
	next	
End Function


'**********************
'Error Function
'**********************
Function f_Error()
  Wscript.echo "Error!"
  Wscript.Quit(intError)
End Function


'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Verify the existence of processes.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript check_service_state_displayname1.vbs -h hostname -s service_displaynames [-user username -pass password]"
str=str&vbCrlF
str=str&"-h hostname                   	Host name."&vbCrlF
str=str&"-s Service_Displaynames        Service Display names,multiple processes will be enclosed in multiple quotes and separated by commas."&vbCrlF
str=str&"-user username              	Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              	Password Account Administrator on Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript check_service_state_displayname1.vbs -h Ser1 -s ""Wireless Configuration, Automatic Updates"" [-user SER1\Administrator -pass password] "&vbCrlF
str=str&"Example: cscript check_service_state_displayname1.vbs -h Ser1 -s * [-user SER1\Administrator -pass password] "&vbCrlF
wscript.echo str
end function


Function Do_Check(StrServices)
	Dim pos		
	pos = 1
	do while (pos > 0)
		pos = Instr(2 , strServices , ",")						
		if (pos = 0) then
			service = strServices
		Else
			service = Mid(strServices , 1 , pos - 1)
			strServices = Mid(strServices , pos + 1 , len(strServices))				
		End If
		CheckService sComputer , service
		WScript.Echo StrResult
	Loop
End Function
Function Do_CheckEx(StrServices , strUser , strPass)
	Dim pos		
	pos = 1
	do while (pos > 0)
		pos = Instr(2 , strServices , ",")						
		if (pos = 0) then
			service = strServices
		Else
			service = Mid(strServices , 1 , pos - 1)
			strServices = Mid(strServices , pos + 1 , len(strServices))				
		End If
		CheckServiceEx sComputer , service , strUser , strPass
	Loop
End Function

'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer , strServices , service
Dim PagesMemory

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
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-S") And (Wscript.Arguments.Count=4) then
		sComputer = arg(1)
		strServices = arg(3)		
		Do_Check(strServices)
		'PagesMemory = GetPagesPerSec(sComputer)
      if (Err.Number > 0) Then
        f_Error()
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-S") And (UCase(arg(4))="-USER") And (UCase(arg(6))="-PASS") And (Wscript.Arguments.Count=8) then
        sComputer = arg(1)
        strServices = arg(3)
        Do_CheckEx strServices , arg(5) , arg(7)
        WScript.Echo StrResult
      else
        f_Error()
      end if
    end if    
  end if
end if