package MIB::Check;

use strict;
use warnings;

# put following in .emacs to make $TS auto-update:
# (add-hook 'before-save-hook 'time-stamp)
my $TS = 'Time-stamp: "2008-02-19 12:36:23 carlos"';

use base 'CGI::Application';

my $project = 'snmp/mibtool';
use CGI::Carp qw(fatalsToBrowser warningsToBrowser);
use HTML::Template;
use File::Temp qw/ tempdir /;

$ENV{'PATH'} = '/bin:/usr/bin';

my $script   = "/cgi-bin/$project/index.cgi";
my $base_dir = "/usr/local/groundwork/common";
my $tmpl_dir = "/usr/local/groundwork/tools/$project/tmpl";
my $smilint_binary = "smilint";
my $SEVERITY_DEFAULT = 3; # TODO: what is the correct default?
my $temp_dir;

sub setup {
  my $self = shift;
  $self->header_props(-charset=>'UTF-8');
  my $q = $self->query();

  $self->start_mode('home');
  $self->mode_param('rm');
  $self->run_modes(
		   'home'  => 'home',
		   'check' => 'check',
		  );
}

sub home {
    my $self = shift;
    my $q = $self->query();

    my $cgi_ts = get_script_timestamp();
    my $title  = 'MIB Validator';

    my $template = HTML::Template->new(filename => "$tmpl_dir/three-col.tmpl");

    $template->param(TITLE         => $title);
    $template->param(JAVASCRIPT    => "");
    $template->param(CSS           => "");
    $template->param(BODYHEAD      => $title);
    $template->param(LEFT_COL_TEXT => "");
    $template->param(RIGHT_COL_TEXT => "");
    $template->param(MAIN_COL_HEAD => "Input MIB Details");
    $template->param(MAIN_COL_TEXT => build_mib_form($self));
    $template->param(BODYFOOT      => "<!-- $cgi_ts -->\n");

    return $template->output;
}

sub build_mib_form {
    my $self = shift;
    my $q = $self->query();

    my $form_start = $q->start_multipart_form();

    my $content = qq(
    $form_start
    <label for="severity">Maximum severity level to show:</label>
    <select id="severity" name="severity">
      <option value="0">0 - Severe service internal errors</option>
      <option value="1">1 - Major errors, must be fixed</option>
      <option value="2">2 - Errors, probably tolerated by some implementations</option>
      <option value="3">3 - Errors, tolerated by many implementations</option>
      <option value="4">4 - Warnings, in most cases recommended to be changed</option>
      <option value="5">5 - Minor warnings or hints</option>
      <option selected value="6">6 - Auxiliary notices</option>
    </select><br>
    <label for="options">Options:</label>
    <fieldset id="options">
      <input type="checkbox" class="input" id="recursive" name="recursive">
        <span class="checkboxlabel">Report errors for included modules</span><br>
      <input type="checkbox" class="input" id="showlevel" name="showlevel">
        <span class="checkboxlabel">Show severity level for reported errors and warnings</span><br>
      <input type="checkbox" class="input" id="shownames" name="shownames">
        <span class="checkboxlabel">Show names and descriptions of reported errors and warnings</span>
    </fieldset><br>
    <label for="mibfile">MIB File:</label>
      <input id="mibfile" class="required" name="mibfile" type="file" size="30" maxlength="1000000"><br>
    <label for="field">Include file:</label>
      <div id="field">
      <input id="incfile" class="required" name="incfile" type="file" size="30" maxlength="1000000"><br>
      <label for="multhelp"> </label>
      <span class="fieldhelp" id="multhelp">For multiple include files, submit as .zip or .tar.gz file.</span>
      </div>
      <input type="submit" id="submit" name="submit" value="Check">
    <input class="hidden" type="hidden" name="rm" value="check">
    </form>);
    unindent($content);

    return $content;
}

sub get_script_timestamp {
  my $timestamp = $TS;
  $timestamp =~ s/^Time\-stamp:\s+"(.*)\s+\w+"$/$1/;

  return $timestamp;
}

sub check {
  my $self = shift;
  my $q = $self->query();
  my $command = '';
  my $results = '';

  my $title  = 'MIB Validator';

  if (!defined($q->upload('mibfile'))) {
    return generate_html($title, "Missing Input", "Please go back and click \"Browse\" to choose a MIB file to upload from your file system.");
  }

  # get params
  my %options = ();
  $options{'severity'} = $q->param('severity')  || '';
  $options{'recursive'}= $q->param('recursive') || '';
  $options{'showlevel'}= $q->param('showlevel') || '';
  $options{'shownames'}= $q->param('shownames') || '';

  # save files
  $temp_dir = tempdir( CLEANUP => 0 );
  my $mib_file = save_file($q->upload('mibfile'), $temp_dir, 1);
  my $mib_file_short = $mib_file;
  $mib_file_short =~ s{.*/}{};

  # save and extract include files, if any
  my $inc_file = '';
  if (defined($q->upload('incfile'))) {
    $inc_file = save_file($q->upload('incfile'), $temp_dir, 0);
    # unpack the tar/zip/tgz/tar.gz include file, if compressed
    unpack_archive($inc_file);
  }

  # check on the import/include files
  my %imports = ();
  @{$imports{includes}} = ();
  mibfile_check_imports($mib_file, \%imports); # no 3rd arg here
  if ( exists $imports{missing} and @{ $imports{missing} } ) {
    # required include files missing; check will not be done.
    my @missing = @{$imports{missing}}; # not needed; just for formatting
    $results = "<p>The following additional files are required to "
      . "satisfy dependencies in your MIB file:</p>\n"
      . "<ul>\n"
      .     "<li>" . join("</li>\n<li>", @missing) . "</li>\n"
      . "</ul>\n";
  }
  else { # everything is ready to go; do the check
    # get the names, with paths, for each include file found
    my @includes = map { $imports{satisfied}{$_} }
      keys %{$imports{satisfied}};
    # and use them to build and run the command
    $command = build_check_command($mib_file, \@includes, \%options);
    $results = run_check($temp_dir, $command);
  }
  $results = "<p>Command run was:</p>\n<p>\n<pre>$command</pre></p>\n"
    . $results unless ($command eq '');

  return generate_html($title, "Results of MIB Validation", $results);
}


sub generate_html {
  my $title  = shift;
  my $head   = shift;
  my $body   = shift;
  my $cgi_ts = get_script_timestamp();

  my $template = HTML::Template->new(filename => "$tmpl_dir/three-col.tmpl");
  $template->param(TITLE         => $title);
  $template->param(BODYHEAD      => $title);
  $template->param(MAIN_COL_HEAD => $head);
  $template->param(MAIN_COL_TEXT => $body . "<br>\n");
  $template->param(BODYFOOT      => "<!-- $cgi_ts -->\n");

  return $template->output;
}


sub build_check_command {
    my $mib_file  = shift;
    my $inc_ref   = shift;
    my $options   = shift;

    my $includes = '';
    # put each include file in quotes after its own -p switch
    $includes = '-p "' .join('" -p "', @$inc_ref). '"'
      if (scalar @$inc_ref > 0);

    if ($^O eq 'darwin' && $smilint_binary !~ /\.dSYM$/) {
      $smilint_binary .= '.dSYM'
    }

    my $smipath = "$base_dir/bin/$smilint_binary";
    my $command = "$smipath $includes \"$mib_file\"";
    my $severity = $SEVERITY_DEFAULT;

    if ($options->{severity} =~ /^(\d+)$/) {
	$severity = $1;
    }
    $command .= " -l $severity";

    if ($options->{recursive} ne '') { $command .= " -r"; }
    if ($options->{showlevel} ne '') { $command .= " -s"; }
    if ($options->{shownames} ne '') { $command .= ' -m'; }

    return $command;
}


sub run_check {
    my $file_dir  = shift;
    my $command = shift;

    # need to run the check from the scratch directory
    my $prev_dir;
    my $prev_dirT = `/bin/pwd`;
    # TODO: improve this regexp
    if ($prev_dirT =~ m{^([-./\w ]{1,255})$}) { $prev_dir = $1; }
    return "Error determining current directory"
	unless (defined($prev_dir));
    chdir($file_dir);

    open (my $cmd, "($command | sed 's/^/STDOUT:/') 2>&1 |");
    my $results_stderr = '';
    my $results_stdout = '';
    while (<$cmd>) {
      if (s/^STDOUT://)  {
	my $line = $_;
	$line =~ s{^$temp_dir/?}{};
	$results_stdout .= $line . "<br>\n";
      } else {
	my $line = $_;
	$line =~ s{$temp_dir/?}{};
	$results_stderr .= $line . "<br>\n";
      }
    }
    close($cmd);

    chdir($prev_dir);

    my $results;
    $results  = "<div class=\"smi_stdout\">$results_stdout</div>\n";
    $results .= "<div class=\"smi_stderr\">$results_stderr</div>\n";

    return $results;
}


sub unpack_archive {
    my $inc_file = shift;

    my ($ext,$file_dir,$prev_dir);
    if ($inc_file =~ /.*?\.(\w+|zip|tar\.gz|tgz|tar)$/i) {
	$ext = $1;
    }
    $file_dir = $inc_file;
    $file_dir =~ s{/[^/]+$}{};

    my $prev_dirT = `/bin/pwd`;
    # TODO: improve this regexp
    if ($prev_dirT =~ m{^([-./\w ]{1,255})$}) {
	$prev_dir = $1;
    }
    chdir($file_dir);

    my $tar_bin = (-x '/bin/tar') ? '/bin/tar' : '/usr/bin/tar';
    my $silence = '> /dev/null 2>&1 &'; # run in background and ignore output

    $silence = '';

    if (defined($ext)) {
      if ($ext eq 'zip') {
	my $zip_args = "-d \"$file_dir\" \"$inc_file\"";
	system("/usr/bin/unzip $zip_args $silence");
      }
      elsif ($ext eq 'tar') {
	system("$tar_bin xf $inc_file $silence");
      }
      elsif ($ext eq 'tgz' || $ext eq 'tar.gz') {
	system("$tar_bin xzf $inc_file $silence");
      }
    }
    chdir($prev_dir);

}


sub save_file {
    my $file = shift;
    my $file_dir = shift;
    my $unique = shift;
    my $fileT = $file;
    my $fn = "";

	$fileT =~ s{.*[/\\]}{}; #Stip of path so asto have only file name in $fileT 
    if ($fileT =~ /^([-\w. ]{1,255})$/i) { # untaint
      $fn = $1;
    }

    if ($unique) {
      $fn .= ".$$";
    }
    my $file_path = "$file_dir/$fn";
    system("/bin/mkdir -p $file_dir") unless (-d $file_dir);
    `/bin/chmod -R 777 $file_dir`;

    my $length     = 0;
    my $file_data  = "";

    open(my $out, '>', "$file_path.tmp")
	or die "error opening $file_path.tmp: $!";
    while (my $bytes_read = read($file,$file_data,1024)) {
      print $out $file_data;
      $length += $bytes_read;
    }
    close($out);

    if ($length != (-s "$file_path.tmp")) {
      die "error uploading [$file_path.tmp]. Please try again.\n";
    }

    # Pre-process the file to remove \r (carriage returns). Don't
    # do this in the above block, because \n\r may be split by the
    # binary read, and stripping them at that point could alter
    # the line numbering.
    if ($file_path !~ /(?:tar|tgz|gz|gzip|zip)$/) {
      # if NOT a binary tar/tgz/gz/gzip/zip file
      open(my $in, "$file_path.tmp") or die "error opening $file_path.tmp: $!";
      open(my $out, '>', $file_path) or die "error opening $file_path: $!";
      while (my $line=<$in>) {
	$line =~ s{[\n\r]+}{};
	print $out "$line\n"; # preserve empty lines
      }
      close($out);
      close($in);
      unlink("$file_path.tmp");
    }
    else {
      rename("$file_path.tmp", $file_path);
    }

    return $file_path;
}

# mibfile_check_imports()
#
# Recursively find imports required in MIB files.
#
sub mibfile_check_imports {
  my $mibname = shift;
  my $imports = shift; # warning: caller's values will be altered
  my $recurse = shift; # do not use this argument on first invocation

  $mibname =~ s{.*/}{};

  return if ($imports->{satisfied}{$mibname});

  my $mibfile = '';
  my $mibfile_T = '';

  if (defined($recurse)) {
    # use mibname to find file, if it exists
    $mibfile_T = '';
    if (-e "$temp_dir/$mibname") { # simple case; mibname is filename
      $mibfile_T = $mibname;
    }
    else {
      # look for the MIB file
      opendir(DIR, $temp_dir) or die "$0 error opening temp dir $temp_dir\n";
      my @tmp_files = grep(!/\.(?:zip|tar|gz|gzip|tgz)$/,
			   grep(!/(?:#~)$/, grep(!/^\.{1,2}$/, readdir(DIR))));
      closedir(DIR);
      my $mibname_regexp = make_mibname_regexp($mibname);
      foreach my $file (@tmp_files) {
	if ($file =~ m/^$mibname_regexp/i) {
          $mibfile_T = $file;
	  last;
	}
      }
    }

    # untaint
    if ($mibfile_T =~ /^([-\w. ]{1,255})/) {
      $mibfile = $1;
    }

    # if file found, mark the name as satisfied and save the path.
    # otherwise, save the name on a list of missing MIB dependencies.
    # Note on the first  invocation (without the $recurse variable set)
    # we assume the file exists, because that's the file we're starting
    # with, the main mib file that is being checked.

    if ($mibfile ne '') {
      # found it
      $imports->{satisfied}{$mibname} = "$temp_dir/$mibfile";
    }
    else {
      # didn't find it
      push(@{$imports->{missing}}, $mibname);
      return; # no use trying to read a file that doesn't exist
    }
  }
  else {
    $mibfile = $mibname;
  }


  # found the file; now read it to look for dependencies
  open(my $in, '<', "$temp_dir/$mibfile")
    or die "$0 error opening $temp_dir/$mibfile: $!";
  my @data = <$in>;
  close($in);

  my $doing_imports = 0;
  foreach my $line (@data) {
    $line =~ s{[\n\r]}{}g;
    if ($doing_imports) {
      if ($line =~ /^\s*      # beginning of line with 0 or more spaces
                     (?:      # open group of 0 or more of
                     \S+?     # non-space token
                     (?:      # open group of zero or more of
                     ,\s*\S+? # tokens preceded by , with optional spaces
                     )*       # close group of 0 or more
                     )?       # close group of 0 or 1
                     \s*      # with optional space
                     \b       # and word break before
                     FROM\s+  # literal text (may be upper or lower case)
                     (\S+?)   # name of required MIB file - non-space string
                     [ ;]*    # followed by space or ; terminator
                     $        # at end of line
                   /ix) {     # i=match any case
        my $name = $1;
        mibfile_check_imports($name, $imports, 1);
      }
      elsif ($line =~ /^\s*$/) {
	$doing_imports = 0;
      }
    }
    elsif ($line =~ /^\s*IMPORTS\s*$/) {
      $doing_imports = 1;
    }
  }
}

sub make_mibname_regexp {
  my $mibname = shift;

  my $regexp = $mibname;
  $regexp =~ s/[-._]/\./g;
  return $regexp;  
}

sub unindent {
    $_[0] =~ s/^[\n\r]*//;
    my ($indent) = ($_[0] =~ /^([ \t]+)/);
    $_[0] =~ s/^$indent//gm;
}



1;

