#!/usr/local/groundwork/perl/bin/perl
use Data::Dumper;
use File::Copy;

my $groundwork_dir="/usr/local/groundwork";
my $views_file="$groundwork_dir/core/performance/performance_views/views.xml";

my $new_dir="$groundwork_dir/nagvis";
my $map_dir="$new_dir/etc/maps";
my $img_dir="$new_dir/share/userfiles/images";
my $temp_dir="$new_dir/share/userfiles/templates";

my $backup_dir=$ARGV[0];
my $backup_map_dir="$backup_dir/etc/maps";
my $backup_img_dir="$backup_dir/nagvis/images";
my $backup_temp_dir="$backup_dir/nagvis/templates";

# load performance views
open (IF, "<$views_file");
my %views;
while (<IF>) {
	my $line=$_;
	if ($line =~ /view name="(.*?)".*CDATA\[(.*)\]\]/) {
		$views{$2}=$1;
	}
}
close(IF);

# load all old maps
opendir(MAPS, $backup_map_dir);
my @files= grep { /\.cfg$/ && -f "$backup_map_dir/$_" } readdir(MAPS);
closedir(MAPS);
%maps = ();
foreach my $map (@files){
	open (IF, "<$backup_map_dir/$map") ;
	my @lines;
	while (<IF>) {
		push(@lines, $_);
	}
	$maps{$map} = \@lines;
	close(IF);
}

# process the maps
foreach $map (keys %maps){
	
	my @lines = @{$maps{$map}};
	my @cache = ();
	my $in_performance = 0;
	
	open (OF, ">$map_dir/$map");
	
	for(my $i = 0; $i < @lines; $i++){
		my $line = $lines[$i];
		
		# smart check for performance objects
		if ($line=~/define performance {/){
			$in_performance = 1;
			$line="define container {\n";
		}elsif ($in_performance && $line=~/}/){
			$in_performance = 0;
			if (!$perf_height) {$perf_height=500;};
			if($perf_allowed){ 
			    push(@cache,"w=724\nh=$perf_height\n");
			    $line = join("",@cache).$line; 
			}
			else { $line = ""; }
			@cache = ();
		}
		
		# change performance configuration
		if ($line=~/perfconfig=(.+)$/) {
			my $perfconfig=$1;
			if (exists($views{$perfconfig})) {
				$line="url=".$views{$perfconfig}."\n";
				$perf_allowed = 1;
			}else { $perf_allowed = 0; }
		}elsif ($line=~/perf=(.+)$/) {
			my $perfconfig=$1;
			$line="url=/performance/cgi-bin/performance/perfchart.cgi?update_main=1&view=get_view&file=".$perfconfig."\n";
			$perf_allowed = 1;
		# upgrade line_type configuration
		}elsif ($line=~/line_type=.*$/) {
			$line="view_type=line\n$line";
		# remove depricated configuration items
		}elsif ($line=~/allowed_user=.*$/ || $line=~/allowed_for_config=.*$/ || $line=~/hover_timeout=.*$/ || $line=~/usegdlibs=.*$/ || $line=~/backend_id=.*$/ || $line=~/graph_timespan=.*$/){
			$line="";
		# hover_url needs to have a schema
		}elsif ($line=~/hover_url=.*$/) {
			if (!($line=~/hover_url=.+:.*$/)) {
				$line = "";
			}
		}
		
		# write to file or cache it first
		if($in_performance){
			if ($line=~/height=(.+)$/) {
			  $perf_height=$1;
			} elsif ($line!~/hover_menu=.*$/ && $line!~/popup=.*$/) {
			  push(@cache,$line);
			}
		}else {
			print OF $line;
		}
	}
	
	# close the file
	close(OF);
	
}

# copy images
my @types = ("maps", "iconsets", "shapes", "templates/hover", "templates/header");
foreach my $type (@types){
	my $type_images="$backup_img_dir/$type";
	if ( -e $type_images ) {
	  opendir(IMAGES, $type_images);
	  my @image_files = grep { -f "$type_images/$_" } readdir(IMAGES);
	  rewinddir(IMAGES);
	  my @image_dirs = grep { /^[^.]/ && -d "$type_images/$_" } readdir(IMAGES);
	  closedir(IMAGES);
	  
	  foreach my $file (@image_files) {
		  if(! -e "$img_dir/$type/$file" ){
			  copy("$type_images/$file","$img_dir/$type/$file");
		  }
	  }
	  foreach my $dir (@image_dirs) {
		  `/bin/cp -a "$type_images/$dir" "$img_dir/$type/"`;
	  }
        }
}


# process templates

# load hover templates
opendir(HOVER_TEMPLATES, "$backup_temp_dir/hover");
my @hover_templates = grep { /(tmpl)/ && -f "$backup_temp_dir/hover/$_" } readdir(HOVER_TEMPLATES);
closedir(HOVER_TEMPLATES);

# rename and copy hover templates
foreach my $file (@hover_templates) {
	
	if($file=~/^tmpl\.(.+)\.(.+)$/){
		my $name = $1;
		my $ext = $2;
		
		if($name != "default"){
			copy("$backup_temp_dir/hover/$file","$temp_dir/$name.hover.$ext");
		}
	}
}

# load header templates
opendir(HEADER_TEMPLATES, "$backup_temp_dir/header");
my @header_templates = grep { /(tmpl)/ && -f "$backup_temp_dir/header/$_" } readdir(HEADER_TEMPLATES);
closedir(HEADER_TEMPLATES);

# rename and copy hover templates
foreach my $file (@header_templates) {
	
	if($file=~/^tmpl\.(.+)\.(.+)$/){
		my $name = $1;
		my $ext = $2;
		
		if($name != "default"){
			copy("$backup_temp_dir/header/$file","$temp_dir/$name.header.$ext");
		}
	}
}

