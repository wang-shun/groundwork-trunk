' Copyright 2004-2005 GroundWork OpenSource Solutions
''
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

Function MailBoxSize( strComputer,strName,argw,argc)

    Dim argresult
    Dim result
    Dim refix
	Set objWMIService = GetObject("winmgmts:" _
    & "{impersonationLevel=impersonate}!\\" & strComputer & _
        "\ROOT\MicrosoftExchangeV2")
	argresult=0
	result=""
	Set colItems = objWMIService.ExecQuery ("Select * from Exchange_Mailbox")
	if (colItems.count = 0) then
		f_Error("Error! Don't have data performed!")
	end if
    if (strName="*") then
    	For Each objItem in colItems
    		if (Int(objItem.Size)<Int(argw)) then    			
    			refix="OK - "
    		else
    			if (Int(objItem.Size)<Int(argc)) then
    				if (argresult<1) then
    					argresult=1
    				end if
    				refix="WARNING - "
    			else
    				if (argresult<2) then
    					argresult=2
    				end if
    				refix="CRITICAL - "
    			end if
    		end if
    		result=result & refix & "MailBox Store Name : " & objItem.MailboxDisplayName & " = " & objItem.Size & " Kb | " & "'MailBox Store Size'=" & objItem.Size & ";" & argw & ";" & argc & ";;     "
    	Next
    	wscript.echo result
    	MailBoxSize=argresult
		Exit Function
	else
		For Each objItem In colItems
	 		if (UCase( objItem.MailboxDisplayName) = UCase( strName )) then
	 			if (Int(objItem.Size)<Int(argw)) then
    				refix="OK - "
    			else
    				if (Int(objItem.Size)<Int(argc)) then
    					if (argresult<1) then
    						argresult=1
    					end if
    					refix="WARNING - "
    				else
    					if (argresult<2) then
    						argresult=2
    					end if
    					refix="CRITICAL - "
    				end if
    			end if
    			result=result & refix & "MailBox Store Name : " & objItem.MailboxDisplayName & " = " & objItem.Size & " Kb | " & "'MailBox Store Size'=" & objItem.Size & ";" & argw & ";" & argc & ";;     "
    			wscript.echo result
    			MailBoxSize=argresult
				Exit Function
			end if
    	Next
    end if
    Err.number=1
End Function
'**********************
'Extend Function
'**********************
Function MailBoxSizeEx( strComputer,strName,argw,argc,strUser,strPassword,strDomain)

    Dim objWMIService, colOS,objOS
    Dim argresult
    Dim result
    Dim refix
	argresult=0
    Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")
    if (strDomain="") then    	
    	Set objWMIService = objSWbemLocator.ConnectServer _
    	(strComputer, "\root\MicrosoftExchangeV2",strUser, strPassword)
    else
    	Set objWMIService = objSWbemLocator.ConnectServer _
			(strComputer, "\root\MicrosoftExchangeV2" , strUser, strPassword,"MS_409","ntlmdomain:" & strDomain )	
    end if
    objWMIService.Security_.ImpersonationLevel = 3
    Set colOS = objWMIService.InstancesOf("Exchange_Mailbox")
    if (colOS.count = 0) then
		f_Error("Error! Don't have data performed!")
	end if
    if (strName="*") then
    	For Each objItem in colOS
    		if (Int(objItem.Size)<Int(argw)) then
    			refix="OK - "
    		else
    			if (Int(objItem.Size)<Int(argc)) then
    				if (argresult<1) then
    					argresult=1
    				end if
    				refix="WARNING - "
    			else
    				if (argresult<2) then
    					argresult=2
    				end if
    				refix="CRITICAL - "
    			end if
    		end if
    		result=result & refix & "MailBox Store Name : " & objItem.MailboxDisplayName & " = " & objItem.Size & " Kb | " & "'MailBox Store Size'=" & objItem.Size & ";" & argw & ";" & argc & ";;     "
    	Next
    	wscript.echo result
    	MailBoxSizeEx=argresult
		Exit Function
	else    
		For Each objItem In colOS
	 		if (UCase( objItem.MailboxDisplayName) = UCase( strName )) then
	 			if (Int(objItem.Size)<Int(argw)) then
    				refix="OK - "
    			else
    				if (Int(objItem.Size)<Int(argc)) then
    					if (argresult<1) then
    						argresult=1
    					end if
    					refix="WARMING - "
    				else
    					if (argresult<2) then
    						argresult=2
    					end if
    					refix="CRITICAL - "
    				end if
    			end if
    			result=result & refix & "MailBox Store Name : " & objItem.MailboxDisplayName & " = " & objItem.Size & " Kb | " & "'MailBox Store Size'=" & objItem.Size & ";" & argw & ";" & argc & ";;     "
    			wscript.echo result
    			MailBoxSizeEx=argresult
				Exit Function
			end if
    	Next
    end if
    Err.number=1
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

'**********************
'Help Function
'**********************
Function f_help()
Dim str
str="Check MailBox Size.If your Local Machine have the same Administrator account and password as Remote Machine,you don't have to use two last parameters."&vbCrlF&vbCrlF
str=str&"cscript exchange_mailbox_size.vbs -h hostname -name mailboxname -w warning_level -c critical_level[-user username -pass password -domain domain]"
str=str&vbCrlF
str=str&"-h hostname                 Host name."&vbCrlF
str=str&"-name mailboxname           MailBox name."&vbCrlF
str=str&"-w warning_level            Warning threshold."&vbCrlF
str=str&"-c critical_level           Critical threshold."&vbCrlF
str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
str=str&"-domain domaim              Net Bios Domain Name of Remote Machine."&vbCrlF
str=str&vbCrlF
str=str&"Example: cscript exchange_mailbox_size.vbs -h Ser1 -name ""MailServer"" -w 30 -c 70[-user SER1\Administrator -pass password -domain ITSP] "&vbCrlF
str=str&"or: cscript exchange_mailbox_size.vbs -h Ser1 -name ""*"" -w 30 -c 70[-user SER1\Administrator -pass password -domain ITSP] "
wscript.echo str
end function


'******************************
'** Exit Function
'******************************
Function f_exit(arg)
  Dim sComputer,sName
  Dim result1
  On Error Resume Next
  If (Err.Number<>0) Then
    f_Error("")
  Else
    if ((UCase(arg(0))="-H") Or (UCase(arg(0))="--HELP")) and (Wscript.Arguments.Count=1) then
      f_help()
    else
      If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-NAME") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8) then
      	sComputer = arg(1)
        result1=MailBoxSize(sComputer,arg(3),arg(5),arg(7))
        If (Err.Number<>0) Then
        	f_Error("")
         end if
      else
        If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-NAME") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10)) = "-PASS") And (Wscript.Arguments.Count=12) then
          sComputer = arg(1)
          result1=MailBoxSizeEx(sComputer,arg(3),arg(5),arg(7),arg(9),arg(11),"")
          If (Err.Number<>0) Then
            f_Error("")
          end if
        else
          If (UCase(arg(0))="-H") And (UCase(arg(2)) = "-NAME") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10)) = "-PASS") And (UCase(arg(12)) = "-DOMAIN") And (Wscript.Arguments.Count=14) then
          	sComputer = arg(1)
          	result1=MailBoxSizeEx(sComputer,arg(3),arg(5),arg(7),arg(9),arg(11),arg(13))
          	If (Err.Number<>0) Then
            	f_Error("")
          	end if
          else          
          	f_Error("Error! | Wrong Arguments!")
          end if
        end if
      end if
    end if
  end if
  f_exit=result1
end function
'******************************
'** Main Function
'******************************

Dim i
Dim arg(20)
Dim res
On Error Resume Next

For i=0 to WScript.Arguments.Count-1
  arg(i)=WScript.Arguments(i)
Next

res=f_exit(arg)
wscript.quit(res)