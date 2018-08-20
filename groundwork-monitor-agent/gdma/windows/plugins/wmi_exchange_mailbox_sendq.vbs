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

' Get Send Mailbox Queue in Local Machine. Input: Computer Name
Function QueueMailboxSend(strComputer)	
	Dim objWMIService, colOS,objOS	
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select * from Win32_PerfRawData_MSExchangeIS_MSExchangeISMailbox")
	if (colOS.count = 0) then
		f_Error("Error! Don't have data performed!")
	end if
	for Each objOS In colOS		
		QueueMailboxSend = objOS.SendQueueSize
	next
End Function

' Get Send Mailbox Queue in Local Machine. Input Computer Name + User Name + Password
Function QueueMailboxSendEx( strComputer,strUser,strPassword, strDomain)
	Dim sql
	Dim objWMIService, colOS,objOS,objSWbemLocator		
	
	' Check for Local Computer.
	Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select * From Win32_ComputerSystem")
	for Each objOS In colOS					
		if strComp ( objOS.Name , strComputer , 1) = 0 then			
			QueueMailboxSendEx = QueueMailboxSend( ".")			
			exit Function
		end if
	next
	' Check for Remote Computer	
	
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")  
	if (strDomain = "") then  
		Set objWMIService = objSWbemLocator.ConnectServer _
		(strComputer, "\root\cimv2" , strUser, strPassword )	
	else 	
		Set objWMIService = objSWbemLocator.ConnectServer _
			(strComputer, "\root\cimv2" , strUser, strPassword,"MS_409","ntlmdomain:" & strDomain )	
	end if
	
	Set colOS = objWMIService.ExecQuery("Select * from Win32_PerfRawData_MSExchangeIS_MSExchangeISMailbox")
	if (colOS.count = 0) then
		f_Error("Error! Don't have data performed!")
	end if
	for Each objOS In colOS				
		QueueMailboxSendEx = objOS.SendQueueSize
	next
End Function


'**********************
'Error Function
'**********************
Function f_Error(message)
  if(message <> "") then
    Wscript.echo message
	Wscript.Quit(3)
  end if

  if (err.number<>0) then
  	if err.number=-2147217392 then
  		Wscript.echo "Error! | Error Number : -2147217392 | Description : Invalid Class"
		Wscript.Quit(3)
	else
    	Wscript.echo "Error! | Error Number : " & err.number & " | Description : " & err.description
		Wscript.Quit(3)
	end if
  end if
End Function

Function f_help()
  Dim str
  str="Check Messages in MailBox Send Queue of MS Exchange. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript exchange_msg_queue_mailbox_sendq.vbs -h hostname -w warning_level -c critical_level [-user username -pass password [-domain domain]]"
  str=str&vbCrlF
  str=str&"-h hostname                 Host name."&vbCrlF  
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level           Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&"-domain domain              Net Bios Domain Name of Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example:cscript exchange_msg_queue_mailbox_sendq.vbs -h Ser1 -w 30 -c 70 [-user Ser1\Administrator -pass password -domain ITSP] "
  wscript.echo str
end function


'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer
Dim Queue

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
	f_Error("")
else 
  if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
    f_help()
  else 
  ' Check for Case 1, local machine
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (Wscript.Arguments.Count=6) then
		sComputer = arg(1)		
		Queue = QueueMailboxSend(sComputer)
      if (Err.Number <> 0) Then		
        f_Error("")
      else 
        if (Queue < CLng(arg(3))) then
          prefix = "OK - "
          Wscript.Echo prefix & " Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
          WScript.Quit(intOK)
        else 
          if (Queue < CLng(arg(5))) then
            prefix = "Warning - "
            Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
            Wscript.Quit(intWarning)
          else 
            prefix = "Critical - "
            Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
            Wscript.Quit(intCritical)
          end if
        end if        
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (Wscript.Arguments.Count=10) then
        sComputer = arg(1)
        Queue =  QueueMailboxSendEx( sComputer , arg(7) , arg(9),"")
        if (Err.Number<>0) Then			
			f_Error("")
        else
			if (Queue <CLng(arg(3))) then
				prefix = "OK - "
				Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
				WScript.Quit(intOK)
			else 
				if (Queue < CLng(arg(5))) then
					prefix = "Warning - "
					Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
					Wscript.Quit(intWarning)
				else 
					prefix = "Critical - "
					Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
					Wscript.Quit(intCritical)
				end if        
			end if
        end if
      else
        if (UCase(arg(0))="-H") And (UCase(arg(2))="-W") And (UCase(arg(4))="-C") And (UCase(arg(6))="-USER") And (UCase(arg(8))="-PASS") And (UCase(arg(10))="-DOMAIN") And (Wscript.Arguments.Count=12) then
        	sComputer = arg(1)
        	Queue =  QueueMailboxSendEx( sComputer , arg(7) , arg(9), arg(11))
        	if (Err.Number<>0) Then			
				f_Error("")
        	else
				if (Queue <CLng(arg(3))) then
					prefix = "OK - "
					Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
					WScript.Quit(intOK)
				else 
					if (Queue < CLng(arg(5))) then
						prefix = "Warning - "
						Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
						Wscript.Quit(intWarning)
					else 
						prefix = "Critical - "
						Wscript.Echo prefix &" Send Queue Size is "& Queue & " | 'QueueSize'=" & Queue & ";" & CLng(arg(3)) & ";" & CLng(arg(5)) & ";;"
						Wscript.Quit(intCritical)
					end if        
				end if
        	end if
      	else
        	f_Error("Error! | Wrong Arguments!")
      	end if
      end if        
    end if    
  end if
end if