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
Dim str1
Function LogUsed(strComputer , strDatabase)		
	Dim objWMIService , colOS , objOS , sql , key , temp
	temp = intOK
	If strDatabase = "*" then
		Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases"
	Else
		strDatabase = "'" & strDatabase & "'"
		Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases where Name=" & strDatabase		
	End If
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery(sql)
	if (colOS.count = 0) then
		temp=intError
		str1=str1 & "Database " &strDatabase& " is not found;;     "
	else
		for Each objOS In colOS
			result = objOS.PercentLogUsed		
			if (result < CDbl(arg(5))) then
	          prefix = " OK - " & "'" & objOS.Name
	          str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	          key = intOK 
	        else 
	          if (result < CDbl(arg(7))) then
	            prefix = " Warning - " & "'" & objOS.Name
	            str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          	str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	            key = intWarning
	          else 
	            prefix = " Critical - " & "'" & objOS.Name
	            str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          	str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	            key = intCritical
	          end if
	        end if
	        if key > temp then
	        	temp = key
	        End If        
		next
	end if
	
    LogUsed = temp    
End Function

' Get Memory Pages. Input Computer Name + User Name + Password
Function LogUsedEx( strComputer , strDatabase , strUser , strPassword, strDomain)
	Dim objWMIService, colOS,objOS,objSWbemLocator , sql , key , temp
	temp = 0
	
	' Check for Local Computer.	
	Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select * From Win32_ComputerSystem")
	for Each objOS In colOS					
		if strComp ( objOS.Name , strComputer , 1) = 0 then
			LogUsedEx = LogUsed("." , strDatabase)
			exit Function
		end if
	next
	' Check for Remote Computer	
	If strDatabase = "*" then
		Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases"
	Else
		strDatabase = "'" & strDatabase & "'"
		Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases where Name=" & strDatabase		
	End If					
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")	
	if (strDomain = "") then  
		Set objWMIService = objSWbemLocator.ConnectServer _
		(strComputer, "root\cimv2" , strUser, strPassword )	
	else 	
		Set objWMIService = objSWbemLocator.ConnectServer _
			(strComputer, "\root\cimv2" , strUser, strPassword,"MS_409","ntlmdomain:" & strDomain )	
	end if
	Set colOS = objWMIService.ExecQuery(sql)
	if (colOS.count = 0) then
		temp=intError
		str1=str1 & "Database " &strDatabase& " is not found;;     "
	else
		for Each objOS In colOS
			result = objOS.PercentLogUsed
			if (result < CDbl(arg(5))) then
	          prefix = " OK - " & "'" & objOS.Name
	          str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          	str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	          key = intOK
	          'WScript.Quit(intOK)
	        else 
	          if (result < CDbl(arg(7))) then
	            prefix = " Warning - " & "'" & objOS.Name 
	            str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          	str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	            'Wscript.Quit(intWarning)
	            key = intWarning
	          else 
	            prefix = " Critical - " & "'" & objOS.Name
	            str1 = str1 & prefix & "' Percent Log Used : "& result & "% | "
	          	str1 = str1 & " 'Percent'="&result&";" & arg(5) & ";" & arg(7) & ";;	"
	            key = intCritical
	          end if
	        end if
	        if key > temp then
	        	temp = key
	        End If        
		next
	end if
	
    LogUsedEx = temp
End Function

Function Do_Check(sComputer , StrDatabases)
	Dim pos	, tmp
	pos = 1
	tmp = 0
	do while (pos > 0)
		pos = Instr(2 , strDatabases , ",")
		if (pos = 0) then
			database = strDatabases
		Else
			database = Mid(strDatabases , 1 , pos - 1)
			strDatabases = Mid(strDatabases , pos + 1 , len(strDatabases))
		End If
		result = LogUsed(sComputer , database)
		if result > tmp then
			tmp = result
		End If	
	Loop
	WScript.Echo str1
	WScript.Quit(tmp)
End Function
Function Do_CheckEx(sComputer , StrDatabases , strUser , strPass, strDomain)
	Dim pos	, tmp
	pos = 1
	tmp = 0
	do while (pos > 0)
		pos = Instr(2 , strDatabases , ",")
		if (pos = 0) then
			database = strDatabases
		Else
			database = Mid(strDatabases , 1 , pos - 1)
			strDatabases = Mid(strDatabases , pos + 1 , len(strDatabases))				
		End If
		result = LogUsedEx(sComputer , database , strUser , strPass, strDomain)		
		if result > tmp then
			tmp = result
		End If		
	Loop
	WScript.Echo str1
	WScript.Quit(tmp)
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
  str="Check Percent Log Used of MS SQL. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript mssql_log_used.vbs -h hostname -d database -w warning_level -c critical_level [-user username -pass password [-domain domain]]"
  str=str&vbCrlF
  str=str&"-h 			               Help."&vbCrlF
  str=str&"-h hostname                 Host name."&vbCrlF  
  str=str&"-d database	               Database Name."&vbCrlF
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level           Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&"-domain domain              Net Bios Domain Name of Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript mssql_log_used.vbs -h Ser1 -d * -w 30 -c 70 [-user Ser1\Administrator -pass password -domain ITSP]"
  wscript.echo str
end function
'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer,sDatabase

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
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8) then
		sComputer = arg(1)
		sDatabase = arg(3)		
	
		Do_Check sComputer , sDatabase
      if (Err.Number <> 0) Then	
        f_Error("")
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (Wscript.Arguments.Count=12) then
        sComputer = arg(1)        
        sDatabase = arg(3)
        Do_CheckEx sComputer , sDatabase , arg(9) , arg(11),""
        if (Err.Number<>0) Then			
			f_Error("")
        else			
        end if
      else
        if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (UCase(arg(12))="-DOMAIN") And (Wscript.Arguments.Count=14) then
        	sComputer = arg(1)        
	        sDatabase = arg(3)
    	    Do_CheckEx sComputer , sDatabase , arg(9) , arg(11) , arg(13)
        	if (Err.Number<>0) Then			
				f_Error("")
        	else			
        	end if
      	else
        	f_Error("Error! | Wrong Arguments!")
      	end if
      end if
    end if    
  end if
end if