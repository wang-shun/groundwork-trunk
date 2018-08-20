#!/usr/bin/perl

package Testhttpd;
use lib qw(../);
use GWInstaller::AL::httpd;
use GWTest::GWTest;

@ISA = qw(GWTest);

sub test_init{
        $pass = 0;
        $prop = GWInstaller::AL::httpd->new();
        $prop->isa(GWInstaller::AL::httpd)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_get_port{
        $prop = GWInstaller::AL::httpd->new();
        $prop->set_port(4913);
        (($prop->get_port()) == 4913)?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_port{
        $prop = GWInstaller::AL::httpd->new();
        return test_get_port();
}

sub test_get_hostname{
        $prop = GWInstaller::AL::httpd->new();
        $prop->set_hostname("localhost");
        (($prop->get_hostname()) eq "localhost")?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_hostname{
        $prop = GWInstaller::AL::httpd->new();
        return test_get_hostname();
}

sub test_get_identifier{
        $prop = GWInstaller::AL::httpd->new();
        $prop->set_identifier("test_identifier");
        (($prop->get_identifier()) eq "test_identifier")?($pass = 1):($pass = 0);
        return $pass;
}

sub test_set_identifier{
        $prop = GWInstaller::AL::httpd->new();
        return test_get_identifier();
}

1;
