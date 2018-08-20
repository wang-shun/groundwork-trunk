$ping = `ping $ARGV[0]`;
$ping =~  /\((\d+)% loss\)/;
print "loss:$1 ";
$ping =~ /Minimum = (\d+)ms, Maximum =  (\d+)ms, Average =  (\d+)ms/;
print "latency:$3 ";