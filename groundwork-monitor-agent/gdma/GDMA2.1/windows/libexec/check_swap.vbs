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
' Author Pham Van Hung of ITSP Company (info@itspco.com)

' Get Memory Pages. Input: Computer Name
Function GetPagesPerSec(strComputer)		
	On Error Resume Next
	Dim objWMIService, colOS,objOS, TimeOld, TimeNew, i
	i = 0
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")		
	Set colOS = objWMIService.ExecQuery("Select PagesPersec From Win32_PerfRawData_PerfOS_Memory")	
	for Each objOS In colOS				
		TimeOld = objOS.PagesPersec				
	next
	WScript.Sleep 1000
	Set colOS = objWMIService.ExecQuery("Select PagesPersec From Win32_PerfRawData_PerfOS_Memory")
	for Each objOS In colOS									
		TimeNew = objOS.PagesPersec		
	next
	GetPagesPerSec = TimeNew - TimeOld		
	exit function
End Function

' Get Memory Pages. Input Computer Name + User Name + Password
Function GetPagesPerSecEx( strComputer,strUser,strPassword)
	Dim sql
	On Error Resume Next	
	Dim objWMIService, colOS,objOS,objSWbemLocator, TimeOld, TimeNew
	
	' Check for Local Computer.
	Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select Name From Win32_ComputerSystem")
	for Each objOS In colOS					
		if strComp ( objOS.Name , strComputer , 1) = 0 then			
			GetPagesPerSecEx = GetPagesPerSec(".")
			exit Function
		end if
	next
	' Check for Remote Computer	
	
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")    
	Set objWMIService = objSWbemLocator.ConnectServer _
	(strComputer, "root\cimv2" , strUser, strPassword )	
	Set colOS = objWMIService.ExecQuery("Select PagesPersec From Win32_PerfRawData_PerfOS_Memory")
	for Each objOS In colOS				
		TimeOld = objOS.PagesPersec		
	next
	WScript.Sleep 1000
	Set colOS = objWMIService.ExecQuery("Select PagesPersec From Win32_PerfRawData_PerfOS_Memory")
	for Each objOS In colOS									
		TimeNew = objOS.PagesPersec						
	next
	GetPagesPerSecEx = TimeNew - TimeOld		
	exit function
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
  str="Check Memory Pages per second. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript check_swap.vbs -h hostname -w warning_level -c critical_level [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h hostname                   Host name."&vbCrlF  
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level                Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript check_swap.vbs -h Ser1 -w 30 -c 70 [-user Ser1\Administrator -pass password] "
  wscript.echo str
end function


'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer
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
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (Wscript.Arguments.Count=6) then
		sComputer = arg(1)		
		PagesMemory = GetPagesPerSec(sComputer)
      if (Err.Number > 0) Then		
        f_Error()
      else 
        if (PagesMemory <= CLng(arg(3))) then
          prefix = "OK - "
          Wscript.Echo prefix & " Swap Pages/sec = "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
        else 
          if (PagesMemory <= CLng(arg(5))) then
            prefix = "Warning - "
            Wscript.Echo prefix &" Swap Pages/sec = "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
            Wscript.Quit(intWarning)
          else 
            prefix = "Critical - "
            Wscript.Echo prefix &" Swap Pages/sec = "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
            Wscript.Quit(intCritical)
          end if
        end if        
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (Wscript.Arguments.Count=10) then
        sComputer = arg(1)
        PagesMemory =  GetPagesPerSecEx( sComputer , arg(7) , arg(9))
        if (Err.Number>0) Then			
			f_Error()
        else
			if (PagesMemory <= CLng(arg(3))) then
				prefix = "OK - "
				Wscript.Echo prefix &" Swap Pages/sec is "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
				WScript.Quit(intOK)
			else 
				if (PagesMemory <= CLng(arg(5))) then
					prefix = "Warning - "
					Wscript.Echo prefix &" Swap Pages/sec = "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
					Wscript.Quit(intWarning)
				else 
					prefix = "Critical - "
					Wscript.Echo prefix &" Swap Pages/sec = "& PagesMemory & " | 'Paging'=" & PagesMemory & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
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