#!/usr/local/groundwork/perl/bin/perl -w

# 2017-06 DN - v1.0.0 - initial version to work within Groundwork framework for QA usage


use strict;
use warnings;
use Getopt::Long;
use Data::Dumper; $Data::Dumper::Indent = 2; $Data::Dumper::Sortkeys = 1;
use Sys::Hostname;
use MIME::Base64;
use JSON;

# read test :
# Expects this to have been run first to load up with data :  ./tsperf.pl -host 300 -ser 20 -metr 2 -interval 2880 -gap 300 -batchsize 25000
# ie about 10 days of data on 10 min intervals for 300 hosts, 20 service per and 2 metrics per service

my $pid = $$;
my $numHosts = 2;
my $numServices = 2;
my $mps = 2; # metrics per service, eg load1, load5. 
my $intervals = 3;
my $help;
my $yes;
my $repetitions = 1;
my $resultsCSV = "./res.csv";
my $quiet = 0;
my $intervalGap = 10; # 10 seconds 
my $nTags = 0;
my $numHostGroups = 0;

# influx
my $influxProps = "/usr/local/groundwork/config/influxdb.properties";
my $influxStorage = "/usr/local/groundwork/influxdb/var/lib/data";
#
# this block - these are all now overridden by values in props file
my $influxHost = Sys::Hostname::hostname(); # default - this host; TBD param cli
my $influxPort = "8086";
my $proto = 'http';
#my $influxDatabase = "groundwork";
my $influxDatabase = "notgw"; # get this from influx.props instead

my $tsType = 'influx'; # influx, opentsdb, rrd
my $batchSize = 1000;
my $curlSSLOpts = ''; # for self-signed cert to work with curl
my $cliSSLOpts = '';
my $ssl = undef;
my $auth = undef;
my ( $authAdminOpts, $authNonAdminOpts ) = "";
my $cliAuthOpts ;

# gw
my $wsClientProps = "/usr/local/groundwork/config/ws_client.properties";
my $GWAppName = "perftesting.$pid"; # to ensure don't have multi's using same token ?
my $useGWRESTAPI = undef;
my ( $GWAuthToken, $GWApiUrl);

# misc
my $useStartTimeTransfer = undef;
my $noTags = undef;
my $rrds = undef;;
my $debug = 1;
my $batchMode = undef;
my $justPrep = undef;
my $multi = undef;
my $readTest = undef;
my @singleTestDetailedResults;
my %summary;
my $forceRedploy=undef;

parse_opts();

if ( defined $justPrep ) { 
    prep_for_tests();
    exit;
}

print "Process ID : $pid\n";


if ( defined $batchMode ) { 
    define_and_run_tests();
}
else {
    my ($avgDatapointsWrittenPerSecTotal,$avgValuesWrittenPerSecTotal,$timeTaken);
    run_a_test( \$avgDatapointsWrittenPerSecTotal, \$avgValuesWrittenPerSecTotal, \$timeTaken, \@singleTestDetailedResults );
    summarize_results( \@singleTestDetailedResults, \%summary );
    write_results_to_csv( \%summary, 1, "summary.csv.$pid", undef );
    open ( F, "> supersummary.csv.$pid" ) or die "Failed to open supersummary.csv : $!\n";
    if ( not defined $readTest ) {
        print  F "avgAvgValuesWrittenPerSec, medianAvgValuesWrittenPerSec, avgTotalCurlWriteTime (ms),medianTotalCurlWriteTime (ms),test duration (sec)\n";
        print  F "$summary{avgAvgValuesWrittenPerSec},";
        print  F "$summary{medianAvgValuesWrittenPerSec},";
        printf F "%0.1f,", $summary{avgTotalCurlWriteTime} * 1000 ;
        printf F "%0.1f,", $summary{medianTotalCurlWriteTime} * 1000 ;
        print  F "$timeTaken\n";
    }
    else { 
        print  F "avgReadTime (ms),medianTotalCurlWriteTime (ms),test duration (sec)\n";
        printf F "%0.3f,", $summary{avgReadTime} * 1000 ;
        print  F "$timeTaken\n";
    }
    close  F;
    system "cat supersummary.csv.$pid";
}
exit;

# ---------------------------------------------------------------------------------
sub summarize_results
{
    my ( $detailedResRef, $summaryHashRef ) = @_;

    if ( defined $useGWRESTAPI ) {
        
        if (  not defined $readTest ) {
            my ( @avgINFLUX, @avgPERF, @avgPRE, @avgValuesWrittenPerSec, @totalCurlWriteTime, @avgProjected );
            foreach my $testDetailHash ( @{$detailedResRef} ) {
                push @avgINFLUX, $testDetailHash->{avgINFLUX} ;
                push @avgPERF, $testDetailHash->{avgPERF} ;
                push @avgPRE, $testDetailHash->{avgPRE} ;
                push @avgValuesWrittenPerSec, $testDetailHash->{avgValuesWrittenPerSec} ;
                push @totalCurlWriteTime, $testDetailHash->{totalCurlWriteTime};
                push @avgProjected, $testDetailHash->{projected};
            }

            ${$summaryHashRef}{"avgAvgINFLUX"} = mean ( @avgINFLUX );
            ${$summaryHashRef}{"avgAvgPERF"} = mean ( @avgPERF );
            ${$summaryHashRef}{"avgAvgPRE"} = mean ( @avgPRE );
            ${$summaryHashRef}{"avgAvgValuesWrittenPerSec"} = int ( mean ( @avgValuesWrittenPerSec ) );
            ${$summaryHashRef}{"avgTotalCurlWriteTime"} = mean ( @totalCurlWriteTime );
            ${$summaryHashRef}{"avgAvgProjected"} = mean ( @avgProjected );
    
            ${$summaryHashRef}{"medianAvgINFLUX"} = median ( @avgINFLUX );
            ${$summaryHashRef}{"medianAvgPERF"} = median ( @avgPERF );
            ${$summaryHashRef}{"medianAvgPRE"} = median ( @avgPRE );
            ${$summaryHashRef}{"medianAvgValuesWrittenPerSec"} = int ( median ( @avgValuesWrittenPerSec ) ) ;
            ${$summaryHashRef}{"medianTotalCurlWriteTime"} = median ( @totalCurlWriteTime );
        }
        else {
            my @readTimes;
            foreach my $testDetailHash ( @{$detailedResRef} ) {
                push @readTimes, $testDetailHash->{readTime};
            }
            ${$summaryHashRef}{"avgReadTime"} =  mean ( @readTimes ) ;
        }
    }

    else {
        my ( @avgDatapointsWrittenPerSec, @avgValuesWrittenPerSec, @totalCurlWriteTime, @readTimes ) ;
            #'avgDatapointsWrittenPerSec' => '33333.3333333333',
            #'avgValuesWrittenPerSec' => '66666.6666666667',
            #'influxHttpdPointsWrittenOk' => '2200',
            #'influxHttpdwriteReqDurationNs' => '85786778',
            #'influxWALWriteRoutineNSPerDatapoint' => '38993.99',
            #'totalCurlWriteTime' => '0.006',
        foreach my $testDetailHash ( @{$detailedResRef} ) {
            if ( defined $readTest ) {
                push @readTimes, $testDetailHash->{readTime};
            }
            else {
                push @avgDatapointsWrittenPerSec, $testDetailHash->{avgDatapointsWrittenPerSec} ;
                push @avgValuesWrittenPerSec, $testDetailHash->{avgValuesWrittenPerSec} ;
                push @totalCurlWriteTime, $testDetailHash->{totalCurlWriteTime} ;
            }
        }

        if ( defined $readTest ) {
            ${$summaryHashRef}{"avgReadTime"} =  mean ( @readTimes ) ;
        }
        else {
            ${$summaryHashRef}{"avgAvgDatapointsWrittenPerSec"} = int ( mean ( @avgDatapointsWrittenPerSec ) );
            ${$summaryHashRef}{"avgAvgValuesWrittenPerSec"} = int ( mean ( @avgValuesWrittenPerSec ) ) ;
            ${$summaryHashRef}{"avgTotalCurlWriteTime"} = mean ( @totalCurlWriteTime );
             
            ${$summaryHashRef}{"medianAvgDatapointsWrittenPerSec"} = int ( median ( @avgDatapointsWrittenPerSec ) ) ;
            ${$summaryHashRef}{"medianAvgValuesWrittenPerSec"} = int (median ( @avgValuesWrittenPerSec ));
            ${$summaryHashRef}{"medianTotalCurlWriteTime"} = median ( @totalCurlWriteTime );
        }
    }
}

# ---------------------------------------------------------------------------------
sub define_and_run_tests
{
    my ( @tests, $test, %testConfig,
         $avgDatapointsWrittenPerSecTotal,$avgValuesWrittenPerSecTotal,$timeTaken,
         @detailedResults, %summary
        );

    # define array of test config hashes
    @tests = (

        { descr => "Test 1",             hosts=>1000,  services=>20,  mps=>2, intervals=>288, gap=>300, batch=>10000, reps=>10 },
        { descr => "Test 1 - STT",       hosts=>1000,  services=>20,  mps=>2, intervals=>288, gap=>300, batch=>10000, reps=>10, STT=>1 },

        { descr => "Test 2",             hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>100 },
        { descr => "Test 2 - STT",       hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>100, STT=>1 },

        { descr => "Test 3 - 1k batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>1000,  reps=>100 },
        { descr => "Test 3 - 5k batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>5000,  reps=>100 },
        { descr => "Test 3 - 10 batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>100 },
        { descr => "Test 3 - 15 batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>15000, reps=>100 },
        { descr => "Test 3 - 20 batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>20000, reps=>100 },
        { descr => "Test 3 - 25 batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>25000, reps=>100 },
        { descr => "Test 3 - 30 batch ", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>30000, reps=>100 },

        { descr => "Test 3 - 15 batch - STT", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>15000, reps=>100, STT=>1 },

        { descr => "Test 4",             hosts=>1000,  services=>20,  mps=>2, intervals=>600, gap=>1  , batch=>25000, reps=>5   },
        { descr => "Test 4 - STT",       hosts=>1000,  services=>20,  mps=>2, intervals=>600, gap=>1  , batch=>25000, reps=>5   },

        { descr => "Test 5 - 2 mps"    , hosts=>500 ,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>50  },
        { descr => "Test 5 - 25 mps"   , hosts=>500 ,  services=>20,  mps=>25, intervals=>1,   gap=>300, batch=>10000, reps=>50  },
        { descr => "Test 5 - 50 mps"   , hosts=>500 ,  services=>20,  mps=>50, intervals=>1,   gap=>300, batch=>10000, reps=>50  },

    );


    # For formatting purposes, don't mix gw=> with no gw tests in a batch for now
    @tests = (

        #{ descr => "Test 2",             hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>100 },
        #{ descr => "[GW] Test 2",        hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>100,   reps=>100, gw=>1 },

        { descr => "t1 INFLUX",  hosts=>10 , services=>20, mps=>2 , intervals=> 1, gap=> 1 , batch=> 1000 ,  reps=>100 },
        { descr => "t1 GW",      hosts=>10 , services=>20, mps=>2 , intervals=> 1, gap=> 1 , batch=> 1000 ,  reps=>100, gw=>1 },
        
    );

    # Influx - exploration into relationship between tag set cardinality and write time metrics
    @tests = (
        { descr=>"1",   hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>0  } ,
        { descr=>"51",  hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>50 } ,
        { descr=>"101", hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>100 } ,
        { descr=>"151", hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>150 } ,
        { descr=>"201", hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>200 } ,
        { descr=>"251", hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>250 },
    );

    # Varying influx batch size
    #@tests = (
    #   { descr => "1000", hosts=>1000,  services=>20,  mps=>2, intervals=>1, gap=>300, batch=>1000,  reps=>50 },
    #   { descr => "5000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>5000,  reps=>50 },
    #   { descr => "10000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>10000, reps=>50 },
    #   { descr => "15000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>15000, reps=>50 },
    #   { descr => "20000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>20000, reps=>50 },
    #   { descr => "25000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>25000, reps=>50 },
    #   { descr => "50000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>50000, reps=>50 },
    #   { descr => "75000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>50000, reps=>50 },
    #   { descr => "100000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>50000, reps=>50 },
    #    { descr => "200000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>50000, reps=>50 },
    #    { descr => "300000", hosts=>1000,  services=>20,  mps=>2, intervals=>1,   gap=>300, batch=>50000, reps=>50 },
    #);

    # varying field set cardinality
    #@tests = (
        #{ descr=>"2",   hosts=>500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10,   } ,
        #{ descr=>"20",  hosts=>500, services=>20, mps=>20, gap=>1, intervals=>1, batch=>25000, reps=>10,} ,
        #{ descr=>"40", hosts=>500, services=>20, mps=>40, gap=>1, intervals=>1, batch=>25000, reps=>10, } ,
        #{ descr=>"60", hosts=>500, services=>20, mps=>60, gap=>1, intervals=>1, batch=>25000, reps=>10, } ,
        #{ descr=>"80", hosts=>500, services=>20, mps=>80, gap=>1, intervals=>1, batch=>25000, reps=>10,  } ,
        #{ descr=>"100", hosts=>500, services=>20, mps=>100, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"150", hosts=>500, services=>20, mps=>150, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"200", hosts=>500, services=>20, mps=>200, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"300", hosts=>500, services=>20, mps=>300, gap=>1, intervals=>1, batch=>25000, reps=>10},
    #);

    # basics - influx - scaling hosts
    #@tests = (
        #{ descr=>"250", hosts=>250, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"500", hosts=>500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"1000", hosts=>1000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"1500", hosts=>1500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"2000", hosts=>2000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"4000", hosts=>4000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"6000", hosts=>6000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"8000", hosts=>8000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"10000", hosts=>10000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>500, reps=>10, gw=>1},
        #{ descr=>"1000", hosts=>1000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"1500", hosts=>1500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"2000", hosts=>2000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"5000", hosts=>5000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"10000", hosts=>10000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"20000", hosts=>20000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
        #{ descr=>"30000", hosts=>30000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10, },
    #);

    # varying mps  app server emulation
   #@tests = (
   #    #[root@grafbridge-demo tsperf]# ./tsperf.pl -hosts 10 -ser 1 -metrics 1000 -gap 300 -intervals 1 -batchsize 25000 -rep 100 -q -y^C
   #    { descr=>"10", hosts=>10, services=>1, mps=>1000, gap=>1, intervals=>1, batch=>25000, reps=>25, },
   #    { descr=>"100", hosts=>100, services=>1, mps=>1000, gap=>1, intervals=>1, batch=>25000, reps=>25, },
   #    { descr=>"500", hosts=>1000, services=>1, mps=>1000, gap=>1, intervals=>1, batch=>25000, reps=>25, },
   #    #{ descr=>"1000", hosts=>10000, services=>1, mps=>1000, gap=>1, intervals=>1, batch=>25000, reps=>25, },
   #);

    #@tests = (
        #{ descr=>"300",   hosts=>300, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"1000",  hosts=>1000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"3500",  hosts=>3500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"5000",  hosts=>5000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"7500",  hosts=>7500, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
        #{ descr=>"10000", hosts=>10000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
    #    { descr=>"25000", hosts=>25000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>10},
    #);

    # comparison test
    @tests = (

        # Influx direct
       #{ descr=>"if300", hosts=>300,   services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
       #{ descr=>"500",   hosts=>500,   services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
       #{ descr=>"1000",  hosts=>1000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
       #{ descr=>"1500",  hosts=>1500,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
        { descr=>"2000",  hosts=>2000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
        { descr=>"5000",  hosts=>5000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, ntags=>12 },
       #{ descr=>"10000", hosts=>10000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>25, ntags=>12 },

        # GW direct - this chokes a bit - better to run manually - possibly hg guava cache not updated in time
       #{ descr=>"gw300", hosts=>300,   services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
       #{ descr=>"500",   hosts=>500,   services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
       #{ descr=>"1000",  hosts=>1000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
       #{ descr=>"1500",  hosts=>1500,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
        { descr=>"2000",  hosts=>2000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
        { descr=>"5000",  hosts=>5000,  services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>50, gw=>1, hgs=>12 },
#       { descr=>"10000", hosts=>10000, services=>20, mps=>2, gap=>1, intervals=>1, batch=>25000, reps=>25, gw=>1, hgs=>12 },

    );

    # space tests
    #@tests = (
    #    #./tsperf.pl -hosts 300 -services 20 -metrics 2 -gap 300 -interval 8640 -batchsize 25000 -ntags 12
    #   { descr=>"300", hosts=>300, services=>20, mps=>2, gap=>300, intervals=>8640, batch=>25000, ntags=>12 },
    #   { descr=>"300", hosts=>300, services=>20, mps=>2, gap=>300, intervals=>8640, batch=>25000, ntags=>12 },
    #);
    
    # headrs that are common to both gw and influx results
    print "test name,avgAvgValuesWrittenPerSec,medianAvgValuesWrittenPerSec,avgTotalCurlWriteTime (ms),medianTotalCurlWriteTime (ms),test duration (sec)\n";

    my $printHdrs = 1;
    foreach $test ( @tests )  {
        %testConfig = %{$test};
        $numHosts             = ( defined $testConfig{hosts}      ? $testConfig{hosts}     : 1000 );
        $numServices          = ( defined $testConfig{services}   ? $testConfig{services}  : 20   ); 
        $mps                  = ( defined $testConfig{mps}        ? $testConfig{mps}       : 2    );
        $batchSize            = ( defined $testConfig{batch}      ? $testConfig{batch}     : 1000 );
        $intervals            = ( defined $testConfig{intervals}  ? $testConfig{intervals} : 1    );
        $intervalGap          = ( defined $testConfig{gap}        ? $testConfig{gap}       : 1    );
        $repetitions          = ( defined $testConfig{reps}       ? $testConfig{reps}      : 1 );
        $useStartTimeTransfer = ( defined $testConfig{STT}        ? $testConfig{STT}       : undef );
        $useGWRESTAPI         = ( defined $testConfig{gw}         ? $testConfig{gw}        : undef );
        $nTags                = ( defined $testConfig{ntags}      ? $testConfig{ntags}     : 0 );
        $numHostGroups        = ( defined $testConfig{hgs}        ? $testConfig{hgs}       : 0 );

        run_a_test( \$avgDatapointsWrittenPerSecTotal, \$avgValuesWrittenPerSecTotal, \$timeTaken, \@detailedResults );
        %summary=();
        summarize_results( \@detailedResults, \%summary );
        #print Dumper \%summary;
        #write_results_to_csv( \%summary, 1, undef, $testConfig{descr} ); 

        #printf "$testConfig{descr},$summary{avgTotalCurlWriteTime},$summary{avgAvgValuesWrittenPerSec},$summary{avgTotalCurlWriteTime},$summary{medianTotalCurlWriteTime},$timeTaken\n";
        print "$testConfig{descr},";
        print "$summary{avgAvgValuesWrittenPerSec},";
        print "$summary{medianAvgValuesWrittenPerSec},";
        printf "%0.1f,", $summary{avgTotalCurlWriteTime} * 1000 ;
        printf "%0.1f,", $summary{medianTotalCurlWriteTime} * 1000 ;
        print "$timeTaken\n";

        $printHdrs = 0;
    }

}

# ---------------------------------------------------------------------------------
sub run_a_test
{
    my ( $avgDatapointsWrittenPerSecTotalRef, $avgValuesWrittenPerSecTotalRef, $timeTakenRef, $detailedResultsRef ) = @_;

    my $startTime = time();
    if ( defined $useGWRESTAPI ) { 

        # get a GW token once for the entire test - this might backfire if the tests take > 8hrs to run - its ok for now TBD
        gwauth( \$GWAuthToken, \$GWApiUrl ) if not $GWAuthToken;
        die "Could not get GW token - is GW running? Quitting\n" if not $GWAuthToken ;
        
        # If using GW REST API, first create hosts and hostgroups in GW for guava caching to work which is required for efficient hostgroup tagging
        create_groundwork_objects();
    }

    if ( not ( defined $multi or $readTest ) ) { # restart influx, etc - don't do this if running multi - instead first run $0 -justprep, then run tests
        prep_for_tests() ; 
    }

    get_influx_config() if ( $readTest ); # except still need to read the config now

    run_tests( $avgDatapointsWrittenPerSecTotalRef, $avgValuesWrittenPerSecTotalRef, $detailedResultsRef ); 

    ${$timeTakenRef} = time() - $startTime;
}

# ---------------------------------------------------------------------------------
sub prep_for_tests
{
    get_influx_config(); # figure out the influx api url, host etc
    restart_influx(); # to reset stats
    wait_for_influxdb(); # need to have influx back up to do drop/create and everything else
    purge_influx_database();  # even if running against GW, want this to be empty first to confirm data arrived later
    purge_stats_files( [ $resultsCSV, "$resultsCSV.details" ] );
}

# ---------------------------------------------------------------------------------
sub get_influx_config
{
    my @props;
    my %props;
    my $props;

    if (  ! -e $influxProps or ! -r $influxProps ) { 
        die "No $influxProps or unreadable - quitting\n";
    }

    # figure out $influxHost, $influxPort and $proto
    # These are now defined in the influx props so grab them from there now instead of twiddling settings in this test script

    open P, $influxProps or do { die "Cannot read $influxProps : $!\n"; } ;
    foreach my $cline ( <P> ) {
        next if $cline =~ /(^\s*#|^\s*$)/ or not $cline;
        chomp $cline;
        $cline =~ s/\s+//g;
        my ( $k, $v ) = split /=/, $cline ;
        $props{$k} = $v;
    }

    close P;
    die "No influxdb database defined in $influxProps\n" if not exists $props{database} ;
    die "No influxdb api url defined in $influxProps\n" if not exists $props{url} ;
    $influxDatabase = $props{database};


    #'url' => 'http://demo64-702:8086'

    $proto = (split /:/, $props{url})[0];
    $influxHost = (split /:/, $props{url})[1]; 
    $influxHost =~ s/^\/\///g;
    $influxPort = (split /:/, $props{url})[2]; 
    
    print "InfluxDB settings : proto:$proto, host:$influxHost, port:$influxPort\n" if not $quiet;
    
}

# ---------------------------------------------------------------------------------
sub run_tests 
{

    # runs the test <repetitions> times over and produces averaged results
    
    my ( $avgDatapointsWrittenPerSecTotalRef, $avgValuesWrittenPerSecTotalRef, $detailedResultsRef, $avgReadsPerSecRef ) = @_;
    my ( @detailedResults, $resSet, %summaryResults ); # an array of hashes of results that can be used for averaging later
    my ( $avgDatapointsWrittenPerSecTotal, $influxWALWriteRoutineNSPerDatapointTotal, $printHdrs, $avgValuesWrittenPerSecTotal );

    @{$detailedResultsRef} = (); # need this for ref to work later

    $printHdrs = 1; # controls results output for csv consumption
    print "Reps " if ( $repetitions > 1 and not $quiet );

    for ( my $rep = 1; $rep <= $repetitions ; $rep++ ) {
        print "." if ( $repetitions > 1 and not $quiet );
        if ( not defined $readTest ) {
            generate_and_send_influx_perf_data( $detailedResultsRef, $printHdrs, $rep );
            #
            # to verify has made it in
            #my $tc = "influx -database perf -execute \"select last(metric_2) from service_20 where \"hostname\" = 'host_300'\"";
            #print "~~" x 40 . "\n";; system( $tc ); print "~~" x 40 . "\n";;
            # purge_influx_database();  # even if running against GW, want this to be empty first to confirm data arrived later
        }
        else {
            read_influx_perf_data( $detailedResultsRef, $printHdrs );
        }
        $printHdrs = 0;
    }
print "\n";
    #die Dumper $detailedResultsRef;

    print "\n" if ($repetitions > 1 and not $quiet );


    if ( not defined $readTest ) {
        $avgDatapointsWrittenPerSecTotal = $influxWALWriteRoutineNSPerDatapointTotal = $avgValuesWrittenPerSecTotal = 0;
        foreach $resSet ( @{$detailedResultsRef} ) {
            if ( not defined $useGWRESTAPI ) {
                $avgDatapointsWrittenPerSecTotal += $resSet->{avgDatapointsWrittenPerSec};
                #$influxWALWriteRoutineNSPerDatapointTotal += $resSet->{influxWALWriteRoutineNSPerDatapoint};
            }
            $avgValuesWrittenPerSecTotal += $resSet->{avgValuesWrittenPerSec};
        }

    
        %summaryResults = (
             ##avgValuesWrittenPerSecTotal => int ( $avgDatapointsWrittenPerSecTotal * $mps / ( scalar @detailedResults )),
             #avgValuesWrittenPerSecTotal => int ( $avgValuesWrittenPerSecTotal / ( scalar @detailedResults )),
             #avgValuesWrittenPerSecTotal => int ( $avgDatapointsWrittenPerSecTotal * $mps / ( scalar @{$detailedResultsRef} )),
             avgValuesWrittenPerSecTotal => int ( $avgValuesWrittenPerSecTotal / ( scalar @{$detailedResultsRef} )),
             numHosts => $numHosts,
             servicesPerHost => $numServices,
             metricsPerService => $mps,
             measurementIntervals => $intervals,
             intervalGap => $intervalGap,
             totalNumberDatapointsToWrite => $numServices * $numHosts * $intervals,
             timeSpanMinutes => $intervals * $intervalGap / 60,
             totalNumberOfInfluxTimeSeries => $numServices * $numHosts,
             totalNumberOfInfluxValues => $numHosts * $numServices * $mps * $intervals,
             batchSize => $batchSize,
             repetitions => $repetitions,
        );

        if ( not defined $useGWRESTAPI ) {
            $summaryResults{avgDatapointsWrittenPerSecTotal} = int ( $avgDatapointsWrittenPerSecTotal / ( scalar @{$detailedResultsRef} )),
            ${$avgDatapointsWrittenPerSecTotalRef} = $summaryResults{avgDatapointsWrittenPerSecTotal};
        }
    
        ${$avgValuesWrittenPerSecTotalRef} = $summaryResults{avgValuesWrittenPerSecTotal};
    }
    else {
        my $readsPerSecTotal = 0;
        foreach $resSet ( @{$detailedResultsRef} ) {
            $readsPerSecTotal += $resSet->{readTime};
        }
        ${$avgReadsPerSecRef} = $readsPerSecTotal / scalar @{$detailedResultsRef};
        $summaryResults{avgReadsPerSecTotal} = ${$avgReadsPerSecRef};
    }

    write_results_to_csv( \%summaryResults, 1, $resultsCSV );

}

# ---------------------------------------------------------------------------------
sub read_influx_perf_data
{
    my ( $resultsArrayRef, $printHdrs ) = @_;

    my ( @writeBuffer, $totalTime, $totalPointsWritten , $totalErrors, @GWInternalMetrics);
    my ( $avgINFLUX, $avgPERF, $avgPRE, %results );

    if (not send_data_to_api( \@writeBuffer, \$totalTime, $totalPointsWritten , $totalErrors, \@GWInternalMetrics) ) {
        print "ERROR An error occurred sending the request to the api\n"; #TBD what next
        return 0;
    }

    %results = (
          readTime => $totalTime
    );

    push ( @{$resultsArrayRef}, \%results );
    write_results_to_csv( \%results, $printHdrs, "$resultsCSV.details" );

}

# ---------------------------------------------------------------------------------
sub generate_and_send_influx_perf_data
{

    my ( $resultsArrayRef, $printHdrs, $iteration ) = @_;

    my ( $hostCount, $hostName,
         $serviceCount, $serviceName,
         $metricCount, $metricName,
         $timestamp, $timestampNS, $dataPoint, $startTime,
         @fieldTags, $fieldTags,
         @fieldValues, $fieldValues,
         @writeBuffer,
         $totalTime, $totalPointsWritten, $totalWriteErrors,
         %influxHTTPDStats, %influxShardStats,
         %results,
         @GWInternalMetrics, $avgINFLUX, $avgPERF, $avgPRE, $totalTimeExcludingPERF,
    );

    if ( not $quiet ) {
        print  "-" x 80 . "\n";
        print  "Number of hosts = $numHosts\n";
        print  "Services per host = $numServices\n";
        print  "Metrics per service : $mps\n";
        print  "Number of measurement intervals : $intervals\n";
        print  "Gap between intervals : $intervalGap seconds\n";
        printf "Total number of datapoints to write : %d\n", $numServices * $numHosts * $intervals;
        printf "Time span : %d seconds (last %d minutes, or %0.2f hours)\n", $intervals * $intervalGap, $intervals * $intervalGap / 60, $intervals * $intervalGap / 60 / 60;
        printf "Number of influx time series ( number of hosts * number of services ) = %d\n" , $numServices * $numHosts;
        printf "Total number of values in dataset ( hosts * services * metrics per service * intervals ) = %d\n", $numHosts * $numServices * $mps * $intervals;
        print  "Datapoints batch size : $batchSize\n";
        print  "ntags: $nTags\n";
        print  "hgs: $numHostGroups\n";
        # doing a du might not be correct either since stuff gets compressed on a schedule - need to better understand the storage engine to do this.
        print "Size of $influxStorage after test run: ". `du -sb $influxStorage | cut -f1`  . " bytes\n";
        print "-" x 80 . "\n\n";
    }
    if ( not defined $yes ) { 
        print  "Hit Enter to start .....\n"; 
        <STDIN>;
    }

    $startTime = time();
    if ( not $quiet ) {
        printf "Epoch seconds start time for data: %d\n",  $startTime - ( $intervals * $intervalGap );
        #print "Time now $startTime\n";
    }
    

    $totalTimeExcludingPERF = $totalTime = $totalPointsWritten = $totalWriteErrors = 0;
    for ( $serviceCount = 1; $serviceCount <= $numServices; $serviceCount++ ) {
        $serviceName = "service_" . $serviceCount;

        for ( $hostCount = 1; $hostCount <= $numHosts; $hostCount++ ) {
            $hostName = "host_" . $hostCount;

            # initialize the first timestamp
            $timestamp = $startTime - ( $intervals * $intervalGap ); 
            #$timestampNS = $timestamp . "000000000"; # convert the timestamp from epoch seconds to epoch nanoseconds

            $dataPoint = 1;
            while ( $dataPoint <= $intervals ) {

                $timestampNS = $timestamp . "000000000"; # convert the timestamp from epoch seconds to epoch nanoseconds
                
                if ( not defined $useGWRESTAPI ) {

                    # build the field values 
                    @fieldValues = ( );
                    for ( $metricCount = 1; $metricCount <= $mps; $metricCount++ ) {
                        push @fieldValues, "metric_$metricCount=" . sprintf ("%0.4f", rand($metricCount) );  
                    }
                    $fieldValues = join(",", @fieldValues);
    
                    if ( defined $noTags ) { 
                        push @writeBuffer, "$serviceName $fieldValues $timestampNS";
                    } 
                    else {

                        # added to explore perf response of adding more tags than just hostname
                        #push @writeBuffer, "$serviceName,hostname=$hostName $fieldValues $timestampNS";
                        my ( @tags, $tags );
                        $tags = "hostname=$hostName";
                        if ( $nTags > 0 ) {
                            for (my $i=1; $i<=$nTags; $i++) {  push @tags, "hostgroup_$i=true" ; } ; $tags .= "," . join ",", @tags;
                        }
                        push @writeBuffer, "$serviceName,$tags $fieldValues $timestampNS";
                        #push @writeBuffer, "$serviceName,hostname=$hostName,timeTag1=${timestampNS}.abc,timeTag2=${timestampNS}.def $fieldValues $timestampNS"; # 3 tags
                    }

                }
                else {
                    # build the fieldTags and fieldValues lists
                    for ( $metricCount = 1; $metricCount <= $mps; $metricCount++ ) {
                        my $value = sprintf ("%0.4f", rand($metricCount) );
                        push @writeBuffer, "{ \"appType\":\"OS\", \"label\":\"metric_$metricCount\", \"serverName\":\"$hostName\", \"serverTime\":$timestamp, \"serviceName\":\"$serviceName\", \"value\" : $value }";
                    }
                }
    
                $timestamp = $timestamp + $intervalGap; # increment the timestamp by adding one measurement interval to it
                $dataPoint++; # keep track of how many datapoints have been created

                if ( scalar @writeBuffer >= $batchSize ) { 
                    # write the batch of data to influx or GW API
                    if ( send_data_to_api( \@writeBuffer, \$totalTime, \$totalPointsWritten , \$totalWriteErrors, \@GWInternalMetrics) )  {
                        @writeBuffer = (); # purge the writeBuffer 
                    }
                    else { 
                        print "ERROR An error occurred writing the data to the api\n";
                        # what next? TBD
                    }
                } 

            } #intervals
        } # hosts
    } # services

    # If there's anything in the buildData buffer, write it now
    if ( scalar @writeBuffer != 0 ) { 
        # write the batch of data to influx or GW API
        if (not send_data_to_api( \@writeBuffer, \$totalTime, \$totalPointsWritten , \$totalWriteErrors, \@GWInternalMetrics) ) {
            print "ERROR An error occurred writing the data to the api\n"; #TBD what next
        }
    }

    # get internal influxdb stats for httpd
    # For some clues as to what these are in more detail : https://github.com/influxdata/influxdb/blob/master/services/httpd/service.go :
    #     writeReqDurationNs  =>  Number of (wall-time) nanoseconds spent inside write requests # Presumably this is WriteAhead Log time, not 'wall'
    #     pointsWrittenOK     =>  Number of points written OK

    # SKipping this for now - used to be in the output but not using it.
    #if ( not defined $useGWRESTAPI ) { # only applicable to influx testing ?? TBD will be useful for GW testing too actually
    #    get_influx_stats( \%influxHTTPDStats, "show stats for 'httpd'" ) if not defined $useGWRESTAPI; # only applicable to influx testing
    #}

    # get internal influxdb stats for sizing
    # For now this isn't working - sometimes it takes a while for the sum to become non zero and I'm still not grokking what this query does exactly:)
    # https://groups.google.com/forum/#!topic/influxdb/I5eady_Ta5Y
    #get_influx_stats( \%influxShardStats, 'select sum(diskBytes) from _internal."monitor"."shard" where time > now() - 10s group by "groundwork"' );

    if ( not $quiet ) {
        print "\n" . "="x80 . "\n";

        if ( not defined $useGWRESTAPI ) { 
            print   "TOTAL DATA POINTS WRITTEN : $totalPointsWritten\n";
            printf  "TOTAL VALUES WRITTEN : %d\n", $totalPointsWritten * $mps;
        }
        else {  # GW
            # GW datapoints are not grouped together efficiently like in influx 
            printf  "TOTAL VALUES WRITTEN : %d\n", $totalPointsWritten;
        }
        print   "TOTAL WRITE ERRORS : $totalWriteErrors\n";
        print   "TOTAL WRITE TIME : $totalTime\n";
        printf  "AVERAGE DATAPOINTS/SECOND : %d\n", $totalPointsWritten/$totalTime if not defined $useGWRESTAPI; # datapoints less clear in GW measurments
        printf  "AVERAGE VALUES/SECOND : %d\n", $totalPointsWritten * $mps/$totalTime;

        # not using this anymore
        #if ( not defined $useGWRESTAPI ) {
        #    print   "Influx internal stats:\n";
        #    # doing a du might not be correct either since stuff gets compressed on a schedule - need to better understand the storage engine to do this.
        #    printf  "\tTotal points written ok [httpd.pointsWrittenOK] : %d\n", $influxHTTPDStats{pointsWrittenOK};
        #    printf  "\tTotal time spent inside WAL write requests [httpd.writeReqDurationNs] : %0.4f seconds\n", $influxHTTPDStats{writeReqDurationNs} / 1000000000 ;
       # 
       #     # this is misleading - its actually datapoints per time spent in WAL write routines
       #     #printf  "\tData points per second : %0d\n", $influxHTTPDStats{pointsWrittenOK} / ( $influxHTTPDStats{writeReqDurationNs} / 1000000000 );
       #     printf  "\tWAL routine time (ns) spent per data point : %f\n",  $influxHTTPDStats{writeReqDurationNs} / $influxHTTPDStats{pointsWrittenOK} ;
       #     #printf  "\tShard size [shared.diskBytes for last 10 sec grouped by groundwork database: %0d Bytes\n", $influxShardStats{sum};
       # }

        print "\n" . "="x80 . "\n";
        print   "Size of $influxStorage after test run: ". `du -sb $influxStorage | cut -f1` . " bytes\n";
    }

    my $sumExcludingPERF; my $avgSEP;
    if ( defined $useGWRESTAPI ) {
        # @GWInternalMetrics will contain one hash entry per curl POST operation
        # This crunching produces averages per curl POST operation
        my ( $GWInternalMetricSet, $sumINFLUX, $sumPERF, $sumPRE);
        $sumINFLUX = $sumPERF = $sumPRE = 0;
        foreach $GWInternalMetricSet ( @GWInternalMetrics ) {
            $sumINFLUX += $GWInternalMetricSet->{INFLUX};
            $sumPERF += $GWInternalMetricSet->{PERF};
            $sumPRE += $GWInternalMetricSet->{PRE};
            $sumExcludingPERF += $GWInternalMetricSet->{timeMinusPERF};
        }
        #print Dumper \@GWInternalMetrics, $sumINFLUX, $sumPERF, $sumPRE;
        $avgINFLUX = ( $sumINFLUX / scalar @GWInternalMetrics ) /  1000 ; # ms
        $avgPERF = ( $sumPERF / scalar @GWInternalMetrics ) / 1000 ;  # ms
        $avgPRE = ( $sumPRE / scalar @GWInternalMetrics ) / 1000 ;  # ms
        $avgSEP = ( $sumExcludingPERF / scalar @GWInternalMetrics ) ;  # sec. the avg total time excluding perf
    }

    # test results
    %results = (
          avgValuesWrittenPerSec => $totalPointsWritten * $mps/$totalTime,
          totalValuesWritten => $totalPointsWritten * $mps,
          totalWriteErrors   => $totalWriteErrors,
          totalCurlWriteTime => $totalTime,
          totalPointsWritten => $totalPointsWritten,
    );
    if ( not defined $useGWRESTAPI  ) {
          $results{avgDatapointsWrittenPerSec} = $totalPointsWritten/$totalTime,
    #     $results{influxHttpdPointsWrittenOk} = $influxHTTPDStats{pointsWrittenOK},
    #     $results{influxHttpdwriteReqDurationNs} = $influxHTTPDStats{writeReqDurationNs},
         #$results{influxDatapointsWrittenPerSec} = $influxHTTPDStats{pointsWrittenOK} / ( $influxHTTPDStats{writeReqDurationNs} / 1000000000 )
    #     $results{influxWALWriteRoutineNSPerDatapoint} = $influxHTTPDStats{writeReqDurationNs} / $influxHTTPDStats{pointsWrittenOK},
    }
    else { # GW
        $results{avgINFLUX} = $avgINFLUX ;
        $results{avgPERF} = $avgPERF ;
        $results{avgPRE} = $avgPRE ;
        $results{avgSEP} = $avgSEP ;
        #$results{projected} = $results{avgValuesWrittenPerSec} * ( $results{totalCurlWriteTime} / ( $results{totalCurlWriteTime} - $avgPERF) ) ;
        #$results{projected} = $results{avgValuesWrittenPerSec} * ( $results{totalCurlWriteTime} / ( $results{totalCurlWriteTime} - $avgPERF) ) ;
        #$results{totalTimeExcludingPERF} = $totalTime - $sumExcludingPERF;
        $results{projected} = ($totalPointsWritten * $mps)/$avgSEP;

    }
          
    push ( @{$resultsArrayRef}, \%results );
    write_results_to_csv( \%results, $printHdrs, "$resultsCSV.details" );

}

# ---------------------------------------------------------------------------------
sub send_data_to_api
{

    # returns 1 if ok, 0 if error 
    # TBD clean up error propogation etc etc blah blah blah
    
    my ( $bufferRef, $totalTimeRef, $totalPointsWrittenRef, $totalErrorsRef, $gwInternalMetricsArrayRef, $totalTimeExcludingPERFRef ) = @_;
    my ( $bufferSize, $curlCommand, $curlOutputFormat, $curlResponse, %curlResponseHash, $dataFile, $fh, $decodedResponse, $query );

    $bufferSize = scalar @{$bufferRef};
    
    # write data to a file
    $dataFile = "./api.data.$pid";
    open $fh, ">", $dataFile or do {
       print "$0: error opening $dataFile: $!";
       return 0 ;
    };

    if ( defined $useGWRESTAPI ) { # GW
        print $fh "{ \"perfDataList\" : [  " or do {
            print "$0: error writing to $dataFile: $!";
            return 0 ;
        };
        print $fh join(",\n", @{$bufferRef}) or do {
            print "$0: error writing to $dataFile: $!";
            return 0 ;
        };
        print $fh "  ]}" or do {
            print "$0: error writing to $dataFile: $!";
            return 0 ;
        };
    }
    else { # influx

        print $fh join("\n", @{$bufferRef}) or do {
            print "$0: error writing to $dataFile: $!";
            return 0 ;
        };
    }

    # close the dat file prior to curl'ing with it
    close $fh or do { 
       print "$0: error closing $dataFile: $!";
       return 0 ;
    };
    
    
    # use curl to send the read/write request to the API
    # curl output formatting

    if ( not defined $useGWRESTAPI ) { # influx
        $curlOutputFormat = '"responseCode=%{http_code}\ntotalTime=%{time_total}\nstartTransfer=%{time_starttransfer}\nconnect=%{time_connect}\n"';
        if ( not defined $readTest ) {
            $curlCommand = "curl $authNonAdminOpts $curlSSLOpts --silent --write-out $curlOutputFormat --include --request POST '$proto://$influxHost:$influxPort/write?db=$influxDatabase' --data-binary @" . $dataFile ;
        }
        else { 
            # query: get all field values for a given service and host 
            $query = 'q=select * from \"service_5\" where \"hostname\" = \'host_10\' ';
            $curlCommand = "curl $authNonAdminOpts $curlSSLOpts --silent --write-out $curlOutputFormat -G '$proto://$influxHost:$influxPort/query' --data-urlencode \"db=$influxDatabase\" --data-urlencode \"$query\" | grep -v '^{'";
            # Note The | grep -v '^{' is the easiest way here to dump the actual query results so avoiding having to recode the parsing of the influx api response structure from /write
        }
    }
    else { # GW
        $curlOutputFormat = '"\nCurlMetrics:responseCode=%{http_code},totalTime=%{time_total},startTransfer=%{time_starttransfer},connect=%{time_connect}\n"';
        if ( not defined $readTest ) {
            $curlCommand = "curl $curlSSLOpts --silent --write-out $curlOutputFormat -X POST -H 'GWOS-API-TOKEN:$GWAuthToken' -H 'GWOS-APP-NAME:$GWAppName' -H 'Accept: application/json' -H 'Content-Type: application/json' $GWApiUrl/perfdata --data-binary @" . $dataFile ;
        }
        else {
            # query: get all data points values for a given service and host , since 1/1/2000 downsampled to 1 second intervals (ie should mean no downsampling)
            $curlCommand = "curl $curlSSLOpts -s -w $curlOutputFormat -H 'GWOS-API-TOKEN:$GWAuthToken' -H 'GWOS-APP-NAME:$GWAppName' -H 'Accept: application/json' \"$GWApiUrl/perfdata?appType=OS&serverName=host_10&serviceName=service_5&startTime=946688473000&interval=1000\" ";
        }
    }

    print ".";
    $curlResponse =`$curlCommand`;
    #print "$curlCommand : $curlResponse\n";<STDIN>;

    if ( not defined $useGWRESTAPI ) { # influx
        %curlResponseHash = map{split /=/, $_}(split /\n/, $curlResponse);
        # only valid http status code from influx at the time of writing, for /write, is 204, and for /query it seems to be 200
        #if ( not exists $curlResponseHash{responseCode} or $curlResponseHash{responseCode} != 204 ) {
        if ( not exists $curlResponseHash{responseCode} or $curlResponseHash{responseCode} !~ /^(200|204)$/ ) {
            # TBD not sure how to get more details about write errors here other than watching _internal->write stats
            ${$totalErrorsRef}++; 
            print "Curl error detected: full curl response:\n\n$curlResponse\n";
            return 0;
        }
    }
    else { # GW
        
        # grab just the curl metrics from the curl response
        my $curlMetrics = $curlResponse;
        # really slow way
        #$curlMetrics =~ s/^.*(CurlMetrics:.*)$/$1/gm; 
        #foreach my $cr ( split /\n/, $curlResponse  ) { 
        #    $curlMetrics = $cr if $cr =~ /^CurlMetrics/; 
        #}
        #$curlMetrics =~ s/^CurlMetrics://g;
        # much faster way
        my @lines = split /\n/, $curlMetrics;
        $curlMetrics = $lines[$#lines];
        $curlMetrics =~ s/CurlMetrics://; 
        %curlResponseHash = map{split /=/, $_}(split /,/, $curlMetrics);

        # strip off the curl metrics
        $curlResponse =~ s/^CurlMetrics.*//gm; 
        $decodedResponse = decode_json( $curlResponse );
        #print "RESPONSE: $curlResponse\n"; print "METRICS: " . Dumper \%curlResponseHash;
        # map the json response back into a hash for parsing
        if ( ( not exists $curlResponseHash{responseCode} ) or ( $curlResponseHash{responseCode} != 200 ) ) {
            print "Error curling to GW REST API /perfdata endpoint : " . Dumper $decodedResponse;
            return 0;
        }
 
        # check for expected bits on success and error if not present
        #if ( not defined $readTest ) { # ie POST
        #    # could prob'y just check responseCode here
        #    if ( ( not exists $decodedResponse->{successful} ) or ( not exists $decodedResponse->{failed} ) ) {
        #        print "Error curling to GW REST API /perfdata endpoint : " . Dumper $decodedResponse;
        #        return 0;
        #    }
        #}
        #else { 
        #    if ( ( not exists $curlResponseHash{responseCode} ) or ( $curlResponseHash{responseCode} != 200 ) ) {
        #        print "Error curling to GW REST API /perfdata endpoint : " . Dumper $decodedResponse;
        #        return 0;
        #    }
        #}
        
    }
  
    # if want to measure time take to do the read/write op, excluding time taken to do other things like dns look, ssl etc, then -useSTT
    # https://netbeez.net/2015/07/08/http-transaction-timing-breakdown-with-curl/ or various other resources including man curl and look for -w
    if ( defined $useStartTimeTransfer ) {
        ${$totalTimeRef} += ($curlResponseHash{totalTime} - $curlResponseHash{startTransfer});
    } 
    else { 
        ${$totalTimeRef} += $curlResponseHash{totalTime};
    }

    ${$totalPointsWrittenRef} += $bufferSize;

    # print some progress output 
    my $percentComplete = ${$totalPointsWrittenRef} * $mps * 100 / ( $numHosts * $numServices * $mps * $intervals ) ;
    if ( not defined $readTest ) {
        if ( not $quiet and not defined $useGWRESTAPI ) { # since we're doing point by point for GW, datapoints context that made sense in influx doesn't in GW
            #printf "[%0.2f %%] Total write : ${$totalTimeRef}, total datapoints written ${$totalPointsWrittenRef}, running avg datapoints/sec: %d\n", 
            printf "[%0.2f] Wrote $bufferSize datapoints in $curlResponseHash{totalTime} sec. Running: total write ${$totalTimeRef}, total datapoints written ${$totalPointsWrittenRef}, running avg datapoints/sec: %d\n",
            $percentComplete, ${$totalPointsWrittenRef}/${$totalTimeRef};
            #printf "[running avg datapoints/sec: %d] Wrote $bufferSize datapoints to influx in $curlResponseHash{totalTime} seconds\n", ${$totalPointsWrittenRef}/${$totalTimeRef};
            #printf "Running average datapoints/second = %d points/sec\n", ${$totalPointsWrittenRef}/${$totalTimeRef};
        }
    }

    # extract the GW perfdata internal metrics and shove it into a hashref
    # A attribute of the response json is now 'metrics' => 'PRE=0,INFLUX=112,PERF=43,'
    # this isn't present for read ops so @{$gwInternalMetricsArrayRef} will be empty
    if ( defined $useGWRESTAPI  and not defined $readTest ) {
        my %gwInternalMetrics = map{split /=/, $_}(split /,/, $decodedResponse->{metrics});
        $gwInternalMetrics{timeMinusPERF} = $curlResponseHash{totalTime} - ( $gwInternalMetrics{PERF} / 1000 ); # TBD this is just non STT for now
        # More recent build skips PRE...
        $gwInternalMetrics{PRE} = 0;
        push @{$gwInternalMetricsArrayRef}, \%gwInternalMetrics;
    }

    return 1;
}


# ---------------------------------------------------------------------------------
sub purge_influx_database
{
    # drop and create the influxdb groundwork database
    my ( $command, $result ) ;
  
    print "Dropping groundwork influxdb database $influxDatabase at $proto://$influxHost:$influxPort \n" if not $quiet;
    $command = "curl $authAdminOpts $curlSSLOpts --silent --get '$proto://$influxHost:$influxPort/query?pretty=true' --data-urlencode 'q=DROP DATABASE $influxDatabase\'";
    $result = `$command`;

    print "Creating groundwork influxdb database $influxDatabase\n" if not $quiet;
    $command = "curl $authAdminOpts $curlSSLOpts --silent --get '$proto://$influxHost:$influxPort/query?pretty=true' --data-urlencode 'q=CREATE DATABASE $influxDatabase\'";
    $result = `$command`;

    if ( defined $auth ) { 
        # Not necessary if the user hasn't been dropped since it was created but heh...
        $command = "curl $authAdminOpts $curlSSLOpts --silent -XPOST '$proto://$influxHost:$influxPort/query?pretty=true' --data-urlencode \"q=CREATE USER gw WITH PASSWORD \'gw\'\"";
        $result = `$command`;

        $command = "curl $authAdminOpts $curlSSLOpts --silent -XPOST '$proto://$influxHost:$influxPort/query?pretty=true' --data-urlencode 'q=GRANT ALL ON \'$influxDatabase\' TO \'gw\'\'";
        $result = `$command`;
    }
       
}

# ---------------------------------------------------------------------------------
sub get_influx_stats
{

    my ( $statsHashRef, $query) = @_;
    
    my ( $command, $statLine , @stats, @headers, @values ) ;

    $query=~s/"/\\"/g;
    $command = "influx $cliAuthOpts $cliSSLOpts -execute \"$query\" -format csv";
    
    @stats = `$command`;
    while ( ! @stats ) { 
        print "Waiting for stats to show ...\n" if not $quiet; 
        sleep 2;
        @stats = `$command`;
    }
    
    chomp @stats;
    
    @headers = split ',', $stats[0];
    @values = split ',', $stats[1];

    for (my $hdr = 0 ; $hdr <= $#headers; $hdr++ ) { 
        $statsHashRef->{ $headers[$hdr] }  = $values[$hdr];
    }

}

# ---------------------------------------------------------------------------------
sub wait_for_influxdb
{
    my $command = "curl $authAdminOpts $curlSSLOpts -sl -I $proto://$influxHost:$influxPort/ping -o /dev/null";
    while ( system( $command ) >> 8 != 0 ) { 
        print "Waiting for influxdb to be ready ...\n" if not $quiet;
        sleep 2;
    }
}
# ---------------------------------------------------------------------------------
sub restart_influx
{
    my $thisHost = Sys::Hostname::hostname(); 
    if ( $thisHost ne $influxHost and $influxHost ne 'localhost') { 
        print "SSH to $influxHost and restart influx service ... hit enter when done\n";
        <STDIN>;
    }
    else {
        print "Restarting influx to reset internal statistics ...\n" if not $quiet;
        system( "/usr/local/groundwork/ctlscript.sh restart influxdb 2>&1 >/dev/null" );
    }
}

# ---------------------------------------------------------------------------------
sub write_results_to_csv
{
    my ( $resHash, $printHdrs, $file, $testName ) = @_;
    my ( $key, $val, @vals );

    open(CSV, ">> ".($file || '-'))  or do {
    #open CSV, ">> $file" or do { 
        die "Can't write to results CSV $file : $! - quitting\n";
    };
    
    if ( $printHdrs ) { 
       print CSV "Test Name," if defined $testName;
       print CSV join( ",", ( sort keys %{$resHash} ) ) . "\n";
    }

    foreach $key ( sort keys %{$resHash} ) { 
        push @vals, $resHash->{$key} ;
    }

    print CSV "$testName," if defined $testName;
    print CSV join( ",", @vals ) . "\n";

    close CSV;
}

# ---------------------------------------------------------------------------------
sub purge_stats_files
{
    my ( $fileArrayRef ) = @_;

    foreach my $file ( @{$fileArrayRef} ) { 
        if ( -e $file ) {
            print "Removing file $file\n" if not $quiet;
            unlink $file;
        }
        if ( -e $file ) {
            print "Failed to remove file $file - quitting\n";
            exit;
        }
    }

}
# ---------------------------------------------------------------------------------
sub get_average_ds_per_rrd
{
    my ( $rrds, $rrd, $dsSum, $dsCount, $rrdCount ) ;
    $rrds = '/usr/local/groundwork/rrd';

    if ( ! -e $rrds or ! -d $rrds ) { 
        die "No $rrds directory found - quitting\n";
    }
    if ( ! -e "/usr/local/groundwork/common/bin/rrdtool" or ! -x "/usr/local/groundwork/common/bin/rrdtool" ) {
        die "No luck with /usr/local/groundwork/common/bin/rrdtool\n";
    }

    $rrdCount = 0;
    foreach my $rrd ( < $rrds/*.rrd > ) {
        $dsCount = `/usr/local/groundwork/common/bin/rrdtool info $rrd | grep '].index = ' | wc -l`;
        print "$rrd: $dsCount";
        $dsSum += $dsCount;
        $rrdCount++;
    }

    if ( $rrdCount == 0 ) { 
    	print "No RRD's found.\n";
    }
    else {
    	printf "Average RRD ds's for rrd's found in $rrds: %0.2f\n", $dsSum / $rrdCount;
    }
    exit;
}

# ---------------------------------------------------------------------------------
sub gwauth
{
	my ( $token_ref, $UrlRef ) = @_;
    if ( ! -e $wsClientProps or ! -r $wsClientProps ) { 
        die "Can't find/read $wsClientProps - quitting\n";
    }

	my $user = `grep ^webservices_user $wsClientProps | cut -d= -f2`;
	my $password = `grep ^webservices_password $wsClientProps | cut -d= -f2`;
	${$UrlRef} = `grep ^foundation_rest_url $wsClientProps | cut -d= -f2`;

	chomp $user; 
	$user =~ s/(^\s+|\s+$)//g;
    chomp $password; 
	$password =~ s/(^\s+|\s+$)//g;
    chomp ${$UrlRef};
	$$UrlRef =~ s/(^\s+|\s+$)//g;

	$user = encode_base64($user); # this adds a \n again
	chomp $user;

	#print "curl -s -k -i -X POST  --data-urlencode \"user=$user\" --data-urlencode \"password=$password\" --data-urlencode \"gwos-app-name=$GWAppName\" $$UrlRef/auth/login | tail -1\n";
 	${$token_ref} = ` curl -s -k -i -X POST  --data-urlencode \"user=$user\" --data-urlencode \"password=$password\" --data-urlencode \"gwos-app-name=$GWAppName\" ${$UrlRef}/auth/login | tail -1`;
    
}


# ---------------------------------------------------------------------------------
sub parse_opts
{

    my $helpstring = "
Usage : $0  
            -hosts #  
		Number of GroundWork hosts
		Default: $numHosts

            -services # 
		Number of GroundWork services
		Default: $numServices

            -metrics # 
		Number of metrics per service
		Default: $mps

            -intervals # 	
		Number of intervals
		Default: $intervals

            -gap # 
		Gap between time intervals
		Default: $intervalGap

            -batchsize # 
		InfluxDB write batch size
		Default: $batchSize

            -reps # 	
		How many times to run the same test and then average results over.
		Default: $repetitions

            -ntags #
		Number of InfluxDB tags to include.
		Tags are hostgroup_<N>, ie hostgroup_1, hostgroup_2,...
		Useful for explorating relationship between tag set cardinality and write time metrics
		Default: $nTags

            -yes 
     		Auto answer yes to start tests running
 		Default: no

            -csv <file>
		Specify a csv file to write the test results to
 		Default: $resultsCSV

            -quiet
		Do it quietly. 
		Default: not quiet

            -ssl
		Configurates to run tests over ssl.

            -notags
 		Sets tag set cardinality to zero.

            -useSTT
		Curl's total time taken will be calculated as timetotal - time_starttransfer 
 		Useful for ruling out DNS and SSL slowness/factors

            -auth <user:pass>
		Sets InfluxDB authentication creds - this is for performance profiling with auth enabled

            -gw
		Use GroundWork REST API instead of InfluxDB API

            -batchmode
		Run tests in batch mode.
		Batch tests are defined in define_and_run_tests() internally today.

            -multi
		Use this if running multiple instances of $0 when doing concurrent tests
		to emulate multiple feeders 

            -readtest
		Do read performance testing rather than write performance testing

            -rrds 
		Calculates the average number of datasources per RRD found in
		/usr/local/groundwork/rrd. 
 		This is useful for setting a sane value for -metrics #.

            -influx_storage <path>
		Determines which path to use for doing storage calculations on.
		Default: $influxStorage
";


    GetOptions(
        'yes'          => \$yes,
        'services=i'   => \$numServices, # num services per host
        'hosts=i'      => \$numHosts, # num hosts
        'metrics=i'    => \$mps,  # num metrics per service
        'intervals=i'  => \$intervals,  # number of intervals
        'gap=i',       => \$intervalGap, # gap in seconds between intervals
        'batchsize=i'  => \$batchSize, # influxdb api batch size
        'help'         => \$help,
        'reps=i'       => \$repetitions,
        'csv=s'        => \$resultsCSV,
        'quiet'        => \$quiet,
        'rrds'         => \$rrds,
        'ssl'          => \$ssl,
        'notags'       => \$noTags,  # turn off tags
        'useSTT'       => \$useStartTimeTransfer, # sum up curl's timetotal - time_starttransfer (nice to rule out DNS and SSL slowness/factors). No useSTT => just sum time_total
        'auth=s'       => \$auth, # eg -auth gw:gw and indicates the auth is enabled and to be tested with
        'gw'           => \$useGWRESTAPI, # run the test through the GW REST API
        'batchmode'    => \$batchMode, # run in batch mode using list of tests defined herein
        'justprep'     => \$justPrep, # just prep but don't run tests (for multi instance testing)
        'multi'        => \$multi, # for concurrent testing
        'readtest'     => \$readTest,
        'ntags=i'      => \$nTags, # num tags
        'hgs=i'        => \$numHostGroups, # num host groups per host
        'influx_storage=s'        => \$influxStorage, # num host groups per host
    ) or die "$helpstring\n";

    if ( defined $help ) { print $helpstring; exit; }
    if ( defined $ssl ) { 
        $curlSSLOpts = "-k";
        $cliSSLOpts = "-ssl -unsafeSsl";
        $proto = "https";
    }
    if ( defined $auth ) {
       $authAdminOpts = "-u admin:admin"; # I already created an admin user with these creds: CREATE USER admin WITH PASSWORD 'admin' WITH ALL PRIVILEGES
       $authNonAdminOpts = "-u $auth"; # I already created a non admin user gw/gw: > CREATE USER gw WITH PASSWORD 'gw' > GRANT ALL ON "groundwork" TO "gw"
       $cliAuthOpts = "-username admin -password admin";
    }
    else { 
        $cliAuthOpts = $authAdminOpts = $authNonAdminOpts = ""; 
    }

    get_average_ds_per_rrd() if defined $rrds  ;

    $resultsCSV .= ".$pid"; # useful for if many of these scripts are running at once

    if ( defined $readTest ) {
        print "Prep InfluxDB with : ./tsperf.pl -host 300 -ser 20 -metr 2 -interval 2880 -gap 300 -batchsize 25000\n"; # - hit enter\n"; <STDIN>;
    }

    if ( defined $useGWRESTAPI ) { 
        if ( not defined $numHostGroups or not $numHostGroups ) {
            die "When using -gw, -hgs # needs to be used and # > 0\n";
        }
    }
    
    if ( defined $influxStorage ) { 
       if ( ! -d $influxStorage or ! -r $influxStorage ) { 
            die "InfluxDB storage directory '$influxStorage' doesn't exist or isn't readable\n";
       }
    }
   
}

# ---------------------------------------------------------------------------------
sub mean {
    my(@data) = @_;
    my $sum;
    foreach(@data) {
        $sum += $_;
    }
    return($sum / @data);
}

sub median {
    my(@data)=sort { $a <=> $b} @_;
    if (scalar(@data) % 2) {
        return($data[@data / 2]);
    } 
    else {
        my($upper, $lower);
        $lower=$data[@data / 2];
        $upper=$data[@data / 2 - 1];
        return(mean($lower, $upper));
    }
}

sub std_dev {
    my(@data)=@_;
    my($sq_dev_sum, $avg)=(0,0);

    $avg = mean(@data);
    foreach my $elem (@data) {
        $sq_dev_sum += ($avg - $elem) **2;
    }
    return(sqrt($sq_dev_sum / ( @data - 1 )));
}

# ---------------------------------------------------------------------------------
sub create_groundwork_objects
{
    # first create some host groups to which the hosts will be members

    use GW::RAPID;
#   use Log::Log4perl qw(get_logger); Log::Log4perl::init('GW_RAPID.log4perl.conf'); my $logger = get_logger("GW.RAPID.module");

    my ( $rest_api, @hosts, $hostCount, @hostNames,
         %outcome, @results,
         @hostGroups, $hostGroupCount,
    );


    print "Deleting any existing host_* hosts ...\n";
    system(" psql -c \"delete from host where hostname like 'host_%';\" gwcollagedb" );

    print "Deleting any existing hostgroup_* hostgroups ...\n";
    system(" psql -c \"delete from hostgroup where name like 'hostgroup_%';\" gwcollagedb" );

    # this was for a version where the hg cache was refreshing too slowly and needed kicking by redploying
    if ( defined $forceRedploy ) {
        # Need to force the hg cache to refresh 
        system( "touch /usr/local/groundwork/foundation/container/jpp2/standalone/deployments/foundation-webapp.war.dodeploy");
        # Takes time for the isdeploying to show...
        while ( not -e "/usr/local/groundwork/foundation/container/jpp2/standalone/deployments/foundation-webapp.war.isdeploying" ) {
            print "Waiting for deploy ...\n";
            sleep 1;
        }
        # now wait for it to go away
        while ( -e "/usr/local/groundwork/foundation/container/jpp2/standalone/deployments/foundation-webapp.war.isdeploying" ) {
            print "Waiting for foundation-webapp.war to finished deploying...\n";
            sleep 1;
        }
    }

    # Do this after redeploy cos token cache goes bye bye
    $rest_api = GW::RAPID->new( undef, undef, undef, undef, 'perftesting', { access => '/usr/local/groundwork/config/ws_client.properties' });

    print "Creating GW objects : $numHosts hosts, $numHostGroups hgs ...\n";

    # create the hosts
    for ( $hostCount = 1; $hostCount <= $numHosts; $hostCount++ ) {
        push @hosts, { hostName => "host_$hostCount", appType=>"OS", deviceIdentification=>"host_$hostCount" };
        push @hostNames, { hostName => "host_$hostCount" }  ; # for hgs later
    };
    print "Creating $numHosts GW hosts : host_1 ... host_$numHosts ...\n" if not $quiet;
    if ( not $rest_api->upsert_hosts(  \@hosts, {}, \%outcome, \@results ) ) {
        die "Failed to upsert hosts : " . Dumper \%outcome, \@results;
    }

    # assign to N hostgroups
    for ( $hostGroupCount = 1; $hostGroupCount <= $numHostGroups; $hostGroupCount++ ) {

        print "Assigning " . scalar @hostNames . " hosts to hostgroup hostgroup_$hostGroupCount\n" if not $quiet;
        # Need to do n-ary bundling for this op else can get 500's from api for large num hosts (1500+)
        my @hostgroupsBundle = ();
        my @theHosts = @hostNames;
        while ( @hostgroupsBundle = splice @theHosts, 0, 250 ) {

            print "\tProcessing bundle of " . ($#hostgroupsBundle + 1 ) . " hosts \n"  if not $quiet;
            @hostGroups = { "name" => "hostgroup_$hostGroupCount", "hosts" => \@hostgroupsBundle } ;
            if ( not $rest_api->upsert_hostgroups( \@hostGroups, {}, \%outcome, \@results ) ) { 
                die "Failed to upsert hostgroups : " . Dumper \%outcome, \@results;
            }

        }
    }
    #print "Hit Enter\n"; <STDIN>;
}
