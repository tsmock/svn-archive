##################################################################
package Geo::Filter::Area;
##################################################################

use strict;
use warnings;
use Carp;

use Data::Dumper;

# ------------------------------------------------------------------
my $AREA_DEFINITIONS = {
    #                     min    |    max  
    #                  lat   lon |  lat lon
    uk         => [ [  49.7,-7.6, 58.8, 3.2  ], # GB
		            [ 49.9 ,-5.8,   54, 0.8  ], # NI
		    ],
    iom        => [ [  49  , -11,   64,   3  ] ],
    france     => [ [  42.3,-1.7, 51.1, 8.2  ] ],
    germany    => [ [  47  ,   5,   54,  16  ] ],
    spain      => [ [  35.5,  -9,   44,   4  ] ],
    europe     => [ [  35  , -12,   75,  35  ],
		    [  62.2,-24.4,66.8,-12.2], # Iceland
		    ],
    africa     => [ [ -45  , -20,   30,  55  ] ],
    # Those eat up all memory on normal machines
    world_east => [ [ -90  , -30,   90, 180  ] ], 
    world_west => [ [ -90  ,-180,   90, -30  ] ],
    world      => [ [ -90  ,-180,   90, 180  ] ],
};

# ------------------------------------
my $stripe_lon     = -180;
my $stripe_step    = 5;
my $stripe_overlap = 0.2;
while ( $stripe_lon < 180 ){
    my $stripe_lon1=$stripe_lon+$stripe_step+$stripe_overlap;
    $AREA_DEFINITIONS->{"stripe_${stripe_lon}_${stripe_lon1}"} =
	[ [ -90,$stripe_lon,   
	    90, $stripe_lon1] ];
    $stripe_lon=$stripe_lon+$stripe_step;
}

# ------------------------------------
sub new($;@){
    my $class = shift;
    if( scalar(@_) % 2 ) {
	Carp::confess("uneven amount of options\n");
	};
    my %options  = @_;

    my $self={};
    my $area;
    if ( defined $options{lat_min} &&
	 defined $options{lon_min} &&
	 defined $options{lat_max} &&
	 defined $options{lon_max} 
	 ) {
	$self->{area_filters}= [
				[$options{lat_min},$options{lon_min},
				 $options{lat_max},$options{lon_max}]
				];
	$self->{area_name} = "[$options{lat_min},$options{lon_min}".
	    " .. ".
	    "$options{lat_max},$options{lon_max}]";
	    
    } elsif ( defined $options{area} ) {
	$area = delete  $options{area};
	if ( ! defined ( $AREA_DEFINITIONS->{$area} ) ) {
	    die "unknown area $area.\n".
		"Allowed Areas:\n\t".
		join("\n\t",$class->allowed_areas())."\n";
	}
	$self->{area_name} = $area;
	$self->{area_filters} = $AREA_DEFINITIONS->{$area};
    }
    
    if( scalar(@_) % 2 ) {
	Carp::confess("uneven amount of options for area->new()\n");
	};
    bless ($self,$class);
    return $self;
}

sub list_areas($) {
    my $class = shift;
    return join("\n",$class->allowed_areas());
}

sub allowed_areas($) {
    my $class = shift;
    return sort keys %{$AREA_DEFINITIONS};
}

sub name($) {
    my $self = shift;
    return $self->{area_name};
}

# ------------------------------------------------------------------

sub inside($$){
    my $self = shift;
    my $obj  = shift;

    my $area_filters=$self->{area_filters};
    #print "in_area(".Dumper(\$obj).")";;
    #print Dumper(\$area_filters);
    for my $a ( @{$area_filters}  ) {
	if (
	    $obj->{lat} >= $a->[0] &&
	    $obj->{lon} >= $a->[1] &&
	    $obj->{lat} <= $a->[2] &&
	    $obj->{lon} <= $a->[3] ) {
	    return 1;
	}
    }
    return 0;
}

# ------------------------------------------------------------------

my $test_area = Geo::Filter::Area->new(area=>"uk");
if ( $test_area->inside({lat=>10,lon=>10})) {
    Carp::confess("Area filters are not working\n");
};
1;
