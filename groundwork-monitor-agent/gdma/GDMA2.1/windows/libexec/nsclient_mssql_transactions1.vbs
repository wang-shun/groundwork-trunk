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
Dim str1 , str2 
Function Transactions(strComputer , strDatabases)	
	On Error Resume Next
	Dim objWMIService , colOS , colOS1 , objOS , objOS1 , Sql , key , temp , pos , tmp , i , j , sum
	Dim NewArrDatabase(100) , NewArrDatabaseName(100) , OldArrDatabase(100) , OldArrDatabaseName(100)
	Dim database , return
	Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases"
	i = 0
	pos = 1 ' Init Value for checking input Databases
	temp = 0 ' Init Return Temp Value
	' Get all values
	Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")	
	Set colOS = objWMIService.ExecQuery(sql)
	if colOS.count = 0 then
		WScript.Echo "Database " & strDatabase & "Not Found"
		WScript.Quit(intError)
	Else
		sum = colOS.count				
		for Each objOS In colOS	
			OldArrDatabase(i) = objOS.TransactionsPerSec
	        OldArrDatabaseName(i) = objOS.Name
	        i = i +1
		next
		i = 0
		WScript.Sleep(1000)
		Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")	
		Set colOS = objWMIService.ExecQuery(sql)		
		for Each objOS In colOS	
			NewArrDatabase(i) = objOS.TransactionsPerSec
	        NewArrDatabaseName(i) = objOS.Name
	        i = i + 1
		next
	End If
	' Check for input database and build up Results
	If Instr(1 , strDatabases , "*") > 0 then ' Check for all Databases		
		for i = 0 to sum -1
			for j = 0 to sum - 1
				if(Ucase(OldArrDatabaseName(i)) = Ucase(NewArrDatabaseName(j)) And (len(NewArrDatabaseName(j)) > 0)) then ' Same Name
					result = NewArrDatabase(j) - OldArrDatabase(i)					
					if (result < CDbl(arg(5))) then
			          prefix = " OK - " & "'" & NewArrDatabaseName(j)
			          str1 = str1 & prefix & " Transactions' : "& result & ";;"
			          str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"
			          temp = intOK			          
			        else 
			          if (result < CDbl(arg(7))) then
			            prefix = " Warning - " & "'" & NewArrDatabaseName(j)
			            str1 = str1 & prefix & " Transactions' : "& result & ";;"
			            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"			            
			            temp = intWarning
			          else 
			            prefix = " Critical - " & "'" & NewArrDatabaseName(j)
			            str1 = str1 & prefix & " Transactions' : "& result & ";;"
			            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"            
			            temp = intCritical
			          end if
			        end if					
				End If
			Next			
		Next
	Else ' Check for input Databases "AAA,BBB,CCC"
		do while (pos > 0)
			pos = Instr(2 , strDatabases , ",")
			if (pos = 0) then
				database = strDatabases
			Else
				database = trim(Mid(strDatabases , 1 , pos - 1))
				strDatabases = trim(Mid(strDatabases , pos + 1 , len(strDatabases)))
			End If			
			key = 0
			for i = 0 to sum - 1				
				for j = 0 to sum - 1				
					if((Ucase(OldArrDatabaseName(i)) = Ucase(NewArrDatabaseName(j))) and Ucase(database) = Ucase(NewArrDatabaseName(j)) And (len(NewArrDatabaseName(j)) > 0)) then
						key = 1
						result = NewArrDatabase(j) - OldArrDatabase(i)						
						if (result < CDbl(arg(5))) then
				          prefix = " OK - " & "'" & NewArrDatabaseName(j)
				          str1 = str1 & prefix & " Transactions' : "& result & ";;"
				          str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"
				          temp = intOK				          
				        else 
				          if (result < CDbl(arg(7))) then
				            prefix = " Warning - " & "'" & NewArrDatabaseName(j)
				            str1 = str1 & prefix & " Transactions' : "& result & ";;"
				            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"				            
				            temp = intWarning
				          else 
				            prefix = " Critical - " & "'" & NewArrDatabaseName(j)
				            str1 = str1 & prefix & " Transactions' : "& result & ";;"
				            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"            
				            temp = intCritical
				          end if
				        end if					
					End If
				Next			
			Next			
			' Check for not found Database
			If key = 0 then
				str1 = str1 & "Database " & database & " is not found" & ";;"
				returnError = intError
			End If
		Loop		
	End If
	' Check for max return value
	If (temp > return) then 
		return = temp
	End If
	' Display final information
	WScript.Echo str1 & "|" & str2
	' Return Value
	If returnError = 0 then
		WScript.Quit(return)
	Else
		WScript.Quit(returnError)
	End If
End Function

' Get Memory Pages. Input Computer Name + User Name + Password
Function TransactionsEx( strComputer , strDatabases , strUser , strPassword)
	On Error Resume Next
	Dim objWMIService , colOS , colOS1 , objOS , objOS1 , Sql , key , temp , pos , tmp , i , j , sum
	Dim NewArrDatabase(100) , NewArrDatabaseName(100) , OldArrDatabase(100) , OldArrDatabaseName(100)
	Dim database , return
	Sql = "Select * From Win32_PerfRawData_MSSQLSERVER_SQLServerDatabases"
	i = 0
	pos = 1 ' Init Value for checking input Databases
	temp = 0 ' Init Return Temp Value
	return = 0	
	' Check for Local Computer.	
	Set objWMIService = GetObject("winmgmts:\\" & "." & "\root\cimv2")
	Set colOS = objWMIService.ExecQuery("Select * From Win32_ComputerSystem")
	for Each objOS In colOS					
		if strComp ( objOS.Name , strComputer , 1) = 0 then
			Transactions "." , strDatabases
			exit Function
		end if
	next
	' Check for Remote Computer	
	' Get all values
	Set objSWbemLocator = CreateObject("WbemScripting.SWbemLocator")	
	Set objWMIService = objSWbemLocator.ConnectServer _
	(strComputer, "root\cimv2" , strUser, strPassword )	
	Set colOS = objWMIService.ExecQuery(sql)
	if colOS.count = 0 then
		WScript.Echo "Database " & strDatabase & "Not Found"
		WScript.Quit(intError)
	Else
		sum = colOS.count				
		for Each objOS In colOS	
			OldArrDatabase(i) = objOS.TransactionsPerSec
	        OldArrDatabaseName(i) = objOS.Name
	        i = i +1
		next
		i = 0
		WScript.Sleep(1000)
		Set objWMIService = GetObject("winmgmts:\\" & strComputer & "\root\cimv2")	
		Set colOS = objWMIService.ExecQuery(sql)		
		for Each objOS In colOS	
			NewArrDatabase(i) = objOS.TransactionsPerSec
	        NewArrDatabaseName(i) = objOS.Name
	        i = i + 1
		next
	End If	
		
	' Check for input database and build up Results	
	If Instr(1 , strDatabases , "*") > 0 then ' Check for all Databases			
		for i = 0 to sum - 1
			for j = 0 to sum - 1
				if(Ucase(OldArrDatabaseName(i)) = Ucase(NewArrDatabaseName(j)) And (len(NewArrDatabaseName(j)) > 0)) then ' Same Name
					result = NewArrDatabase(j) - OldArrDatabase(i)					
					if (result < CDbl(arg(5))) then
			          prefix = " OK - " & "'" & NewArrDatabaseName(j)
			          str1 = str1 & prefix & " Transactions' : "& result & ";;"
			          str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"
			          temp = intOK			          
			        else 
			          if (result < CDbl(arg(7))) then
			            prefix = " Warning - " & "'" & NewArrDatabaseName(j)
			            str1 = str1 & prefix & " Transactions' : "& result & ";;"
			            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"			            
			            temp = intWarning
			          else 
			            prefix = " Critical - " & "'" & NewArrDatabaseName(j)
			            str1 = str1 & prefix & " Transactions' : "& result & ";;"
			            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"            
			            temp = intCritical
			          end if
			        end if					
				End If
			Next			
		Next
	Else ' Check for input Databases "AAA,BBB,CCC"
		do while (pos > 0)
			pos = Instr(2 , strDatabases , ",")
			if (pos = 0) then
				database = strDatabases
			Else
				database = trim(Mid(strDatabases , 1 , pos - 1))
				strDatabases = trim(Mid(strDatabases , pos + 1 , len(strDatabases)))
			End If			
			key = 0
			for i = 0 to sum - 1			
				for j = 0 to sum - 1 
					if((Ucase(OldArrDatabaseName(i)) = Ucase(NewArrDatabaseName(j))) and Ucase(database) = Ucase(NewArrDatabaseName(j)) And (len(NewArrDatabaseName(j)) > 0)) then
						key = 1
						result = NewArrDatabase(j) - OldArrDatabase(i)						
						if (result < CDbl(arg(5))) then
				          prefix = " OK - " & "'" & NewArrDatabaseName(j)
				          str1 = str1 & prefix & " Transactions' : "& result & ";;"
				          str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"
				          temp = intOK				          
				        else 
				          if (result < CDbl(arg(7))) then
				            prefix = " Warning - " & "'" & NewArrDatabaseName(j)
				            str1 = str1 & prefix & " Transactions' : "& result & ";;"
				            str2 = str2 & " ' " &  NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"				            
				            temp = intWarning
				          else 
				            prefix = " Critical - " & "'" & NewArrDatabaseName(j)
				            str1 = str1 & prefix & " Transactions' : "& result & ";;"
				            str2 = str2 & " ' " & NewArrDatabaseName(j) & " Transactions' = "&result&";" & arg(5) & ";" & arg(7) & ";;"            
				            temp = intCritical
				          end if
				        end if					
					End If
				Next			
			Next			
			' Check for not found Database
			If key = 0 then
				str1= str1 & "Database " & database & " is not found" & ";;"
				returnError = intError
			End If
			' Check for max return value
		Loop		
	End If
	If (temp > return) then 
		return = temp
	End If
	' Display final information
	WScript.Echo str1 & "|" & str2
	' Return Value
	If returnError = 0 then
		WScript.Quit(return)
	Else
		WScript.Quit(returnError)
	End If	
End Function

Function Do_Check
	
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
  str="Check Transactions of MS SQL. If your Local Machine has the same Administrator account and password as the Remote Machine then you don't have to use the two last parameters."&vbCrlF&vbCrlF
  str=str&"cscript nsclient_mssql_transactions1.vbs -h hostname -d <Database> -w warning_level -c critical_level [-user username -pass password]"
  str=str&vbCrlF
  str=str&"-h 			               Help."&vbCrlF
  str=str&"-d 			               Database Name."&vbCrlF
  str=str&"-h hostname                 Host name."&vbCrlF  
  str=str&"-w warning_level            Warning threshold."&vbCrlF
  str=str&"-c critical_level           Critical threshold."&vbCrlF
  str=str&"-user username              Account Administrator on Remote Machine."&vbCrlF
  str=str&"-pass password              Password Account Administrator on Remote Machine."&vbCrlF
  str=str&vbCrlF
  str=str&"Example: cscript nsclient_mssql_transactions1.vbs -h Ser1 -d * -w 30 -c 70 [-user Ser1\Administrator -pass password]"
  wscript.echo str
end function
'******************************
'** Main program
'******************************
Dim arg(20)
Dim i
Dim prefix
Dim sComputer,sDatabase , returnError
returnError = 0 ' Initialize for Error Case
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
    if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (Wscript.Arguments.Count=8) then
		sComputer = arg(1)
		sDatabase = arg(3)		
		Transactions sComputer , sDatabase
      if (Err.Number > 0) Then	
        f_Error()      
      end if
    else
      if (UCase(arg(0))="-H") And (UCase(arg(2))="-D") And (UCase(arg(4))="-W") And (UCase(arg(6))="-C") And (UCase(arg(8))="-USER") And (UCase(arg(10))="-PASS") And (Wscript.Arguments.Count=12) then
        sComputer = arg(1)        
        sDatabase = arg(3)
        TransactionsEx sComputer , sDatabase , arg(9) , arg(11)	
        if (Err.Number>0) Then			
			f_Error()
        else			
        end if
      else
        f_Error()
      end if
    end if    
  end if
end if