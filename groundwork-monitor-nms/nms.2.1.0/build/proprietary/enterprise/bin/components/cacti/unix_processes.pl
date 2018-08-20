#!/usr/local/groundwork/nms/tools/perl/bin/perl

open(PROCESS, "ps ax | grep -c : |");
$output = <PROCESS>;
close(PROCESS);
chomp($output);
print $output;
