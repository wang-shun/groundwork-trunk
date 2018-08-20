# ----------------------------------------------------------------------
# service_profile    = {name}|{description}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  service_name      = {name}|{template}|{extinfo}|{description}
#   service_external = {type}
#    service_data    = {text}
#    ...                      
#    service_data    = {text}
#  command           = {name}|{type}|{commandline}
#  time_period       = {name}|{alias}               ... internal subroutine expands
#  service_template  = {name}
# ----------------------------------------------------------------------

service_profile    = exch_std|Exchange Standard Checks for All Roles
 service_name      = ad_ldap|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\{{profile}}.ps1 -warning 50 -critical 100  -waittime 2 ; exit $LASTEXITCODE "
   service_data    = Check_Interval="1"

 service_name      = mem_avail|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\{{profile}}.ps1 -warning 200 -critical 100 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = mem_page_leak|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\mem_paging.ps1 -warning 5 -critical 5 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = mem_page_rate|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\mem_pages_sec.ps1 -warning 750 -critical 1000 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = mem_page_used|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\mem_page_file.ps1 -warning 80 -critical 90 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = mount_point|gdma|percent_graph|desc-gdma_wmi_cpu

 service_name      = net_bw|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\net_bw.ps1 -warning 7,1 -critical 10,10 -instance -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = net_web_exc|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\net_web_execptions.ps1 -warning 5 -critical 10 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = proc_que|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\process_queue.ps1 -warning 5 -critical 10 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 service_name      = proc_time|gdma|percent_graph|desc-gdma_wmi_cpu
  service_external = service
   service_data    = Enable="ON"
   service_data    = Service="{{profile}}_{{service_name}}"
   service_data    = Command="cmd /c powershell.exe -noprofile -command $Plugin_Directory$\v3\processor_time.ps1 -warning 50 -critical 75 -waittime 2 ; exit $LASTEXITCODE"
   service_data    = Check_Interval="1"

 time_period       = 24x7|Tired and Swamped Old Computer
 service_template  = gdma
 command           = check_gdma_fresh|check|check_dummy 3 $ARG1$
 extended_service_info_template = percent_graph|||services.gif|/graphs/cgi-bin/label_graph.cgi?host=$HOSTNAME$&service=$SERVICENAME$|Service Detail

