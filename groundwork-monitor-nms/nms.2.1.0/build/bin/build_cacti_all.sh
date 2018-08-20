source ./error_handling.sh

./build_cacti-spine.sh		|| bomb_out cacti-spine
./build_cacti.sh		|| bomb_out cacti
./build_cacti-plugin-arch.sh	|| bomb_out cacti-plugin-arch
./build_weathermap.sh		|| bomb_out weathermap
./build_thold.sh		|| bomb_out thold
